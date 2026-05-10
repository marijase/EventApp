package marija.sekulic.eventsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AttendingEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attending_events);

        TextView txtEmpty = findViewById(R.id.txt_attending_empty);
        RecyclerView sectionUpcoming = findViewById(R.id.rv_upcoming);
        RecyclerView sectionPast = findViewById(R.id.rv_past);

        if (AppData.attendingEvents.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            sectionUpcoming.setVisibility(View.GONE);
            sectionPast.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            setupRecyclerViews();
        }
    }

    private void setupRecyclerViews() {
        List<Event> upcoming = new ArrayList<>();
        List<Event> past = new ArrayList<>();

        for (Event e : AppData.attendingEvents) {
            if (e.isPast()) past.add(e);
            else upcoming.add(e);
        }

        // Predstojeći
        if (!upcoming.isEmpty()) {
            RecyclerView rv = findViewById(R.id.rv_upcoming);
            rv.setLayoutManager(new LinearLayoutManager(this));
            EventAdapter adapter = new EventAdapter(this::openDetails);
            adapter.setEvents(upcoming);
            rv.setAdapter(adapter);
        } else {
            findViewById(R.id.header_upcoming).setVisibility(View.GONE);
        }

        // Prošli - Sa dugmetom OCENI
        if (!past.isEmpty()) {
            RecyclerView rv = findViewById(R.id.rv_past);
            rv.setLayoutManager(new LinearLayoutManager(this));
            // Koristi poseban adapter ili modifikuj postojeći za dugme "OCENI"
            EventAdapter adapter = new EventAdapter(this::openRating);
            adapter.setEvents(past);
            rv.setAdapter(adapter);
        } else {
            findViewById(R.id.header_past).setVisibility(View.GONE);
        }
    }

    private void openDetails(Event e) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_NAME", e.getName());
        startActivity(intent);
    }

    private void openRating(Event e) {
        Intent intent = new Intent(this, RatingActivity.class);
        intent.putExtra("EVENT_NAME", e.getName());
        startActivity(intent);
    }
}