package marija.sekulic.eventsapp;

import android.content.Intent;
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

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            user_name = bundle.getString("username_kljuc");
            mail_name = bundle.getString("email_kljuc");

            username.setText(user_name);
            mail.setText(mail_name);
        }

        // Password dugme
        btn_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open_password = new Intent(ProfileActivity.this, PasswordActivity.class);
                startActivity(open_password);
            }
        });


        // End session dugme
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent back_main = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(back_main);
            }
        });
    }
}