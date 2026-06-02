package marija.sekulic.eventsapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceRepository {

    private final AttendanceDAO attendanceDAO;
    private final EventDAO eventDAO;
    private final String baseUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AttendanceRepository(Context context) {
        this.attendanceDAO = new AttendanceDAO(context);
        this.eventDAO = new EventDAO(context);
        this.baseUrl = ServerConfig.BASE_URL;
    }

    // ===== SET INTERESTED / ATTENDING =====
    public void markAttendance(String serverUserId, String serverEventId,
                               int localUserId, int localEventId,
                               String commitment, RepositoryCallback<String> callback) {
        Log.d("ATTENDANCE_DEBUG", ">> POZVAN markAttendance za korisnika: " + serverUserId + " i dogadjaj: " + serverEventId);
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("userId", serverUserId);
                body.put("eventId", serverEventId);
                body.put("commitment", commitment);

                Log.d("ATTENDANCE_DEBUG", "Slanje POST zahteva na: " + baseUrl + "/attendance sa telom: " + body.toString());
                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/attendance", body);

                if (response == null) {
                    Log.e("ATTENDANCE_DEBUG", "POST ERROR: Server vratio NULL odgovor (Nema konekcije).");
                    mainHandler.post(() -> callback.onError("Nema konekcije. Akcija nije moguća offline."));
                    return;
                }

                int status = response.optInt("_httpStatus", 0);
                Log.d("ATTENDANCE_DEBUG", "POST odgovor primljen. HTTP Status: " + status);

                if (status == 400) {
                    String msg = response.optString("message", "Kapacitet je popunjen.");
                    Log.w("ATTENDANCE_DEBUG", "POST odbijen (400): " + msg);
                    mainHandler.post(() -> callback.onError(msg));
                    return;
                }

                if (status < 200 || status >= 300) {
                    Log.e("ATTENDANCE_DEBUG", "POST ERROR: Server vratio status van opsega 2xx: " + status);
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
                Log.d("ATTENDANCE_DEBUG", "POST USPEO. Upisano u lokalnu baju. Poruka: " + poruka);
                mainHandler.post(() -> callback.onResult(poruka));

            } catch (Exception e) {
                Log.e("ATTENDANCE_DEBUG", "CRASH u markAttendance: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // ===== GET ATTENDING EVENTS =====
    public void getAttendingEvents(String serverUserId, int localUserId,
                                   RepositoryCallback<List<Event>> callback) {
        Log.d("ATTENDANCE_DEBUG", ">> POZVAN getAttendingEvents. serverUserId=" + serverUserId + " | localUserId=" + localUserId);
        new Thread(() -> {
            try {
                String url = baseUrl + "/attendance/" + serverUserId;
                Log.d("ATTENDANCE_DEBUG", "Pokrećem mrežni zahtev na URL: " + url);
                JSONArray response = HttpHelper.getJSONArrayFromURL(url);

                if (response == null) {
                    Log.w("ATTENDANCE_DEBUG", "UPOZORENJE: Server vratio NULL za Attending! Prebacujem na lokalnu SQLite bazu.");
                    List<Event> local = attendanceDAO.getAttendingEvents(localUserId);
                    mainHandler.post(() -> callback.onResult(local));
                    return;
                }

                Log.d("ATTENDANCE_DEBUG", "Mreža vratila podatke. Sinhronizujem sa lokalnom bazom...");
                List<Event> finalResult = parseAndSyncAttendance(response, "PRISUSTVUJE", localUserId);

                Log.d("ATTENDANCE_DEBUG", "getAttendingEvents završava. Vraćam " + finalResult.size() + " događaja iz lokalne baze.");
                mainHandler.post(() -> callback.onResult(finalResult));

            } catch (Exception e) {
                Log.e("ATTENDANCE_DEBUG", "CRASH u getAttendingEvents! Poruka: " + e.getMessage(), e);
                List<Event> local = attendanceDAO.getAttendingEvents(localUserId);
                mainHandler.post(() -> callback.onResult(local));
            }
        }).start();
    }

    // ===== GET INTERESTED EVENTS =====
    public void getInterestedEvents(String serverUserId, int localUserId,
                                    RepositoryCallback<List<Event>> callback) {
        Log.d("ATTENDANCE_DEBUG", ">> POZVAN getInterestedEvents. serverUserId=" + serverUserId + " | localUserId=" + localUserId);
        new Thread(() -> {
            try {
                String url = baseUrl + "/attendance/" + serverUserId;
                Log.d("ATTENDANCE_DEBUG", "Pokrećem mrežni zahtev na URL: " + url);
                JSONArray response = HttpHelper.getJSONArrayFromURL(url);

                if (response == null) {
                    Log.w("ATTENDANCE_DEBUG", "UPOZORENJE: Server vratio NULL za Interested! Prebacujem na lokalnu SQLite bazu.");
                    List<Event> local = attendanceDAO.getInterestedEvents(localUserId);
                    mainHandler.post(() -> callback.onResult(local));
                    return;
                }

                Log.d("ATTENDANCE_DEBUG", "Mreža vratila podatke. Sinhronizujem sa lokalnom bazom...");
                List<Event> finalResult = parseAndSyncAttendance(response, "ZAINTERESOVAN", localUserId);

                Log.d("ATTENDANCE_DEBUG", "getInterestedEvents završava. Vraćam " + finalResult.size() + " događaja iz lokalne baze.");
                mainHandler.post(() -> callback.onResult(finalResult));

            } catch (Exception e) {
                Log.e("ATTENDANCE_DEBUG", "CRASH u getInterestedEvents! Poruka: " + e.getMessage(), e);
                List<Event> local = attendanceDAO.getInterestedEvents(localUserId);
                mainHandler.post(() -> callback.onResult(local));
            }
        }).start();
    }

    // ===== FRIENDS ACTIVITY =====
    public void getFriendsActivity(String serverUserId, RepositoryCallback<List<String>> callback) {
        new Thread(() -> {
            try {
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

                    String tekst = commitment.equals("PRISUSTVUJE")
                            ? username + " is also attending " + eventName
                            : username + " is also interested in " + eventName;
                    items.add(tekst);
                }
                mainHandler.post(() -> callback.onResult(items));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška pri dobavljanju aktivnosti prijatelja: " + e.getMessage()));
            }
        }).start();
    }

    // ===== NOVI POPRAVLJENI SINHRONIZATOR =====
    private List<Event> parseAndSyncAttendance(JSONArray array, String filterCommitment, int localUserId) {
        Log.d("ATTENDANCE_DEBUG", "[PARSER] Pokrećem obradu. Dužina niza: " + array.length() + " | Filter: " + filterCommitment);

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String commitment = item.optString("commitment", "");

                // Ako se status ne poklapa (npr. tražimo ZAINTERESOVAN, a stiglo PRISUSTVUJE), preskačemo
                if (!commitment.equalsIgnoreCase(filterCommitment)) {
                    continue;
                }

                // Čitamo flat polje "eventId" kako ga server zapravo šalje
                String mongoId = item.optString("eventId", "");
                if (mongoId.isEmpty()) {
                    Log.w("ATTENDANCE_DEBUG", "[PARSER] Stavka indeks [" + i + "] nema 'eventId'! Preskačem.");
                    continue;
                }

                // Pronalazimo lokalni ID u našoj SQLite bazi na osnovu MongoID-ja
                int localEventId = eventDAO.getLocalIdByMongoId(mongoId);
                Log.d("ATTENDANCE_DEBUG", "[PARSER] Za MongoID: " + mongoId + " nađen lokalni SQLite ID: " + localEventId);

                // Ako događaj postoji lokalno, ažuriramo tabelu prisustva u lokalnoj SQLite bazi
                if (localEventId > 0) {
                    if (filterCommitment.equalsIgnoreCase("ZAINTERESOVAN")) {
                        attendanceDAO.setInterested(localUserId, localEventId);
                        Log.d("ATTENDANCE_DEBUG", "[PARSER] Sinhronizovano: Događaj " + localEventId + " upisan u lokalne zainteresovane.");
                    } else if (filterCommitment.equalsIgnoreCase("PRISUSTVUJE")) {
                        attendanceDAO.setAttending(localUserId, localEventId);
                        Log.d("ATTENDANCE_DEBUG", "[PARSER] Sinhronizovano: Događaj " + localEventId + " upisan u lokalne dolazeće.");
                    }
                } else {
                    Log.e("ATTENDANCE_DEBUG", "[PARSER] Greška! Događaj sa MongoID " + mongoId + " ne postoji u lokalnoj SQLite bazi.");
                }
            }
        } catch (Exception e) {
            Log.e("ATTENDANCE_DEBUG", "[PARSER] KRIZNA GREŠKA tokom sinhronizacije JSON-a: ", e);
        }

        // Na samom kraju, kada smo sve ID-jeve unijeli u bazu, povlačimo čistu,
        // kompletnu listu sa svim podacima (slike, opisi) iz SQLite-a i nju šaljemo na UI ekran!
        if (filterCommitment.equalsIgnoreCase("ZAINTERESOVAN")) {
            return attendanceDAO.getInterestedEvents(localUserId);
        } else {
            return attendanceDAO.getAttendingEvents(localUserId);
        }
    }
}