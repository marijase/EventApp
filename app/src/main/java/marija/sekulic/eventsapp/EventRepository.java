package marija.sekulic.eventsapp;

import android.content.Context;
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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public EventRepository(Context context) {
        this.eventDAO = new EventDAO(context);
        // SADA PONOVO KORISTIMO TVOJ SERVERCONFIG 🎉
        this.baseUrl = ServerConfig.BASE_URL;
    }

    // ===== GET ALL EVENTS =====
    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        new Thread(() -> {
            try {
                // 1. Pokušavamo ONLINE dobavljanje sa servera
                JSONArray response = HttpHelper.getJSONArrayFromURL(baseUrl + "/events");

                if (response == null) {
                    // Ako server vrati null, preusmeri na lokalni DAO (OFFLINE)
                    List<Event> localEvents = eventDAO.getAllEvents();
                    mainHandler.post(() -> callback.onResult(localEvents));
                    return;
                }

                List<Event> events = parseEvents(response);

                // === PAMETNA IZMENA ===
                // Ako je server uspešno odgovorio, ali je njegova MongoDB baza prazna ([]),
                // prikaži naših 18 podrazumevanih događaja iz lokalne SQLite baze da ekran ne bude prazan.
                if (events.isEmpty()) {
                    events = eventDAO.getAllEvents();
                }

                List<Event> finalEvents = events;
                mainHandler.post(() -> callback.onResult(finalEvents));

            } catch (Exception e) {
                // 2. Ako nema konekcije, automatski čita iz SQLite baze
                List<Event> localEvents = eventDAO.getAllEvents();
                mainHandler.post(() -> callback.onResult(localEvents));
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
                JSONArray response = HttpHelper.getJSONArrayFromURL(baseUrl + "/events/" + category);

                if (response == null) {
                    List<Event> localEvents = eventDAO.getEventsByCategory(category);
                    mainHandler.post(() -> callback.onResult(localEvents));
                    return;
                }

                syncEventsFromServer(response);

                List<Event> events = eventDAO.getEventsByCategory(category);
                mainHandler.post(() -> callback.onResult(events));

            } catch (Exception e) {
                List<Event> localEvents = eventDAO.getEventsByCategory(category);
                mainHandler.post(() -> callback.onResult(localEvents));
            }
        }).start();
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
                    eventDAO.createEvent(toSave);
                } else {
                    eventDAO.createEvent(event);
                }

                mainHandler.post(() -> callback.onResult(true));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // ===== POMOCNE METODE =====

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

    private void syncEventsFromServer(JSONArray array) {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Event e = parseEventFromJSON(obj);
                if (e != null) {
                    eventDAO.createEvent(e);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Event parseEventFromJSON(JSONObject obj) {
        try {
            String mongoId = obj.optString("_id", ""); // IZMENA: Čitamo MongoDB jedinstveni ID sa servera
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

            event.setMongoId(mongoId); // IZMENA: Dodajemo pročitan ID u objekat
            return event;

        } catch (Exception e) {
            return null;
        }
    }
}