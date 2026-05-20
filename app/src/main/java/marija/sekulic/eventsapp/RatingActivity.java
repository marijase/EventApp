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

            // 2. Pravimo radnika za bazu podataka
            EventDAO eventDAO = new EventDAO(RatingActivity.this);

            // 3. Tražimo događaj iz baze preko ID-ja (onog što smo maločas sredili u EventDAO)
            Event event = eventDAO.getEventById(eventId);

            if (event != null) {
                // Kod baze podataka, proveru da li je korisnik već ocenio ovaj događaj
                // radimo u posebnoj tabeli ili preko SharedPreferences, ali pošto tvoj objekat ima isRated():
                if (event.isRated()) {
                    Toast.makeText(this, "Već ste ocenili ovaj događaj", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 4. Računamo novi prosek i broj ocena u Javi unutar samog objekta
                event.addRating(currentRating);
                event.setRated(true);

                // 5. Čuvamo te nove izračunate ocene nazad u bazu podataka!
                boolean uspesnoOcenjeno = eventDAO.updateEventRating(event);

                if (uspesnoOcenjeno) {
                    Toast.makeText(this, "Hvala na oceni!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Greška pri čuvanju ocene u bazi.", Toast.LENGTH_SHORT).show();
                }
            }

            finish();
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