package marija.sekulic.eventsapp;

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

        // Inicijalizacija zvezdica
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        // Preuzimanje imena eventa
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            txtEventName.setText(eventName);
        }

        // Postavljanje click listenera za svaku zvezdicu
        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> updateStars(index + 1));
        }

        btnConfirm.setOnClickListener(v -> {
            if (currentRating == 0) {
                Toast.makeText(this, "Molimo odaberite ocenu", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ocenili ste sa: " + currentRating, Toast.LENGTH_SHORT).show();
                finish(); // Zatvara aktivnost
            }
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