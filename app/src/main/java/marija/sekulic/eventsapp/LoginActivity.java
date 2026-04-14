package marija.sekulic.eventsapp;

import android.content.Intent;
import android.os.Bundle;
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
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        lgn_username = findViewById(R.id.id_lgn_username);
        lgn_password = findViewById(R.id.id_lgn_password);
        lgn_btn = findViewById(R.id.id_lgn_btn);
        bundle = new Bundle();

        lgn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = lgn_username.getText().toString();
                String password = lgn_password.getText().toString();

                if (username.equals("admin") && password.equals("admin")){
                    // uspesno logovanje Admine
                    Toast.makeText(LoginActivity.this, "Login succesful!",
                                    Toast.LENGTH_LONG).show();
                    // u bundle se stavljaju samo validni podaci, zato ne treba ranije da se pise
                    bundle.putString("username_kljuc", username);
                    bundle.putString("password_kljuc", password);// tek "admin" i "admin" su validni

                    Intent open_events = new Intent(LoginActivity.this, EventsActivity.class);
                    open_events.putExtras(bundle);
                    startActivity(open_events);

                }
                else if (!username.equals("admin") && !password.equals("admin")){
                    lgn_username.setText("");
                    lgn_password.setText("");
                    Toast.makeText(LoginActivity.this, "Email and password wrong! Try again.",
                                    Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals("admin")){
                    lgn_username.setText("");
                    lgn_password.setText("");
                    Toast.makeText(LoginActivity.this, "Wrong password! Try again.",
                                    Toast.LENGTH_SHORT).show();
                }
                else {
                    lgn_username.setText("");
                    lgn_password.setText("");
                    Toast.makeText(LoginActivity.this,"Wrong email! Try again",
                                    Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}