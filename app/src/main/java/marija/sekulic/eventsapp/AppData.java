package marija.sekulic.eventsapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppData {
    public static List<Event> allEvents = new ArrayList<>();
    public static List<Event> interestedEvents = new ArrayList<>();
    public static List<Event> attendingEvents = new ArrayList<>();
    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());


    static {

 // 1. ALL EVENTS
        // promoted
        allEvents.add(EventFactory.createPromotedEvent("EXIT Festival", "Muzički festival", "Novi Sad", d("15.07.2026 18:00"), "FESTIVAL", EventFactory.getImageForCategory("FESTIVAL"), 50000));
        allEvents.add(EventFactory.createPromotedEvent("NEON Party", "Tehno žurka", "Beograd", d("10.05.2026 22:00"), "PARTY", EventFactory.getImageForCategory("PARTY"), 500));
        // prosli
        allEvents.add(EventFactory.createRegularEvent("Winter Fest", "Zimska idila", "Kopaonik", d("01.01.2024 20:00"), "FESTIVAL", EventFactory.getImageForCategory("FESTIVAL")));
        allEvents.add(EventFactory.createRegularEvent("StandUp Veče", "Smeha do suza", "Novi Sad", d("15.02.2024 21:00"), "STAND-UP & THEATER", EventFactory.getImageForCategory("STAND-UP & THEATER")));
        allEvents.add(EventFactory.createRegularEvent("Art Exhibition", "Moderna umetnost", "Beograd", d("10.03.2024 10:00"), "EXHIBITION", EventFactory.getImageForCategory("EXHIBITION")));
        allEvents.add(EventFactory.createRegularEvent("Rock Gig", "Lokalni bendovi", "Subotica", d("20.03.2024 21:00"), "CONCERT", EventFactory.getImageForCategory("CONCERT")));
        // regularni dogadjaji (dopuna do 18)
        allEvents.add(EventFactory.createRegularEvent("Rooftop Summer", "Party na krovu", "Beograd", d("20.06.2026 21:00"), "PARTY", EventFactory.getImageForCategory("PARTY")));
        allEvents.add(EventFactory.createRegularEvent("Comedy Show", "Stand up komedija", "Novi Sad", d("05.06.2026 20:00"), "STAND-UP & THEATER", EventFactory.getImageForCategory("STAND-UP & THEATER")));
        allEvents.add(EventFactory.createRegularEvent("Beer Fest", "Pivo i muzika", "Beograd", d("15.08.2026 18:00"), "FESTIVAL", EventFactory.getImageForCategory("FESTIVAL")));
        allEvents.add(EventFactory.createRegularEvent("Classical Night", "Betoven i Mocart", "Novi Sad", d("12.06.2026 19:30"), "CONCERT", EventFactory.getImageForCategory("CONCERT")));
        allEvents.add(EventFactory.createRegularEvent("Tech Talk", "Budućnost IT-a", "Novi Sad", d("25.06.2026 17:00"), "EXHIBITION", EventFactory.getImageForCategory("EXHIBITION")));
        allEvents.add(EventFactory.createRegularEvent("Wine Tasting", "Najbolja vina", "Fruška Gora", d("01.07.2026 14:00"), "FESTIVAL", EventFactory.getImageForCategory("FESTIVAL")));
        allEvents.add(EventFactory.createRegularEvent("Jazz Night", "Džez u bašti", "Beograd", d("05.07.2026 21:00"), "CONCERT", EventFactory.getImageForCategory("CONCERT")));
        allEvents.add(EventFactory.createRegularEvent("Cinema Open Air", "Film pod zvezdama", "Novi Sad", d("10.07.2026 21:30"), "STAND-UP & THEATER", EventFactory.getImageForCategory("STAND-UP & THEATER")));
        allEvents.add(EventFactory.createRegularEvent("Dance Battle", "Breakdance takmičenje", "Niš", d("15.07.2026 18:00"), "PARTY", EventFactory.getImageForCategory("PARTY")));
        allEvents.add(EventFactory.createRegularEvent("Gallery Opening", "Novi talenti", "Beograd", d("20.07.2026 19:00"), "EXHIBITION", EventFactory.getImageForCategory("EXHIBITION")));
        allEvents.add(EventFactory.createRegularEvent("Punk Rock Show", "Energija na bini", "Kragujevac", d("25.07.2026 22:00"), "CONCERT", EventFactory.getImageForCategory("CONCERT")));
        allEvents.add(EventFactory.createRegularEvent("Magic Show", "Trikovi i iluzije", "Subotica", d("30.07.2026 20:00"), "STAND-UP & THEATER", EventFactory.getImageForCategory("STAND-UP & THEATER")));

// 2. INTERESTED
        interestedEvents.add(allEvents.get(0));
        interestedEvents.add(allEvents.get(6));
        interestedEvents.add(allEvents.get(8));
        interestedEvents.add(allEvents.get(10));
        interestedEvents.add(allEvents.get(12));
// 3. ATTENDING
        attendingEvents.add(allEvents.get(0));
        attendingEvents.add(allEvents.get(2));
        attendingEvents.add(allEvents.get(3));
        attendingEvents.add(allEvents.get(7));
        attendingEvents.add(allEvents.get(8));
        attendingEvents.add(allEvents.get(4));
        attendingEvents.add(allEvents.get(1));
        attendingEvents.add(allEvents.get(5));
    }

    private static Date parse(String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }
    private static Date d(String value) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    // promoted idu na vrh
    public static List<Event> getSortedEvents() {
        List<Event> sorted = new ArrayList<>(allEvents);
        sorted.sort(Comparator.comparing(Event::isPromoted).reversed());
        return sorted;
    }

    // promoted idu na vrh
    public static List<Event> getSortedEventsByCategory(String category) {
        if (category.equalsIgnoreCase("SVE")) return getSortedEvents();

        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                filtered.add(e);
            }
        }
        filtered.sort(Comparator.comparing(Event::isPromoted).reversed());
        return filtered;
    }
    public static Event findByName(String name) {
        for (Event e : allEvents) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

}