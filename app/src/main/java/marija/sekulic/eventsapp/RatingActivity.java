package marija.sekulic.eventsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RatingActivity extends AppCompatActivity {

    private TextView txtEventName;
    private ImageView[] stars = new ImageView[5];
    private int currentRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        txtEventName = findViewById(R.id.txt_rating_event_name);
        Button btnConfirm = findViewById(R.id.btn_confirm_rating);

        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        // Hvatamo i ID i Ime koje nam je adapter poslao
        int eventId = getIntent().getIntExtra("EVENT_ID", -1);

        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            txtEventName.setText(eventName);
        }

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> updateStars(index + 1));
        }

        btnConfirm.setOnClickListener(v -> {
            // Provera da li je korisnik uopšte izabrao neku zvezdicu
            if (currentRating == 0) {
                Toast.makeText(this, "Molimo vas izaberite ocenu od 1 do 5!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
            String serverUserId = sp.getString("current_server_id", "");
            String currentUsername = sp.getString("current_user", "");

            UserDAO userDAO = new UserDAO(this);
            int localUserId = userDAO.getUserIdByUsername(currentUsername);

            String serverEventId = String.valueOf(eventId); // fallback

            RatingRepository ratingRepo = new RatingRepository(this);
            btnConfirm.setEnabled(false);

            ratingRepo.leaveRating(serverUserId, serverEventId, localUserId, eventId, currentRating,
                    new RepositoryCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            Toast.makeText(RatingActivity.this, "Hvala na oceni!", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            btnConfirm.setEnabled(true);
                            Toast.makeText(RatingActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void updateStars(int rating) {
        currentRating = rating;
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(android.R.drawable.btn_star_big_on); // Žuta zvezda
            } else {
                stars[i].setImageResource(android.R.drawable.btn_star_big_off); // Siva zvezda
            }
        }
    }
}