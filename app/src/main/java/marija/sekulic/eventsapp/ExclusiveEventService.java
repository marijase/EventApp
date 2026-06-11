package marija.sekulic.eventsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ExclusiveEventService extends Service {

    private static final String TAG = "ExclusiveEventService";
    // ZA TESTIRANJE: 2 minute. Za produkciju: 24 * 60 * 60 * 1000L
    private static final long INTERVAL_MS = 2 * 60 * 1000L;
    // Prozor za prijavu: 5 minuta
    private static final long WINDOW_MS = 5 * 60 * 1000L;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand pokrenut");
        new Thread(this::createAndNotify).start();
        scheduleNext();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void scheduleNext() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ExclusiveEventService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long triggerAt = System.currentTimeMillis() + INTERVAL_MS;
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        Log.d(TAG, "Sledece pokretanje: " + new Date(triggerAt));
    }

    private void createAndNotify() {
        try {
            // 20 razlicitih naziva
            String[] names = {
                    "PREMIUM Neon Rave Party",
                    "PREMIUM Secret Rooftop Concert",
                    "PREMIUM Exclusive Jazz Evening",
                    "PREMIUM VIP Electronic Night",
                    "PREMIUM Underground Hip-Hop Show",
                    "PREMIUM Sunset Beach Festival",
                    "PREMIUM Acoustic Live Session",
                    "PREMIUM Techno Warehouse Party",
                    "PREMIUM Classical Gala Concert",
                    "PREMIUM Indie Rock Showcase",
                    "PREMIUM Latin Dance Night",
                    "PREMIUM Open Air Cinema Special",
                    "PREMIUM Comedy Club Exclusive",
                    "PREMIUM Pop-Up Art & Music",
                    "PREMIUM Silent Disco Event",
                    "PREMIUM Drum & Bass Night",
                    "PREMIUM Vintage Soul Evening",
                    "PREMIUM Balkan Beats Party",
                    "PREMIUM Midnight Jazz Session",
                    "PREMIUM Summer Closing Festival"
            };

            String[] locations = {
                    "Novi Sad", "Beograd", "Niš", "Subotica", "Kragujevac",
                    "Zrenjanin", "Pančevo", "Čačak", "Valjevo", "Zlatibor"
            };

            String[] categories = { "CONCERT", "PARTY", "FESTIVAL" };

            String[] descriptions = {
                    "Ekskluzivni premium događaj – samo 5 minuta za prijavu!",
                    "Jedinstvena prilika – prozor za prijavu traje samo 5 minuta!",
                    "VIP iskustvo koje ne smeš propustiti – prijavi se za 5 minuta!",
                    "Limitirani pristup – obezbedi mesto za 5 minuta!",
                    "Poseban premium događaj – vreme za prijavu je ograničeno na 5 minuta!"
            };

            Random rand = new Random();
            String name = names[rand.nextInt(names.length)];
            String location = locations[rand.nextInt(locations.length)];
            String category = categories[rand.nextInt(categories.length)];
            String description = descriptions[rand.nextInt(descriptions.length)];
            Date eventTime = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // za 1h

            // POST na server
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("description", description);
            body.put("location", location);
            body.put("eventTime", sdf.format(eventTime));
            body.put("category", category);
            body.put("promoted", false);
            body.put("capacity", 0);
            body.put("exclusive", true);

            Log.d(TAG, "Saljem POST: " + body.toString());
            JSONObject response = HttpHelper.postJSONObjectFromURL(
                    ServerConfig.BASE_URL + "/events", body);

            if (response == null) {
                Log.e(TAG, "Server ne odgovara");
                return;
            }

            int status = response.optInt("_httpStatus", 0);
            Log.d(TAG, "Server status: " + status);
            if (status < 200 || status >= 300) {
                Log.e(TAG, "Server greška: " + status);
                return;
            }

            String mongoId = response.optString("_id", "");
            if (mongoId.isEmpty()) {
                Log.e(TAG, "Nema _id u odgovoru!");
                return;
            }
            Log.d(TAG, "Event kreiran, mongoId=" + mongoId);

            // Upiši u lokalnu SQLite bazu
            Event localEvent = new Event(name, description, location, eventTime, category,
                    EventFactory.getImageForCategory(category),
                    false, 0, 0, 0.0, 0);
            localEvent.setMongoId(mongoId);

            EventDAO eventDAO = new EventDAO(this);
            boolean saved = eventDAO.createEventByMongoId(localEvent);
            Log.d(TAG, "Upisan u lokalnu bazu: " + saved);

            // Sačuvaj expire time
            long expireTime = System.currentTimeMillis() + WINDOW_MS;
            long nextOpportunity = System.currentTimeMillis() + INTERVAL_MS;

            SharedPreferences sp = getSharedPreferences("ExclusiveEvents", MODE_PRIVATE);
            sp.edit()
                    .putLong("exclusive_" + mongoId + "_expireTime", expireTime)
                    .putString("exclusive_" + mongoId + "_name", name)
                    .apply();

            // Notifikacija 1: novi event
            NotificationHelper.sendExclusiveEventNotification(this, name, mongoId);
            Log.d(TAG, "Notifikacija 1 poslata");

            // Notifikacija 2: posle 5 minuta - prozor zatvoren
            final String finalName = name;
            final long finalNext = nextOpportunity;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                NotificationHelper.sendExclusiveEventExpiredNotification(
                        ExclusiveEventService.this, finalName, finalNext);
                Log.d(TAG, "Notifikacija 2 poslata");
            }, WINDOW_MS);

        } catch (Exception e) {
            Log.e(TAG, "Greška: " + e.getMessage(), e);
        }
    }
}