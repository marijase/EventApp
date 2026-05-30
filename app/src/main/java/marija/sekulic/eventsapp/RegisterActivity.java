package marija.sekulic.eventsapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
//import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    EditText rgs_username, rgs_email, rgs_password;
    TextView rgs_password_strength;
    Button rgs_btn;
    CheckBox rgs_is_admin;
    private UserRepository userRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        rgs_username = findViewById(R.id.id_rgs_username);
        rgs_email = findViewById(R.id.id_rgs_email);
        rgs_password = findViewById(R.id.id_rgs_password);
        rgs_btn = findViewById(R.id.id_rgs_btn);
        rgs_password_strength = findViewById(R.id.id_rgs_password_strength);
        rgs_btn = findViewById(R.id.id_rgs_btn);
        rgs_is_admin = findViewById(R.id.id_rgs_is_admin);

        userRepository = new UserRepository(this);

        rgs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uzimanje vrednosti tek na klik dugmeta ovde u click metodi
                String username = rgs_username.getText().toString();
                String email = rgs_email.getText().toString();
                String password = rgs_password.getText().toString();
                boolean isAdmin = rgs_is_admin.isChecked();

                if (username.isEmpty()) {
                    rgs_username.setError("Username cannot be empty!");
                    return;
                }
                if (email.isEmpty()) {
                    rgs_email.setError("Email cannot be empty!");
                    Toast.makeText(RegisterActivity.this, "Email je obavezno polje!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!email.contains("@")){
                    rgs_email.setError("Invalid email format!");
                    return;
                }
                if (password.length() < 8){
                    rgs_password.setError("Password must be at least 8 characters!");
                    //rgs_password.setText("");
                    return;
                }
                // Upisivanje u SharedPreferences
//                SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putString("registered_username", username);
//                editor.putString("registered_password", password);
//                editor.putString("registered_email", email);
//                editor.apply(); // cuva sve odjednom

                UserDAO userDAO = new UserDAO(RegisterActivity.this);
                boolean uspesnoReg = userDAO.registerUser(username, email, password);

                if (uspesnoReg) {
                    Toast.makeText(RegisterActivity.this, "Registracija uspešna!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Korisničko ime ili email već postoje!", Toast.LENGTH_SHORT).show();
                }

                // Prelazak na Login umesto na Events
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // ne dozvoljava Back dugme da vrati unazad
            }
        });
        rgs_username.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (Validation.isUsernameTaken(s.toString())) {
                    rgs_username.setError("Username is already taken.");
                } else {
                    rgs_username.setError(null);
                }
            }
        });

        rgs_email.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                if (val.length() == 0) {
                    rgs_email.setError(null);
                } else if (!Validation.isEmailValid(val)) {
                    rgs_email.setError("Invalid email format.");
                } else if (Validation.isEmailTaken(val)) {
                    rgs_email.setError("An account with this email already exists.");
                } else {
                    rgs_email.setError(null);
                }
            }
        });

        rgs_password.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString();
                int strength = Validation.getPasswordStrength(val);
                if (strength == -1 || val.length() == 0) {
                    rgs_password_strength.setText("");
                } else if (strength == 0) {
                    rgs_password_strength.setText("Weak");
                    rgs_password_strength.setTextColor(Color.RED);
                } else if (strength == 1) {
                    rgs_password_strength.setText("Good");
                    rgs_password_strength.setTextColor(Color.parseColor("#FFA500"));
                } else {
                    rgs_password_strength.setText("Strong");
                    rgs_password_strength.setTextColor(Color.parseColor("#2E7D32"));
                }
            }
        });
    }
}