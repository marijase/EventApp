package marija.sekulic.eventsapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventRepository {

    private final EventDAO eventDAO;
    private final String baseUrl;
    private final Context context; // Dodato za direktan pristup bazi tokom sinhronizacije
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public EventRepository(Context context) {
        this.context = context;
        this.eventDAO = new EventDAO(context);
        this.baseUrl = ServerConfig.BASE_URL;
    }

    // ===== GET ALL EVENTS =====
    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        new Thread(() -> {
            try {
                JSONArray response = null;
                try {
                    response = HttpHelper.getJSONArrayFromURL(baseUrl + "/events");
                } catch (Exception netEx) {
                    loadLocalEventsAndPost(callback);
                    return;
                }

                if (response == null) {
                    loadLocalEventsAndPost(callback);
                    return;
                }

                // 1. Sinhronizujemo podatke sa servera u lokalnu bazu (pametni upis bez dupliranja)
                try {
                    syncEventsFromServer(response);
                } catch (Exception syncEx) {
                    syncEx.printStackTrace();
                }

                // 2. Učitavamo podatke IZ LOKALNE BAZE na ekran da bi dobili prave SQLite ID-eve
                loadLocalEventsAndPost(callback);

            } catch (Exception e) {
                loadLocalEventsAndPost(callback);
            }
        }).start();
    }

    // ===== GET EVENTS BY CATEGORY =====
    public void getEventsByCategory(String category, RepositoryCallback<List<Event>> callback) {
        new Thread(() -> {
            if (category.equalsIgnoreCase("SVE")) {
                getAllEvents(callback);
                return;
            }

            try {
                JSONArray response = null;
                try {
                    response = HttpHelper.getJSONArrayFromURL(baseUrl + "/events/" + category);
                } catch (Exception netEx) {
                    loadLocalEventsByCategoryAndPost(category, callback);
                    return;
                }

                if (response == null) {
                    loadLocalEventsByCategoryAndPost(category, callback);
                    return;
                }

                try {
                    syncEventsFromServer(response);
                } catch (Exception syncEx) {
                    syncEx.printStackTrace();
                }

                loadLocalEventsByCategoryAndPost(category, callback);

            } catch (Exception e) {
                loadLocalEventsByCategoryAndPost(category, callback);
            }
        }).start();
    }

    private void loadLocalEventsAndPost(RepositoryCallback<List<Event>> callback) {
        try {
            List<Event> localEvents = eventDAO.getAllEvents();
            mainHandler.post(() -> callback.onResult(localEvents));
        } catch (Exception e) {
            mainHandler.post(() -> callback.onResult(new ArrayList<>()));
        }
    }

    private void loadLocalEventsByCategoryAndPost(String category, RepositoryCallback<List<Event>> callback) {
        try {
            List<Event> localEvents = eventDAO.getEventsByCategory(category);
            mainHandler.post(() -> callback.onResult(localEvents));
        } catch (Exception e) {
            mainHandler.post(() -> callback.onResult(new ArrayList<>()));
        }
    }

    // ===== CREATE EVENT =====
    public void createEvent(Event event, RepositoryCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("name", event.getName());
                body.put("description", event.getDescription());
                body.put("location", event.getLocation());
                body.put("eventTime", event.getDateTime() != null ? sdf.format(event.getDateTime()) : "");
                body.put("category", event.getCategory());
                body.put("promoted", event.isPromoted());
                body.put("capacity", event.getCapacity());

                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/events", body);

                if (response == null) {
                    mainHandler.post(() -> callback.onError("Nema konekcije. Kreiranje događaja nije moguće offline."));
                    return;
                }

                int status = response.optInt("_httpStatus", 0);
                if (status < 200 || status >= 300) {
                    mainHandler.post(() -> callback.onError("Greška pri kreiranju događaja na serveru."));
                    return;
                }

                Event toSave = parseEventFromJSON(response);
                if (toSave != null) {
                    try { eventDAO.createEvent(toSave); } catch (Exception ignored) {}
                } else {
                    try { eventDAO.createEvent(event); } catch (Exception ignored) {}
                }

                mainHandler.post(() -> callback.onResult(true));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    private List<Event> parseEvents(JSONArray array) {
        List<Event> list = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Event e = parseEventFromJSON(obj);
                if (e != null) {
                    list.add(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // PAMETNA SINHRONIZACIJA: Sprečava duplikate i ažurira ocene/prisustvo sa servera u SQLite
    private void syncEventsFromServer(JSONArray array) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            db = dbHelper.getWritableDatabase();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Event e = parseEventFromJSON(obj);
                if (e != null) {
                    cursor = db.rawQuery("SELECT id FROM events WHERE mongo_id = ?", new String[]{e.getMongoId()});
                    if (cursor.getCount() == 0) {
                        // Ako ne postoji u SQLite, kreiraj ga preko DAO
                        eventDAO.createEvent(e);
                    } else {
                        // Ako već postoji, ažuriraj dinamičke podatke (prisustvo i ocene) sa servera
                        if (cursor.moveToFirst()) {
                            int localId = cursor.getInt(0);
                            db.execSQL("UPDATE events SET brojPrisutnih = ?, prosecnaOcena = ?, brojOcena = ? WHERE id = ?",
                                    new Object[]{e.getAttendingCount(), e.getAverageRating(), e.getRatingCount(), localId});
                        }
                    }
                    cursor.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    private Event parseEventFromJSON(JSONObject obj) {
        try {
            String mongoId = obj.optString("_id", "");
            String name = obj.optString("name", "");
            String desc = obj.optString("description", "");
            String loc = obj.optString("location", "");
            String cat = obj.optString("category", "");
            String timeStr = obj.optString("eventTime", "");
            boolean promoted = obj.optBoolean("promoted", false);
            int capacity = obj.optInt("capacity", 0);
            int attending = obj.optInt("numberOfAttendees", 0);
            double avgRating = obj.optDouble("avgRating", 0.0);
            int numRatings = obj.optInt("numberOfRatings", 0);

            Date date = null;
            try { date = sdf.parse(timeStr); } catch (Exception ignored) {}

            int img = EventFactory.getImageForCategory(cat);

            Event event = new Event(name, desc, loc, date, cat, img, promoted,
                    capacity, attending, avgRating, numRatings);

            event.setMongoId(mongoId);
            return event;

        } catch (Exception e) {
            return null;
        }
    }
}