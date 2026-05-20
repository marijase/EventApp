package marija.sekulic.eventsapp;

import android.content.SharedPreferences;
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

//                if (!currentInput.equals(old_password)) {
//                    current_pass.setError("Trenutna lozinka nije tačna!");
//                    current_pass.setText("");
//                    new_password.setText("");
//                    current_pass.requestFocus();
//                    return;
//                }

                if (newInput.length() < 8) {
                    new_password.setError("Nova lozinka mora imati bar 8 karaktera!");
                    new_password.setText("");
                    return;
                }

                SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                String user = sp.getString("current_user", "");
                UserDAO userDAO = new UserDAO(PasswordActivity.this);

                // 5. ISPRAVLJENO: Prosleđujemo tvog 'user'-a umesto 'loggedInUser'
                if (!userDAO.checkLogin(user, currentInput)) {
                    current_pass.setError("Trenutna lozinka nije tačna!");
                    current_pass.setText("");
                    new_password.setText("");
                    current_pass.requestFocus();
                    return;
                }

                // 6. Ako je stara lozinka uspešno proverena kroz bazu, upisujemo novu lozinku u bazu!
                boolean uspesnoIzmenjeno = userDAO.updateUserPassword(user, newInput);

                if (uspesnoIzmenjeno) {
                    // Ažuriramo SharedPreferences čisto da se osiguramo ako ti zatreba negde u sesiji
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("registered_password", newInput);
                    editor.putString("current_password", newInput);
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