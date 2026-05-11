package marija.sekulic.eventsapp;

import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String name, description, location,  category;
    private int imageResId, capacity, attendingCount, ratingCount;
    private boolean isPromoted;
    private double averageRating;
    private Date dateTime;

    // promoted
    public Event(String name, String description, String location, Date dateTime, String category,
                 int imageResId, boolean isPromoted, int capacity, int attendingCount,
                 double averageRating, int ratingCount) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.category = category;
        this.imageResId = imageResId;
        this.isPromoted = isPromoted;
        this.capacity = capacity;
        this.attendingCount = attendingCount;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    //  regularni
    public Event(String name, String description, String location, Date dateTime, String category, int imageResId) {
        this(name, description, location, dateTime, category, imageResId, false, 0, 0, 0.0, 0);
    }

    public boolean isPast() {
        return dateTime != null && dateTime.before(new Date());
    }

    public double addRating(int rating) {
        double totalScore = averageRating * ratingCount;
        ratingCount++;
        averageRating = (totalScore + rating) / ratingCount;
        return averageRating;
    }

    // Geteri i Seteri
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Date getDateTime() { return dateTime; }
    public String getCategory() { return category; }
    public int getImageResId() { return imageResId; }
    public boolean isPromoted() { return isPromoted; }
    public int getCapacity() { return capacity; }
    public int getAttendingCount() { return attendingCount; }
    public double getAverageRating() { return averageRating; }
    public int getRatingCount() { return ratingCount; }
}