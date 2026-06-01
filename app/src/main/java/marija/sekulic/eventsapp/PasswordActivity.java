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

                btn_save.setEnabled(false);
                UserRepository userRepository = new UserRepository(PasswordActivity.this);
                userRepository.changePassword(user, currentInput, newInput, new RepositoryCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        btn_save.setEnabled(true);
                        Toast.makeText(PasswordActivity.this, "Lozinka uspešno izmenjena!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        btn_save.setEnabled(true);
                        current_pass.setText("");
                        new_password.setText("");
                        Toast.makeText(PasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}