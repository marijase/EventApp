package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {
    EditText current_pass, new_password;
    Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        current_pass = findViewById(R.id.id_current_password);
        new_password = findViewById(R.id.id_new_password);
        btn_save = findViewById(R.id.id_btn_save);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentInput = current_pass.getText().toString();
                String newInput = new_password.getText().toString();

                if (currentInput.isEmpty() || newInput.isEmpty()) {
                    Toast.makeText(PasswordActivity.this, "Popunite sva polja!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newInput.length() < 8) {
                    new_password.setError("Nova lozinka mora imati bar 8 karaktera!");
                    new_password.setText("");
                    return;
                }

                SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                String user = sp.getString("current_user", "");
                String savedPassInSp = sp.getString("current_password", ""); // Fallback sa Logina

                UserDAO userDAO = new UserDAO(PasswordActivity.this);

                // HIBRIDNA PROVERA TRENUTNE LOZINKE: Proveri lokalnu bazu ILI SharedPreferences fallback
                boolean isCurrentPasswordCorrect = false;

                if (userDAO.checkLogin(user, currentInput)) {
                    isCurrentPasswordCorrect = true;
                } else if (!savedPassInSp.isEmpty() && currentInput.equals(savedPassInSp)) {
                    isCurrentPasswordCorrect = true;
                }

                if (!isCurrentPasswordCorrect) {
                    current_pass.setError("Trenutna lozinka nije tačna!");
                    current_pass.setText("");
                    new_password.setText("");
                    current_pass.requestFocus();
                    return;
                }

                // OSIGURANJE KORISNIKA: Ako korisnik ne postoji lokalno u SQLite, ubacujemo ga pre izmene lozinke
                int localUserId = userDAO.getUserIdByUsername(user);
                if (localUserId <= 0) {
                    DatabaseHelper dbHelper = new DatabaseHelper(PasswordActivity.this);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues uValues = new ContentValues();
                    uValues.put("username", user);
                    uValues.put("email", user + "@gmail.com");
                    uValues.put("password", "temp_hash_placeholder"); // Privremeni zapis
                    db.insert("users", null, uValues);
                    db.close();
                }

                // Sada sigurno imamo korisnika u bazi i tvoj UserDAO može uspešno da odradi update i hesiranje
                boolean uspesnoIzmenjeno = userDAO.updateUserPassword(user, newInput);

                if (uspesnoIzmenjeno) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("registered_password", newInput);
                    editor.putString("current_password", newInput); // Ažuriramo i fallback
                    editor.apply();

                    Toast.makeText(PasswordActivity.this, "Lozinka uspešno izmenjena!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PasswordActivity.this, "Greška pri čuvanju lozinke u bazi.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}