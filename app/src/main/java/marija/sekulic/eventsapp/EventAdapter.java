package marija.sekulic.eventsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;
    private OnRateClickListener rateListener;
    private boolean attendingMode = false;
    public void setAttendingMode(boolean attendingMode) { // new
        this.attendingMode = attendingMode;
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
    public interface OnRateClickListener {
        void onRateClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }


    public EventAdapter(OnEventClickListener listener, boolean attendingMode) {
        this.listener = listener;
        this.attendingMode = attendingMode;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position), listener, attendingMode);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }


    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEvent;
        TextView txtName, txtCategory, txtLocation, txtDate, txtFeatured, txtSpaces;
        View container;
        Button btnRate;
        Context context;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            imgEvent = itemView.findViewById(R.id.img_event);
            txtName = itemView.findViewById(R.id.txt_event_name);
            txtCategory = itemView.findViewById(R.id.txt_category);
            txtLocation = itemView.findViewById(R.id.txt_location);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtFeatured = itemView.findViewById(R.id.txt_featured);
            txtSpaces = itemView.findViewById(R.id.txt_capacity);
            container = itemView.findViewById(R.id.item_container); // Glavni layout u event_item.xml
            btnRate = itemView.findViewById(R.id.btn_rate_item);
        }



        @SuppressLint("ResourceAsColor")
        public void bind(final Event event, final OnEventClickListener listener, boolean attendingMode) {
            Toast.makeText(context, "attendingMode=" + attendingMode + " isPast=" + event.isPast(), Toast.LENGTH_SHORT).show();
            txtName.setText(event.getName());

            txtCategory.setText(event.getCategory());
            txtLocation.setText(event.getLocation());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            imgEvent.setImageResource(event.getImageResId());

            if (event.getDateTime() != null) {
                txtDate.setText(sdf.format(event.getDateTime()));
            } else {
                txtDate.setText(""); // FIX da ne puca
            }


            if (event.isPromoted()) {
                if (txtFeatured != null) txtFeatured.setVisibility(View.VISIBLE);
                if (txtSpaces != null) {
                    txtSpaces.setVisibility(View.VISIBLE);
                    int left = event.getCapacity() - event.getAttendingCount();
                    txtSpaces.setText("Slobodnih mesta: " + left + "/" + event.getCapacity());
                }
                // Ispravan način postavljanja boje iz resursa
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.narandzasta_svetlija));
            } else {
                if (txtFeatured != null) txtFeatured.setVisibility(View.GONE);
                if (txtSpaces != null) txtSpaces.setVisibility(View.GONE);
                container.setBackgroundColor(Color.WHITE);
            }

            if (attendingMode && event.isPast()) {
                btnRate.setVisibility(View.VISIBLE);
                imgEvent.setVisibility(View.GONE);

                btnRate.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RatingActivity.class);
                    intent.putExtra("EVENT_NAME", event.getName());
                    context.startActivity(intent);
                });
            } else {
                btnRate.setVisibility(View.GONE);
            }


            itemView.setOnClickListener(v -> listener.onEventClick(event));

        }
    }
}