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

public class AttendanceRepository {

    private final AttendanceDAO attendanceDAO;
    private final String baseUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public AttendanceRepository(Context context) {
        this.attendanceDAO = new AttendanceDAO(context);
        this.baseUrl = ServerConfig.BASE_URL; // Koristimo BASE_URL iz HttpHelper-a po dogovoru
    }

    // ===== SET INTERESTED / ATTENDING =====
    public void markAttendance(String serverUserId, String serverEventId,
                               int localUserId, int localEventId,
                               String commitment, RepositoryCallback<String> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("userId", serverUserId);
                body.put("eventId", serverEventId);
                body.put("commitment", commitment);

                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/attendance", body);

                if (response == null) {
                    mainHandler.post(() -> callback.onError("Nema konekcije. Akcija nije moguća offline."));
                    return;
                }

                int status = response.optInt("_httpStatus", 0);

                if (status == 400) {
                    String msg = response.optString("message", "Kapacitet je popunjen.");
                    mainHandler.post(() -> callback.onError(msg));
                    return;
                }

                if (status < 200 || status >= 300) {
                    mainHandler.post(() -> callback.onError("Greška na serveru."));
                    return;
                }

                if (commitment.equals("ZAINTERESOVAN")) {
                    attendanceDAO.setInterested(localUserId, localEventId);
                } else {
                    attendanceDAO.setAttending(localUserId, localEventId);
                }

                String poruka = commitment.equals("ZAINTERESOVAN")
                        ? "Dodat u zainteresovane."
                        : "Uspešno ste se prijavili!";
                mainHandler.post(() -> callback.onResult(poruka));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // ===== GET ATTENDING EVENTS =====
    // ===== GET ATTENDING EVENTS =====
    public void getAttendingEvents(String serverUserId, int localUserId,
                                   RepositoryCallback<List<Event>> callback) {
        new Thread(() -> {
            try {
                JSONArray response = HttpHelper.getJSONArrayFromURL(baseUrl + "/attendance/" + serverUserId);

                if (response == null) {
                    List<Event> local = attendanceDAO.getAttendingEvents(localUserId);
                    mainHandler.post(() -> callback.onResult(local));
                    return;
                }

                List<Event> attending = parseAttendanceResponse(response, "PRISUSTVUJE");

                // POŠTO SERVER NE ŠALJE DETALJE: Ako je lista prazna, povuci kompletne događaje iz lokalne SQLite baze
                if (attending.isEmpty()) {
                    attending = attendanceDAO.getAttendingEvents(localUserId);
                }

                List<Event> finalResult = attending;
                mainHandler.post(() -> callback.onResult(finalResult));

            } catch (Exception e) {
                List<Event> local = attendanceDAO.getAttendingEvents(localUserId);
                mainHandler.post(() -> callback.onResult(local));
            }
        }).start();
    }

    // ===== GET INTERESTED EVENTS =====
    public void getInterestedEvents(String serverUserId, int localUserId,
                                    RepositoryCallback<List<Event>> callback) {
        new Thread(() -> {
            try {
                JSONArray response = HttpHelper.getJSONArrayFromURL(baseUrl + "/attendance/" + serverUserId);

                if (response == null) {
                    List<Event> local = attendanceDAO.getInterestedEvents(localUserId);
                    mainHandler.post(() -> callback.onResult(local));
                    return;
                }

                List<Event> interested = parseAttendanceResponse(response, "ZAINTERESOVAN");

                // POŠTO SERVER NE ŠALJE DETALJE: Ako je lista prazna, povuci kompletne događaje iz lokalne SQLite baze
                if (interested.isEmpty()) {
                    interested = attendanceDAO.getInterestedEvents(localUserId);
                }

                List<Event> finalResult = interested;
                mainHandler.post(() -> callback.onResult(finalResult));

            } catch (Exception e) {
                List<Event> local = attendanceDAO.getInterestedEvents(localUserId);
                mainHandler.post(() -> callback.onResult(local));
            }
        }).start();
    }
    // ===== FRIENDS ACTIVITY =====
    public void getFriendsActivity(String serverUserId, RepositoryCallback<List<String>> callback) {
        new Thread(() -> {
            try {
                // Proširen try blok da obuhvati i sam HTTP poziv
                JSONArray response = HttpHelper.getJSONArrayFromURL(baseUrl + "/friends-activity/" + serverUserId);

                if (response == null) {
                    mainHandler.post(() -> callback.onError("Nema konekcije. Friends nisu dostupni offline."));
                    return;
                }

                List<String> items = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    String username = obj.optString("username", "Unknown");
                    String eventName = obj.optString("eventName", "Unknown");
                    String commitment = obj.optString("commitment", "");

                    String tekst;
                    if (commitment.equals("PRISUSTVUJE")) {
                        tekst = username + " is also attending " + eventName;
                    } else {
                        tekst = username + " is also interested in " + eventName;
                    }
                    items.add(tekst);
                }
                mainHandler.post(() -> callback.onResult(items));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška pri dobavljanju aktivnosti prijatelja: " + e.getMessage()));
            }
        }).start();
    }

    // ===== POMOCNA METODA =====
    private List<Event> parseAttendanceResponse(JSONArray array, String filterCommitment) {
        List<Event> list = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String commitment = item.optString("commitment", "");
                if (!commitment.equals(filterCommitment)) continue;

                JSONObject eventObj = item.optJSONObject("event");
                if (eventObj == null) continue;

                String name = eventObj.optString("name", "");
                String desc = eventObj.optString("description", "");
                String loc = eventObj.optString("location", "");
                String cat = eventObj.optString("category", "");
                String timeStr = eventObj.optString("eventTime", "");
                boolean promoted = eventObj.optBoolean("promoted", false);
                int capacity = eventObj.optInt("capacity", 0);
                int attending = eventObj.optInt("numberOfAttendees", 0);
                double avgRating = eventObj.optDouble("avgRating", 0.0);
                int numRatings = eventObj.optInt("numberOfRatings", 0);

                Date date = null;
                try { date = sdf.parse(timeStr); } catch (Exception ignored) {}

                int img = EventFactory.getImageForCategory(cat);
                Event e = new Event(name, desc, loc, date, cat, img, promoted,
                        capacity, attending, avgRating, numRatings);
                list.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}