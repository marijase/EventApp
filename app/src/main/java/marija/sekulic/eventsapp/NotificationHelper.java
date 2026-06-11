package marija.sekulic.eventsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "exclusive_events_channel";

    // Pozovi ovo iz Application klase ili MainActivity.onCreate()
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Premium Events",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikacije za premium događaje");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    // Notifikacija 1: Novi exclusive event kreiran, ima 5 min za prijavu
    // Klik otvara EventDetailsActivity sa mongoId
    public static void sendExclusiveEventNotification(Context context, String eventName, String mongoId) {
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("EXCLUSIVE_EVENT_ID", mongoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🎉 Novi Premium Event!")
                .setContentText(eventName + " – imate 5 minuta da se prijavite!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1001, builder.build());
    }

    // Notifikacija 2: Prozor je zatvoren
    // Klik nije potreban (nema PendingIntent)
    public static void sendExclusiveEventExpiredNotification(Context context, String eventName, long nextOpportunityMs) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        String nextTime = sdf.format(new java.util.Date(nextOpportunityMs));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("⏰ Prozor za prijavu je zatvoren")
                .setContentText(eventName + " – sledeća prilika: " + nextTime)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1002, builder.build());
    }
}