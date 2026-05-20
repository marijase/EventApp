package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUI();
    }

    private void setupUI() {
        TextView txtEmpty = findViewById(R.id.txt_attending_empty);
        RecyclerView rvUpcoming = findViewById(R.id.rv_upcoming);
        RecyclerView rvPast = findViewById(R.id.rv_past);

        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String currentUsername = sp.getString("current_user", "");
        UserDAO userDAO = new UserDAO(this);
        int userId = userDAO.getUserIdByUsername(currentUsername);

        AttendanceDAO attendanceDAO = new AttendanceDAO(this);
        EventDAO eventDAO = new EventDAO(this);

        List<Event> upcoming = attendanceDAO.getAttendingEvents(userId);

        // svi prošli eventi iz baze automatski idu u past sekciju
        List<Event> sviEventi = eventDAO.getAllEvents();
        List<Event> past = new ArrayList<>();
        for (Event e : sviEventi) {
            if (e.isPast()) past.add(e);
        }

        boolean imaIcega = !upcoming.isEmpty() || !past.isEmpty();

        if (!imaIcega) {
            txtEmpty.setVisibility(View.VISIBLE);
            rvUpcoming.setVisibility(View.GONE);
            rvPast.setVisibility(View.GONE);
            return;
        }

        txtEmpty.setVisibility(View.GONE);

        if (!upcoming.isEmpty()) {
            rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
            EventAdapter adapter = new EventAdapter(this::openDetails, false);
            adapter.setEvents(upcoming);
            rvUpcoming.setAdapter(adapter);
        } else {
            findViewById(R.id.header_upcoming).setVisibility(View.GONE);
            rvUpcoming.setVisibility(View.GONE);
        }

        if (!past.isEmpty()) {
            rvPast.setLayoutManager(new LinearLayoutManager(this));
            EventAdapter adapter = new EventAdapter(this::openDetails, true);
            adapter.setEvents(past);
            rvPast.setAdapter(adapter);
        } else {
            findViewById(R.id.header_past).setVisibility(View.GONE);
            rvPast.setVisibility(View.GONE);
        }
    }

    private void openDetails(Event e) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", e.getId()); // ispravljeno sa EVENT_NAME na EVENT_ID
        startActivity(intent);
    }
}