package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InterestedEventsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView txtEmpty;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_events);

        rv = findViewById(R.id.rv_interested_events);
        txtEmpty = findViewById(R.id.id_interested_events_empty);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(event -> {
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });

        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String currentUsername = sp.getString("current_user", "");
        UserDAO userDAO = new UserDAO(this);
        int userId = userDAO.getUserIdByUsername(currentUsername);

        AttendanceDAO attendanceDAO = new AttendanceDAO(this);
        List<Event> interestedEvents = attendanceDAO.getInterestedEvents(userId);

        if (interestedEvents == null || interestedEvents.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            adapter.setEvents(interestedEvents);
        }
    }
}