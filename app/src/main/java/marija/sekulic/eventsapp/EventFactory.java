package marija.sekulic.eventsapp;
import java.util.Date;

public class EventFactory {
    public static Event createRegularEvent(String name, String description, String location, Date dateTime, String category, int imageResId) {
        return new Event(name, description, location, dateTime, category, imageResId);
    }

    public static Event createPromotedEvent(String name, String description, String location, Date dateTime, String category, int imageResId, int capacity) {
        return new Event(name, description, location, dateTime, category, imageResId, true, capacity, 0, 0.0, 0);
    }

    public static int getImageForCategory(String category) {
        switch (category) {
            case "FESTIVAL":    return R.drawable.ic_festival;
            case "PARTY":       return R.drawable.ic_party;
            case "CONCERT":     return R.drawable.ic_concert;
            case "EXHIBITION":  return R.drawable.ic_exhibition;
            default:            return R.drawable.ic_event_default;
        }
    }
}