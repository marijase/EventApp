package marija.sekulic.eventsapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String name, description, location, dateTime, category;
    private int imageResId, capacity, attendingCount, ratingCount;
    private boolean isPromoted;
    private double averageRating;

    // Konstruktor za promoted događaje
    public Event(String name, String description, String location, String dateTime, String category,
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

    // Konstruktor za regularne događaje
    public Event(String name, String description, String location, String dateTime, String category, int imageResId) {
        this(name, description, location, dateTime, category, imageResId, false, 0, 0, 0.0, 0);
    }

    public boolean isPast() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(this.dateTime);
            return eventDate != null && eventDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
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
    public String getDateTime() { return dateTime; }
    public String getCategory() { return category; }
    public int getImageResId() { return imageResId; }
    public boolean isPromoted() { return isPromoted; }
    public int getCapacity() { return capacity; }
    public int getAttendingCount() { return attendingCount; }
    public double getAverageRating() { return averageRating; }
    public int getRatingCount() { return ratingCount; }
}