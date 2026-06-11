package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        final Event event = findEvent();

        if (event == null) {
            Toast.makeText(this, "Event nije pronađen", Toast.LENGTH_SHORT).show();
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
        Button btnRate = findViewById(R.id.btn_confirm_rating);

        imgEvent.setImageResource(event.getImageResId());
        txtName.setText(event.getName());
        txtDesc.setText(event.getDescription());
        txtLoc.setText(event.getLocation());

        if (event.getDateTime() != null) {
            txtDate.setText(sdf.format(event.getDateTime()));
        } else {
            txtDate.setText("Datum nije dostupan");
        }

        if (event.isPromoted()) {
            txtCapacity.setVisibility(View.VISIBLE);
            txtCapacity.setText("Slobodnih mesta: "
                    + (event.getCapacity() - event.getAttendingCount())
                    + "/" + event.getCapacity());
        } else {
            txtCapacity.setVisibility(View.GONE);
        }

        if (event.getRatingCount() == 0) {
            txtRating.setText("Još uvek nema ocena");
        } else {
            txtRating.setText(String.format("Prosečna ocena: %.1f (%d ocena)",
                    event.getAverageRating(), event.getRatingCount()));
        }

        // Provjeri exclusive prozor za SVE evente (ne samo one iz notifikacije)
        if (event.getMongoId() != null) {
            SharedPreferences exclusiveSp = getSharedPreferences("ExclusiveEvents", MODE_PRIVATE);
            long expireTime = exclusiveSp.getLong("exclusive_" + event.getMongoId() + "_expireTime", 0);

            if (expireTime > 0) {
                // Ovo je exclusive event
                if (System.currentTimeMillis() > expireTime) {
                    // Prozor je zatvoren
                    btnAttending.setEnabled(false);
                    btnAttending.setText("Prozor zatvoren");
                    Log.d(TAG, "Exclusive event - prozor zatvoren");
                } else {
                    // Prozor je otvoren
                    Log.d(TAG, "Exclusive event - prozor otvoren, ostalo: "
                            + (expireTime - System.currentTimeMillis()) / 1000 + "s");
                }
            }
        }

        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean isOfflineMode = sp.getBoolean("is_offline_mode", false);

        if (event.isPast()) {
            btnInterested.setOnClickListener(v ->
                    Toast.makeText(this, "Ovaj događaj je završen", Toast.LENGTH_SHORT).show());
            btnAttending.setOnClickListener(v ->
                    Toast.makeText(this, "Ovaj događaj je završen", Toast.LENGTH_SHORT).show());

            if (btnRate != null) {
                btnRate.setOnClickListener(v -> {
                    if (isOfflineMode) {
                        Toast.makeText(this, "Ocenjivanje nije moguće u offline režimu!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(this, RatingActivity.class);
                    intent.putExtra("EVENT_ID", event.getId());
                    intent.putExtra("EVENT_NAME", event.getName());
                    startActivity(intent);
                });
            }
        } else {
            String serverUserId = sp.getString("current_server_id", "");
            String currentUsername = sp.getString("current_user", "");

            UserDAO userDAO = new UserDAO(this);
            int localUserId = userDAO.getUserIdByUsername(currentUsername);

            if (localUserId <= 0) {
                SQLiteDatabase db = new DatabaseHelper(this).getWritableDatabase();
                ContentValues uValues = new ContentValues();
                uValues.put("username", currentUsername);
                uValues.put("email", currentUsername + "@gmail.com");
                uValues.put("password", "placeholder_pass");
                long newId = db.insert("users", null, uValues);
                db.close();
                localUserId = (int) newId;
            }

            final int finalLocalUserId = localUserId;
            final String serverEventId = event.getMongoId();
            AttendanceRepository attendanceRepo = new AttendanceRepository(this);

            btnInterested.setOnClickListener(v -> {
                if (isOfflineMode) {
                    Toast.makeText(this, "Nije moguće u offline režimu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                attendanceRepo.markAttendance(serverUserId, serverEventId,
                        finalLocalUserId, event.getId(), "ZAINTERESOVAN",
                        new RepositoryCallback<String>() {
                            @Override public void onResult(String result) {
                                Toast.makeText(EventDetailsActivity.this, result, Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String errorMessage) {
                                Toast.makeText(EventDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            btnAttending.setOnClickListener(v -> {
                if (isOfflineMode) {
                    Toast.makeText(this, "Nije moguće u offline režimu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proveri exclusive prozor SVAKI PUT kad se klikne dugme
                if (event.getMongoId() != null) {
                    SharedPreferences exclusiveSp = getSharedPreferences("ExclusiveEvents", MODE_PRIVATE);
                    long expireTime = exclusiveSp.getLong("exclusive_" + event.getMongoId() + "_expireTime", 0);
                    if (expireTime > 0 && System.currentTimeMillis() > expireTime) {
                        Toast.makeText(this, "Prozor za prijavu je zatvoren!", Toast.LENGTH_SHORT).show();
                        btnAttending.setEnabled(false);
                        btnAttending.setText("Prozor zatvoren");
                        return;
                    }
                }

                attendanceRepo.markAttendance(serverUserId, serverEventId,
                        finalLocalUserId, event.getId(), "PRISUSTVUJE",
                        new RepositoryCallback<String>() {
                            @Override public void onResult(String result) {
                                Toast.makeText(EventDetailsActivity.this, result, Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String errorMessage) {
                                Toast.makeText(EventDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }

    private Event findEvent() {
        EventDAO eventDAO = new EventDAO(this);

        String exclusiveMongoId = getIntent().getStringExtra("EXCLUSIVE_EVENT_ID");
        if (exclusiveMongoId != null) {
            Log.d(TAG, "Otvaram exclusive event mongoId=" + exclusiveMongoId);
            Event e = eventDAO.getEventByMongoId(exclusiveMongoId);
            if (e == null) {
                Log.e(TAG, "Exclusive event nije pronađen u bazi!");
            }
            return e;
        }

        int eventId = getIntent().getIntExtra("EVENT_ID", -1);
        Log.d(TAG, "Otvaram event id=" + eventId);
        if (eventId == -1) return null;
        return eventDAO.getEventById(eventId);
    }
}