package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AttendingEventsActivity extends AppCompatActivity {

    private AttendanceRepository attendanceRepo;
    private EventDAO eventDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attending_events);

        Log.d("ATTENDANCE_DEBUG", "AttendingEventsActivity: onCreate pokrenut");
        attendanceRepo = new AttendanceRepository(this);
        eventDAO = new EventDAO(this);
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

        // POPRAVLJENO: Sada vuče tačan ključ iz tvog LoginActivity-ja!
        String serverUserId = sp.getString("current_server_id", "");

        UserDAO userDAO = new UserDAO(this);
        int localUserId = userDAO.getUserIdByUsername(currentUsername);

        Log.d("ATTENDANCE_DEBUG", "Activity -> Pozivam repo za Attending. serverUserId: " + serverUserId + " | localUserId: " + localUserId);

        attendanceRepo.getAttendingEvents(serverUserId, localUserId, new RepositoryCallback<List<Event>>() {
            @Override
            public void onResult(List<Event> upcoming) {
                Log.d("ATTENDANCE_DEBUG", "Activity -> Attending onResult primio " + (upcoming != null ? upcoming.size() : 0) + " dolazećih događaja.");

                if (upcoming == null) upcoming = new ArrayList<>();

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
                    rvUpcoming.setVisibility(View.VISIBLE);
                    findViewById(R.id.header_upcoming).setVisibility(View.VISIBLE);
                    rvUpcoming.setLayoutManager(new LinearLayoutManager(AttendingEventsActivity.this));
                    EventAdapter upcomingAdapter = new EventAdapter(AttendingEventsActivity.this::openDetails, false);
                    upcomingAdapter.setEvents(upcoming);
                    rvUpcoming.setAdapter(upcomingAdapter);
                } else {
                    findViewById(R.id.header_upcoming).setVisibility(View.GONE);
                    rvUpcoming.setVisibility(View.GONE);
                }

                if (!past.isEmpty()) {
                    rvPast.setVisibility(View.VISIBLE);
                    findViewById(R.id.header_past).setVisibility(View.VISIBLE);
                    rvPast.setLayoutManager(new LinearLayoutManager(AttendingEventsActivity.this));
                    EventAdapter pastAdapter = new EventAdapter(AttendingEventsActivity.this::openDetails, true);
                    pastAdapter.setEvents(past);
                    rvPast.setAdapter(pastAdapter);
                } else {
                    findViewById(R.id.header_past).setVisibility(View.GONE);
                    rvPast.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ATTENDANCE_DEBUG", "Activity -> Attending onError: " + error);
                txtEmpty.setVisibility(View.VISIBLE);
                rvUpcoming.setVisibility(View.GONE);
                rvPast.setVisibility(View.GONE);
            }
        });
    }

    private void openDetails(Event e) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", e.getId());
        startActivity(intent);
    }
}