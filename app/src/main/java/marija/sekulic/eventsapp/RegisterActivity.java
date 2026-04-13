package marija.sekulic.eventsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    EditText rgs_username;
    EditText rgs_email;
    EditText rgs_password;
    Button rgs_btn;
    Bundle rgs_bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        rgs_username = findViewById(R.id.id_rgs_username);
        rgs_email = findViewById(R.id.id_rgs_email);
        rgs_password = findViewById(R.id.id_rgs_password);
        rgs_btn = findViewById(R.id.id_rgs_btn);

        rgs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uzimanje vrednosti tek na klik dugmeta ovde u click metodi
                String username = rgs_username.getText().toString();
                String email = rgs_email.getText().toString();
                String password = rgs_password.getText().toString();

                rgs_bundle = new Bundle();
                rgs_bundle.putString("username_kljuc", username);
                rgs_bundle.putString("email_kljuc", email);
                rgs_bundle.putString("password_kljuc", password);

                Intent open_events = new Intent(RegisterActivity.this, EventsActivity.class);
                open_events.putExtras(rgs_bundle); // mora ovaj redosled prvo put pa onda START
                startActivity(open_events);

            }
        });

    }
}