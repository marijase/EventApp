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

        //String eventName = getIntent().getStringExtra("EVENT_NAME");
        //Event event = AppData.findByName(eventName);
        int eventId = getIntent().getIntExtra("EVENT_ID", -1);
        EventDAO eventDAO = new EventDAO(this);
        Event trenutniEvent = eventDAO.getEventById(eventId);

        if (trenutniEvent == null) {
            finish();
            return;
        }

        ImageView imgEvent = findViewById(R.id.img_details);
        TextView txtName = findViewById(R.id.txt_details_name);
        TextView txtDesc = findViewById(R.id.txt_details_desc);
        TextView txtLoc = findViewById(R.id.txt_details_loc);
        TextView txtDate = findViewById(R.id.txt_details_date);
        TextView txtCapacity = findViewById(R.id.txt_details_capacity);
        TextView txtRating = findViewById(R.id.txt_details_rating);
        Button btnInterested = findViewById(R.id.btn_interested);
        Button btnAttending = findViewById(R.id.btn_attending);

        imgEvent.setImageResource(trenutniEvent.getImageResId());
        txtName.setText(trenutniEvent.getName());
        txtDesc.setText(trenutniEvent.getDescription());
        txtLoc.setText(trenutniEvent.getLocation());
        if (trenutniEvent.getDateTime() != null) {
            txtDate.setText(sdf.format(trenutniEvent.getDateTime()));
        } else {
            txtDate.setText("Datum nije dostupan");
        }

        if (trenutniEvent.isPromoted()) {
            txtCapacity.setVisibility(View.VISIBLE);
            txtCapacity.setText("Slobodnih mesta: " + (trenutniEvent.getCapacity() - trenutniEvent.getAttendingCount()) + "/" + trenutniEvent.getCapacity());
        } else {
            txtCapacity.setVisibility(View.GONE);
        }

        if (trenutniEvent.getRatingCount() == 0) {
            txtRating.setText("Još uvek nema ocena");
        } else {
            txtRating.setText(String.format("Prosečna ocena: %.1f (%d ocena)", trenutniEvent.getAverageRating(), trenutniEvent.getRatingCount()));
        }

        // blokada za prosle dogadjaje
        if (trenutniEvent.isPast()) {

            btnInterested.setOnClickListener(v ->
                    Toast.makeText(this, "Ovaj događaj je završen", Toast.LENGTH_SHORT).show()
            );

            btnAttending.setOnClickListener(v ->
                    Toast.makeText(this, "Ovaj događaj je završen", Toast.LENGTH_SHORT).show()
            );

        } else {

            btnInterested.setOnClickListener(v -> {
                if (!AppData.interestedEvents.contains(trenutniEvent)) {
                    AppData.interestedEvents.add(trenutniEvent);
                }
                Toast.makeText(this, "Dodat u zainteresovane.", Toast.LENGTH_SHORT).show();
            });

            btnAttending.setOnClickListener(v -> {
                if (!AppData.attendingEvents.contains(trenutniEvent)) {
                    AppData.attendingEvents.add(trenutniEvent);
                }
                Toast.makeText(this, "Prijavili ste se na događaj.", Toast.LENGTH_SHORT).show();
            });
        }
    }
}