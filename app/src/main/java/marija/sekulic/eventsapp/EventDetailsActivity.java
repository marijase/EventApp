package marija.sekulic.eventsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Preuzimanje naziva iz Intent-a [cite: 102]
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        Event event = AppData.findByName(eventName);

        if (event == null) {
            finish();
            return;
        }

        // Povezivanje UI elemenata [cite: 95]
        ImageView imgEvent = findViewById(R.id.img_details);
        TextView txtName = findViewById(R.id.txt_details_name);
        TextView txtDesc = findViewById(R.id.txt_details_desc);
        TextView txtLoc = findViewById(R.id.txt_details_loc);
        TextView txtDate = findViewById(R.id.txt_details_date);
        TextView txtCapacity = findViewById(R.id.txt_details_capacity);
        TextView txtRating = findViewById(R.id.txt_details_rating);
        Button btnInterested = findViewById(R.id.btn_interested);
        Button btnAttending = findViewById(R.id.btn_attending);

        // Popunjavanje podataka
        imgEvent.setImageResource(event.getImageResId());
        txtName.setText(event.getName());
        txtDesc.setText(event.getDescription());
        txtLoc.setText(event.getLocation());
        if (event.getDateTime() != null) {
            txtDate.setText(sdf.format(event.getDateTime()));
        } else {
            txtDate.setText("Datum nije dostupan");
        }

        // Prikaz kapaciteta samo za promoted [cite: 96]
        if (event.isPromoted()) {
            txtCapacity.setVisibility(View.VISIBLE);
            txtCapacity.setText("Slobodnih mesta: " + (event.getCapacity() - event.getAttendingCount()) + "/" + event.getCapacity());
        } else {
            txtCapacity.setVisibility(View.GONE);
        }

        // Prikaz ocene [cite: 98]
        if (event.getRatingCount() == 0) {
            txtRating.setText("Još uvek nema ocena");
        } else {
            txtRating.setText(String.format("Prosečna ocena: %.1f (%d ocena)", event.getAverageRating(), event.getRatingCount()));
        }

        // Dugmad [cite: 99, 100, 101]
        btnInterested.setOnClickListener(v -> {
            if (!AppData.interestedEvents.contains(event)) {
                AppData.interestedEvents.add(event);
            }
            Toast.makeText(this, "Dodat u zainteresovane.", Toast.LENGTH_SHORT).show();
        });

        btnAttending.setOnClickListener(v -> {
            if (!AppData.attendingEvents.contains(event)) {
                AppData.attendingEvents.add(event);
            }
            Toast.makeText(this, "Prijavili ste se na događaj.", Toast.LENGTH_SHORT).show();
        });
    }
}