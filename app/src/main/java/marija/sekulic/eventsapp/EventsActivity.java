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
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events);

        btn = findViewById(R.id.btn_events);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uzmi EventsFragment i stavi na mesto id_fragment_events:
                getSupportFragmentManager().beginTransaction().replace(R.id.id_fragment_events,new EventsFragment())
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