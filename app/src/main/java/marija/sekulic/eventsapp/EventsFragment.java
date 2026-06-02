package marija.sekulic.eventsapp;

import android.content.Context; // Potrebno za MODE_PRIVATE
import android.content.Intent;
import android.content.SharedPreferences; // Potrebno za SharedPreferences
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private EventAdapter adapter;
    private List<Button> categoryButtons = new ArrayList<>();
    private EventDAO eventDAO;

    public EventsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryButtons = new ArrayList<>();
        eventDAO = new EventDAO(getContext());

        adapter = new EventAdapter(event -> {
            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // ===== KONTROLA VIDLJIVOSTI DUGMETA "DODAJ EVENT" =====
        Button btnAddEvent = view.findViewById(R.id.btn_add_event);

        if (getContext() != null) {
            // Otvaramo SharedPreferences koji je LoginActivity napunio
            SharedPreferences sp = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
            // Izvlačimo da li je admin (podrazumevano je false ako ne nađe ključ)
            boolean isAdmin = sp.getBoolean("current_is_admin", false);

            if (isAdmin) {
                btnAddEvent.setVisibility(View.VISIBLE); // Prikaži dugme ako je admin
            } else {
                btnAddEvent.setVisibility(View.GONE);    // Potpuno ukloni dugme ako nije admin
            }
        }

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateEventActivity.class);
            startActivity(intent);
        });

        // Filtriranje
        LinearLayout categoryContainer = view.findViewById(R.id.categories_container);
        setupCategories(categoryContainer);

        // Početni prikaz: Svi sortirani događaji
        updateList("SVE");

        return view;
    }

    private String aktivnaKategorija = "SVE";

    private void setupCategories(LinearLayout container) {
        String[] categories = {"SVE", "FESTIVAL", "PARTY", "CONCERT", "EXHIBITION", "STAND-UP & THEATER"};

        container.removeAllViews();
        categoryButtons.clear();

        for (String cat : categories) {
            Button btn = new Button(getContext());
            btn.setText(cat);
            btn.setAllCaps(true);

            btn.setOnClickListener(v -> {
                aktivnaKategorija = cat;
                updateList(cat);
                highlightActiveButton(btn);
            });
            container.addView(btn);
            categoryButtons.add(btn);
        }
        highlightActiveButton(categoryButtons.get(0));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList(aktivnaKategorija);
    }

    private void updateList(String category) {
        if (eventDAO != null) {
            List<Event> listaIzBaze = eventDAO.getEventsByCategory(category);
            adapter.setEvents(listaIzBaze);
        }
    }

    private void highlightActiveButton(Button activeBtn) {
        for (Button b : categoryButtons) {
            b.setBackgroundTintList(null);
            b.setTextColor(Color.BLACK);
        }
        if (getContext() != null) {
            activeBtn.setBackgroundTintList(getContext().getColorStateList(R.color.ljubicasta_tamnija));
            activeBtn.setTextColor(Color.GREEN);
        }
    }
}