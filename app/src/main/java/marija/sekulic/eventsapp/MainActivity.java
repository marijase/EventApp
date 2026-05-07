package marija.sekulic.eventsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// import androidx.lifecycle.ViewTreeViewModelKt;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_login;
    Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);  - da aplikacija ide skroz od ivice do ivice

        // setContentView() → prikazuje taj UI na ekranu
        // R.layout.activity_main je XML fajl
        // => dakle povezuje XML layout sa ovim
        setContentView(R.layout.activity_main);

        //služi za “moderni full-screen layout”,
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // IZMENE:
        btn_login = findViewById(R.id.id_btn_login);
        btn_login.setOnClickListener(this); // this znači:“ova trenutna klasa (MainActivity)”

        btn_register = findViewById(R.id.id_btn_register);
        btn_register.setOnClickListener(this);

    }

    @Override                        // View = bilo koji UI element koji je kliknut
    public void onClick (View viju){ // svi oni (slika, dugme...) nasledjuju View
        if (viju.getId() == R.id.id_btn_login){
            // kliknut Login ->  napravi objekat intenta pa pokreni
            Intent open_login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(open_login);
        }
        else if (viju.getId() == R.id.id_btn_register){
            // kliknut Register
            Intent open_register = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(open_register);
        }

    }



}