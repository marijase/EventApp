package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    private AttendanceRepository attendanceRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_events);

        Log.d("ATTENDANCE_DEBUG", "InterestedEventsActivity: onCreate pokrenut");

        rv = findViewById(R.id.rv_interested_events);
        txtEmpty = findViewById(R.id.id_interested_events_empty);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(event -> {
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });

        rv.setAdapter(adapter);

        attendanceRepo = new AttendanceRepository(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String currentUsername = sp.getString("current_user", "");

        // POPRAVLJENO: Sada vuče tačan ključ iz tvog LoginActivity-ja!
        String serverUserId = sp.getString("current_server_id", "");

        UserDAO userDAO = new UserDAO(this);
        int localUserId = userDAO.getUserIdByUsername(currentUsername);

        Log.d("ATTENDANCE_DEBUG", "Activity -> Pozivam repo za Interested. serverUserId: " + serverUserId + " | localUserId: " + localUserId);

        attendanceRepo.getInterestedEvents(serverUserId, localUserId, new RepositoryCallback<List<Event>>() {
            @Override
            public void onResult(List<Event> interestedEvents) {
                Log.d("ATTENDANCE_DEBUG", "Activity -> callback onResult primio listu od " + (interestedEvents != null ? interestedEvents.size() : "null") + " elemenata.");

                if (interestedEvents == null || interestedEvents.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    adapter.setEvents(interestedEvents);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ATTENDANCE_DEBUG", "Activity -> callback onError: " + error);
                txtEmpty.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            }
        });
    }
}