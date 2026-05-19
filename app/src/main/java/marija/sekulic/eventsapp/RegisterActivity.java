package marija.sekulic.eventsapp;

import android.content.Intent;
import android.os.Bundle;
//import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    }
}