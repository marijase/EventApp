package marija.sekulic.eventsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateEventActivity extends AppCompatActivity {

    private EditText etName, etDesc, etLoc, etDateTime, etCapacity;
    private Spinner spCategory;
    private CheckBox cbPromoted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etName = findViewById(R.id.et_event_name);
        etDesc = findViewById(R.id.et_event_description);
        etLoc = findViewById(R.id.et_event_location);
        etDateTime = findViewById(R.id.et_event_datetime);
        etCapacity = findViewById(R.id.et_event_capacity);
        spCategory = findViewById(R.id.sp_category);
        cbPromoted = findViewById(R.id.cb_promoted);
        Button btnCreate = findViewById(R.id.btn_create_event);

        // spinner
        String[] categories = {"FESTIVAL", "PARTY", "CONCERT", "EXHIBITION", "STAND-UP & THEATER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // vidljivost kapaciteta
        cbPromoted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCapacity.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnCreate.setOnClickListener(v -> validateAndCreate());
    }

    private void validateAndCreate() {
        String name = etName.getText().toString().trim();
        String loc = etLoc.getText().toString().trim();
        String dateTime = etDateTime.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String desc = etDesc.getText().toString().trim();
        boolean isPromoted = cbPromoted.isChecked();

        // osnovna validacija
        if (name.isEmpty() || loc.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(this, "Popunite sva obavezna polja.", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = 0;
        // validacija kapaciteta za promoted
        if (isPromoted) {
            String capStr = etCapacity.getText().toString().trim();
            try {
                capacity = Integer.parseInt(capStr);
                if (capacity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Unesite ispravan kapacitet.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // kreiranje objekta i dodavanje u AppData
        Event newEvent;
        if (isPromoted) {
            newEvent = EventFactory.createPromotedEvent(name, desc, loc, dateTime, category, R.drawable.ic_launcher_background, capacity);
        } else {
            newEvent = EventFactory.createRegularEvent(name, desc, loc, dateTime, category, R.drawable.ic_launcher_background);
        }

        AppData.allEvents.add(newEvent);
        Toast.makeText(this, "Događaj uspešno kreiran.", Toast.LENGTH_SHORT).show();
        finish();
    }
}