package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EventsActivity extends AppCompatActivity {
    TextView subtitle;
    Button btn_events, btn_myevents, btn_friends;
    private EventDAO eventDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        eventDAO = new EventDAO(this);

        EventRepository eventRepository = new EventRepository(this);
        eventRepository.getAllEvents(new RepositoryCallback<java.util.List<Event>>() {
            @Override
            public void onResult(java.util.List<Event> result) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_kontejner_fragmenata, new EventsFragment())
                        .commit();
            }
            @Override
            public void onError(String errorMessage) {
                eventDAO.populateIfEmpty(AppData.allEvents);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_kontejner_fragmenata, new EventsFragment())
                        .commit();
            }
        });

        btn_events = findViewById(R.id.btn_events);
        btn_events.setOnClickListener(v ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_kontejner_fragmenata, new EventsFragment())
                        .commit());

        btn_myevents = findViewById(R.id.btn_myevents);
        btn_myevents.setOnClickListener(v ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_kontejner_fragmenata, new MyEventsFragment())
                        .commit());

        btn_friends = findViewById(R.id.btn_friends);
        btn_friends.setOnClickListener(v ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_kontejner_fragmenata, new FriendsFragment())
                        .commit());

        subtitle = findViewById(R.id.id_events_activity_subtitle);
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String username = sp.getString("current_user", null);
        subtitle.setText(username != null ? username : "Guest");

        // Pokreni servis svaki put kad se uloguje - AlarmManager ce spreciti duplikate
        android.util.Log.d("EventsActivity", "Pokrecem ExclusiveEventService");
        startService(new Intent(this, ExclusiveEventService.class));
    }
}