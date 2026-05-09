package marija.sekulic.eventsapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    public void clearAll() {
        this.events.clear();
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEvent;
        TextView txtName, txtCategory, txtLocation, txtDate, txtFeatured, txtSpaces;
        View container;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEvent = itemView.findViewById(R.id.img_event);
            txtName = itemView.findViewById(R.id.txt_event_name);
            txtCategory = itemView.findViewById(R.id.txt_category);
            txtLocation = itemView.findViewById(R.id.txt_location);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtFeatured = itemView.findViewById(R.id.txt_featured);
            txtSpaces = itemView.findViewById(R.id.txt_capacity);
            container = itemView.findViewById(R.id.item_container); // Glavni layout u event_item.xml
        }

        @SuppressLint("ResourceAsColor")
        public void bind(final Event event, final OnEventClickListener listener) {
            txtName.setText(event.getName());
            txtCategory.setText(event.getCategory());
            txtLocation.setText(event.getLocation());
            txtDate.setText(event.getDateTime());
            imgEvent.setImageResource(event.getImageResId());

            // Logika za Promoted događaje
            if (event.isPromoted()) {
                txtFeatured.setVisibility(View.VISIBLE);
                txtSpaces.setVisibility(View.VISIBLE);
                txtSpaces.setText("Slobodnih mesta: " + event.getAttendingCount() + "/" + event.getCapacity());
                container.setBackgroundColor(R.color.narandzasta_svetlija); // Distinktivna boja pozadine
            } else {
                txtFeatured.setVisibility(View.GONE);
                txtSpaces.setVisibility(View.GONE);
                container.setBackgroundColor(Color.WHITE);
            }

            itemView.setOnClickListener(v -> listener.onEventClick(event));
        }
    }
}