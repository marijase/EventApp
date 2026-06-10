package marija.sekulic.eventsapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ExclusiveEventService extends Service {

    private static final String TAG = "ExclusiveEventService";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servis je pokrenuo exclusive event kreiranje");

        new Thread(() -> {
            try {
                // 1. Kreiramo random exclusive event
                Event exclusiveEvent = generateRandomExclusiveEvent();

                // 2. Pošaljemo na server
                createExclusiveEventOnServer(exclusiveEvent);

            } catch (Exception e) {
                Log.e(TAG, "Greška pri kreiranju exclusive eventa: " + e.getMessage());
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Event generateRandomExclusiveEvent() {
        String[] names = {
                "PREMIUM Flash Sale Concert",
                "PREMIUM Limited Access Party",
                "PREMIUM Exclusive DJ Night",
                "PREMIUM VIP Festival Preview",
                "PREMIUM Secret Show Announcement"
        };

        String[] locations = {
                "Novi Sad",
                "Beograd",
                "Niš",
                "Kragujevac",
                "Subotica"
        };

        String[] categories = {
                "CONCERT",
                "PARTY",
                "FESTIVAL"
        };

        Random rand = new Random();
        String name = names[rand.nextInt(names.length)];
        String location = locations[rand.nextInt(locations.length)];
        String category = categories[rand.nextInt(categories.length)];
        String description = "Ekskluzivni događaj dostupan samo 5 minuta!";

        // Vreme: sada + 60 sekundi (za testiranje, profesionalno bi bilo sada)
        Date eventTime = new Date(System.currentTimeMillis() + 2*60000);

        Event event = new Event(name, description, location, eventTime, category,
                EventFactory.getImageForCategory(category), false, 0, 0, 0.0, 0);

        return event;
    }

    private void createExclusiveEventOnServer(Event event) {
        try {
            JSONObject body = new JSONObject();
            body.put("name", event.getName());
            body.put("description", event.getDescription());
            body.put("location", event.getLocation());
            body.put("eventTime", event.getDateTime() != null ? sdf.format(event.getDateTime()) : "");
            body.put("category", event.getCategory());
            body.put("promoted", false);
            body.put("capacity", 0);
            // Server može dodati custom polje za exclusive ako želi, ali nije obavezno
            body.put("exclusive", true);

            JSONObject response = HttpHelper.postJSONObjectFromURL(
                    ServerConfig.BASE_URL + "/events", body);

            if (response == null) {
                Log.e(TAG, "Server nije odgovorio");
                return;
            }

            int status = response.optInt("_httpStatus", 0);
            if (status < 200 || status >= 300) {
                Log.e(TAG, "Server greška: " + status);
                return;
            }

            // 3. Čitamo _id koji je server vratio
            String mongoId = response.optString("_id", "");
            Log.d(TAG, "Ekskluzivni event kreiran sa ID: " + mongoId);

            // 4. Čuvamo vreme isteka (sada + 5 minuta) u SharedPreferences
            long expireTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minuta
            SharedPreferences sp = getSharedPreferences("ExclusiveEvents", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("exclusive_" + mongoId + "_expireTime", expireTime);
            editor.putString("exclusive_" + mongoId + "_name", event.getName());
            editor.apply();

            Log.d(TAG, "Vreme isteka čuvano u SharedPreferences");

            // 5. Šaljemo notifikaciju
            NotificationHelper.sendExclusiveEventNotification(this, mongoId, event.getName());

        } catch (Exception e) {
            Log.e(TAG, "Greška pri POST-u: " + e.getMessage());
        }
    }
}