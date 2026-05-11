package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    EditText lgn_username;
    EditText lgn_password;
    Button lgn_btn;
    //String s1 = "admin";
    //String s2 = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        lgn_username = findViewById(R.id.id_lgn_username);
        lgn_password = findViewById(R.id.id_lgn_password);
        lgn_btn = findViewById(R.id.id_lgn_btn);

        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // Ako admin još nije u bazi, dodajemo ga sa tvojim podacima
        if (!sp.contains("registered_username")) {
            editor.putString("registered_username", "admin");
            editor.putString("registered_email", "admin@gmail.com");
            editor.putString("registered_password", "admin123"); // ili "admin123" po tvojoj želji
            editor.apply();
        }

        lgn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sad ukucano; privremeni podaci (samo u ovoj funkciji)
                String username = lgn_username.getText().toString();
                String password = lgn_password.getText().toString();

                // trajni podaci (one koje prenosimo)
                String registeredUser = sp.getString("registered_username", "");
                String registeredPass = sp.getString("registered_password", "");
                String registeredEmail = sp.getString("registered_email", "");

                // provera da li su username i password "admin"
                if (username.equals(registeredUser) && password.equals(registeredPass))
                {
                    Toast.makeText(LoginActivity.this, "Login succesful!", Toast.LENGTH_LONG).show();

                    // Zapamti ko je "trenutni" korisnik, za sledece aktivitije
                    editor.putString("current_user", username);
                    editor.putString("current_email", registeredEmail);
                    editor.putString("current_password", password);
                    editor.apply();

                    Intent open_events = new Intent(LoginActivity.this, EventsActivity.class);
                    startActivity(open_events);
                    finish();

                }
                else
                {
                    lgn_username.setText("");
                    lgn_password.setText("");
                    lgn_username.requestFocus();
                    Toast t = Toast.makeText(LoginActivity.this, "Username or password wrong! Try again.",
                                    Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }

            }
        });

    }
}