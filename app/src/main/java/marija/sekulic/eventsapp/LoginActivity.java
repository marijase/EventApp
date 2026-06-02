package marija.sekulic.eventsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText lgn_username;
    EditText lgn_password;
    Button lgn_btn;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lgn_username = findViewById(R.id.id_lgn_username);
        lgn_password = findViewById(R.id.id_lgn_password);
        lgn_btn = findViewById(R.id.id_lgn_btn);

        userRepository = new UserRepository(this);

        lgn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = lgn_username.getText().toString().trim();
                String password = lgn_password.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Popunite sva polja!", Toast.LENGTH_SHORT).show();
                    return;
                }
                lgn_btn.setEnabled(false);

                userRepository.login(username, password, new RepositoryCallback<JSONObject>() {
                    @Override
                    public void onResult(JSONObject result) {
                        lgn_btn.setEnabled(true);

                        try {
                            String serverId = result.optString("_id", "");
                            String uname = result.optString("username", username);
                            String email = result.optString("email", "");
                            boolean isAdmin = result.optBoolean("isAdmin", false);
                            boolean offline = result.optBoolean("_offline", false);

                            // Čuvanje sesije u SharedPreferences
                            SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("current_user", uname);
                            editor.putString("current_email", email);
                            editor.putString("current_server_id", serverId);
                            editor.putBoolean("current_is_admin", isAdmin);

                            // KLJUČNO ZA OFFLINE MOD:
                            editor.putBoolean("is_offline_mode", offline);
                            editor.apply();

                            if (offline) {
                                Toast.makeText(LoginActivity.this,
                                        "Ulogovani ste offline.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Login successful!", Toast.LENGTH_SHORT).show();
                            }

                            Intent intent = new Intent(LoginActivity.this, EventsActivity.class);
                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Greška pri obradi odgovora.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        lgn_btn.setEnabled(true);
                        lgn_username.setText("");
                        lgn_password.setText("");
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}