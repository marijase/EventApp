package marija.sekulic.eventsapp;

import android.content.Intent;
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
    private final List<Button> categoryButtons = new ArrayList<>();
    // greska: categoryButtons may be final

    public EventsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(event -> {
            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("EVENT_NAME", event.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        Button btnAddEvent = view.findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateEventActivity.class);
            // greska: can't resolve symbol CreateEventActivity
            startActivity(intent);
        });

        // filtriranje
        LinearLayout categoryContainer = view.findViewById(R.id.categories_container);
        setupCategories(categoryContainer);

        // Početni prikaz: Svi sortirani događaji
        updateList("SVE");

        return view;
    }

    private void setupCategories(LinearLayout container) {
        String[] categories = {"SVE", "FESTIVAL", "PARTY", "CONCERT", "EXHIBITION", "STAND-UP & THEATER"};

        container.removeAllViews();
        categoryButtons.clear();

        for (String cat : categories) {
            Button btn = new Button(getContext());
            btn.setText(cat);
            btn.setAllCaps(true);

            btn.setOnClickListener(v -> {
                updateList(cat);
                highlightActiveButton(btn);
            });
            container.addView(btn);
            categoryButtons.add(btn);
        }
            highlightActiveButton(categoryButtons.get(0));
    }

    private void updateList(String category) {
        adapter.setEvents(AppData.getSortedEventsByCategory(category));
    }

    private void highlightActiveButton(Button activeBtn) {
        for (Button b : categoryButtons) {
            b.setBackgroundTintList(null); // Reset
            b.setTextColor(Color.BLACK);
        }
        // greska: method invocation getColorStateList may produce nullpointerexception
        if (getContext()!=null){
            activeBtn.setBackgroundTintList(getContext().getColorStateList(R.color.ljubicasta_tamnija));
            activeBtn.setTextColor(Color.GREEN);
        }

    }
}