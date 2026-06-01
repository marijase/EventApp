    package marija.sekulic.eventsapp;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.TextView;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    public class EventsActivity extends AppCompatActivity {
        TextView subtitle;
        Button btn_events, btn_myevents, btn_friends;
        private EventDAO eventDAO;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //EdgeToEdge.enable(this);
            setContentView(R.layout.activity_events);


            // Inicijalizujemo EventDAO radnika za bazu podataka
            eventDAO = new EventDAO(this);

            // Puni bazu sirovim podacima iz AppData ako je prazna (samo prvi put)
            EventRepository eventRepository = new EventRepository(this);
            eventRepository.getAllEvents(new RepositoryCallback<java.util.List<Event>>() {
                @Override
                public void onResult(java.util.List<Event> result) {
                    if (result.isEmpty()) {
                        for (Event e : AppData.allEvents) {
                            eventRepository.createEvent(e, new RepositoryCallback<Boolean>() {
                                @Override public void onResult(Boolean r) {}
                                @Override public void onError(String err) {}
                            });
                        }
                    }
                    // osvezi fragment nakon sto su eventi kesirani
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.id_kontejner_fragmenata, new EventsFragment())
                            .commit();
                }
                @Override
                public void onError(String errorMessage) {
                    eventDAO.populateIfEmpty(AppData.allEvents);
                }
            });


            // " Podrazumevano se pri pokretanju prikazuje EventsFragment" :
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.id_kontejner_fragmenata,new EventsFragment())
                    .commit();



            btn_events = findViewById(R.id.btn_events);
            btn_events.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // uzmi EventsFragment i stavi na mesto id_fragment_events:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.id_kontejner_fragmenata,new EventsFragment())
                            .commit();

                }
            });

            btn_myevents = findViewById(R.id.btn_myevents);
            btn_myevents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // samo se instance objekata smenjuju, a kontejner je isti
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.id_kontejner_fragmenata, new MyEventsFragment())
                            .commit();
                }
            });

            btn_friends = findViewById(R.id.btn_friends);
            btn_friends.setOnClickListener(v -> {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_kontejner_fragmenata, new FriendsFragment())
                        .commit();
            });



            subtitle = findViewById(R.id.id_events_activity_subtitle);

            // --- IZMENA: citanje iz SharedPreferences umesto Bundle-a ---
            SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
            String username = sp.getString("current_user", null);

            if (username != null) {
                subtitle.setText(username);
            } else {
                subtitle.setText("Guest"); // Ako niko nije ulogovan, piše Guest
            }

        }
    }