package marija.sekulic.eventsapp;

import android.content.Intent;
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
    Bundle bundle;
    Button btn_events, btn_myevents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events);

        // " Podrazumevano se pri pokretanju prikazuje EventsFragment" :
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_kontejner_fragmenata,new EventsFragment())
                .commit();



        btn_events = findViewById(R.id.btn_events);
        btn_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uzmi EventsFragment i stavi na mesto id_fragment_events:
                getSupportFragmentManager().beginTransaction().replace(R.id.id_kontejner_fragmenata,new EventsFragment())
                        .commit();

            }
        });
        btn_myevents = findViewById(R.id.btn_myevents);
        btn_myevents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // samo se instance objekata smenjuju, a kontejner je isti
                getSupportFragmentManager().beginTransaction().replace(R.id.id_kontejner_fragmenata, new MyEventsFragment())
                        .commit();
            }
        });


        subtitle = findViewById(R.id.id_events_activity_subtitle);
        bundle = getIntent().getExtras();   // ovo obavezno zapamtiti

        if (bundle!=null){  // ako nije prazan ako ima nesto unutra
            String username = bundle.getString("username_kljuc");
            subtitle.setText(username);
        }
        else{
            subtitle.setText("Error!");
        }



    }
}