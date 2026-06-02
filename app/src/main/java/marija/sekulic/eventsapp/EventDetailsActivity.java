package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
        Button btnRate = findViewById(R.id.btn_confirm_rating);

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

        // Provera da li smo u offline režimu
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean isOfflineMode = sp.getBoolean("is_offline_mode", false);

        // Blokada za prošle događaje
        if (trenutniEvent.isPast()) {
            btnInterested.setOnClickListener(v -> Toast.makeText(this, "Ovaj događaj je završen", Toast.LENGTH_SHORT).show());
            btnAttending.setOnClickListener(v -> Toast.makeText(this, "Ovaj događaj je završen", Toast.LENGTH_SHORT).show());

            if (btnRate != null) {
                btnRate.setOnClickListener(v -> {
                    if (isOfflineMode) {
                        Toast.makeText(this, "Ocenjivanje nije moguće u offline režimu!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(this, RatingActivity.class);
                    intent.putExtra("EVENT_ID", trenutniEvent.getId());
                    intent.putExtra("EVENT_NAME", trenutniEvent.getName());
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
            String serverEventId = trenutniEvent.getMongoId();

            AttendanceRepository attendanceRepo = new AttendanceRepository(this);

            btnInterested.setOnClickListener(v -> {
                // BLOKADA ZA OFFLINE MOD
                if (isOfflineMode) {
                    Toast.makeText(this, "Dodavanje u zainteresovane nije moguće u offline režimu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                attendanceRepo.markAttendance(
                        serverUserId, serverEventId,
                        finalLocalUserId, trenutniEvent.getId(),
                        "ZAINTERESOVAN",
                        new RepositoryCallback<String>() {
                            @Override
                            public void onResult(String result) {
                                Toast.makeText(EventDetailsActivity.this, result, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(EventDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            btnAttending.setOnClickListener(v -> {
                // BLOKADA ZA OFFLINE MOD
                if (isOfflineMode) {
                    Toast.makeText(this, "Prijava na događaj nije moguća u offline režimu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                attendanceRepo.markAttendance(
                        serverUserId, serverEventId,
                        finalLocalUserId, trenutniEvent.getId(),
                        "PRISUSTVUJE",
                        new RepositoryCallback<String>() {
                            @Override
                            public void onResult(String result) {
                                Toast.makeText(EventDetailsActivity.this, result, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(EventDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }
}