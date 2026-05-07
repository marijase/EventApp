package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class ProfileActivity extends AppCompatActivity {

    TextView username, mail;
    Button btn_password, btn_end;
    String user_name, mail_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username = findViewById(R.id.id_profile_username);
        mail = findViewById(R.id.id_profile_mail);
        btn_password = findViewById(R.id.id_btn_change_password);
        btn_end = findViewById(R.id.id_btn_end_sesion);


        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);

        // Uzimamo podatke (ako ih nema, ispisaće "N/A"  - ova if logika skrivena je u getString metodi)
        // getString trazi u memoriji kljuc pod nazivom "current_user".
        user_name = sp.getString("current_user", "Not Available");
        mail_name = sp.getString("current_email", "Not Available");

        // Prikazujemo podatke na ekranu
        username.setText(user_name);
        mail.setText(mail_name);


        btn_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open_password = new Intent(ProfileActivity.this, PasswordActivity.class);
                startActivity(open_password);
            }
        });


        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Brisemo ko je trenutno ulogovan da bi sesija bila cista
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("current_user");
                editor.remove("current_email");
                editor.apply();

                Intent back_main = new Intent(ProfileActivity.this, MainActivity.class);
                back_main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(back_main);
            }
        });
    }
}