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
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        lgn_username = findViewById(R.id.id_lgn_username);
        lgn_password = findViewById(R.id.id_lgn_password);
        lgn_btn = findViewById(R.id.id_lgn_btn);

        userRepository = new UserRepository(this);

        // SharedPreferences  sada služi samo za pamćenje trenutne sesije (ko je trenutno na ekranu)
//        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();

        // Ako admin još nije u bazi, dodajemo ga sa tvojim podacima
//        if (!sp.contains("registered_username")) {
//            editor.putString("registered_username", "admin");
//            editor.putString("registered_email", "admin@gmail.com");
//            editor.putString("registered_password", "admin123"); // ili "admin123" po tvojoj želji
//            editor.apply();
//        }

        lgn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sad ukucano; privremeni podaci (samo u ovoj funkciji)
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

                            // cuvamo u SharedPreferences
                            SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("current_user", uname);
                            editor.putString("current_email", email);
                            editor.putString("current_server_id", serverId);
                            editor.putBoolean("current_is_admin", isAdmin);
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
















                //// !  Pravimo radnika za bazu korisnika ! ////
                //UserDAO userDAO = new UserDAO(LoginActivity.this);

                //  metoda iz UserDAO koja sama radi SQL upit, vadi heš i proverava lozinku
//                if (userDAO.checkLogin(username, password)) {
//
//                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
//
//                    // Pamtimo u SharedPreferences ko je trenutni ulogovani korisnik
//                    String praviEmailIzBaze = userDAO.getEmailByUsername(username);
//                    editor.putString("current_user", username);
//                    editor.putString("current_email", praviEmailIzBaze);
//                    editor.apply();
//
//                    // Prelazak na EventsActivity
//                    Intent open_events = new Intent(LoginActivity.this, EventsActivity.class);
//                    startActivity(open_events);
//                    finish();
//
//                } else {
//                    // Ako podaci u bazi ne postoje ili je lozinka pogrešna
//                    lgn_username.setText("");
//                    lgn_password.setText("");
//                    lgn_username.requestFocus();
//
//                    Toast t = Toast.makeText(LoginActivity.this, "Username or password wrong! Try again.", Toast.LENGTH_SHORT);
//                    t.show();
//                }

                // trajni podaci (one koje prenosimo)
//                String registeredUser = sp.getString("registered_username", "");
//                String registeredPass = sp.getString("registered_password", "");
//                String registeredEmail = sp.getString("registered_email", "");

                // provera da li su username i password "admin"
//                if (username.equals(registeredUser) && password.equals(registeredPass))
//                {
//                    Toast.makeText(LoginActivity.this, "Login succesful!", Toast.LENGTH_LONG).show();
//
//                    // Zapamti ko je "trenutni" korisnik, za sledece aktivitije
//                    editor.putString("current_user", username);
//                    editor.putString("current_email", registeredEmail);
//                    editor.putString("current_password", password);
//                    editor.apply();
//
//                    Intent open_events = new Intent(LoginActivity.this, EventsActivity.class);
//                    startActivity(open_events);
//                    finish();
//
//                }
//                else
//                {
//                    lgn_username.setText("");
//                    lgn_password.setText("");
//                    lgn_username.requestFocus();
//                    Toast t = Toast.makeText(LoginActivity.this, "Username or password wrong! Try again.",
//                                    Toast.LENGTH_SHORT);
//                    t.setGravity(Gravity.CENTER, 0, 0);
//                    t.show();
//                }

            }
        });

    }
}