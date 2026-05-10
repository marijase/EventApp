package marija.sekulic.eventsapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//test2
public class AppData {
    public static List<Event> allEvents = new ArrayList<>();
    public static List<Event> interestedEvents = new ArrayList<>();
    public static List<Event> attendingEvents = new ArrayList<>();
    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());


    static {
        // POPUNJAVANJE LISTA RUCNO:


 // 1. ALL EVENTS
        // promoted
        allEvents.add(EventFactory.createPromotedEvent("EXIT Festival","Muzički festival","Novi Sad",parse("15.07.2026 18:00"),"FESTIVAL",R.drawable.pictures,50000));
        allEvents.add(EventFactory.createPromotedEvent("NEON Party","Tehno žurka","Beograd",parse("10.05.2026 22:00"),"PARTY",R.drawable.pictures,500));
        // prosli
        allEvents.add(EventFactory.createRegularEvent("Winter Fest","Zimska idila","Kopaonik",parse("01.01.2024 20:00"),"FESTIVAL",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("StandUp Veče","Smeha do suza","Novi Sad",parse("15.02.2024 21:00"),"STAND-UP & THEATER",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Art Exhibition","Moderna umetnost","Beograd",parse("10.03.2024 10:00"),"EXHIBITION",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Rock Gig","Lokalni bendovi","Subotica",parse("20.03.2024 21:00"),"CONCERT",R.drawable.pictures));
        // regularni dogadjaji (dopuna do 18)
        allEvents.add(EventFactory.createRegularEvent("Rooftop Summer","Party na krovu","Beograd",parse("20.06.2026 21:00"),"PARTY",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Comedy Show","Stand up komedija","Novi Sad",parse("05.06.2026 20:00"),"STAND-UP & THEATER",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Beer Fest","Pivo i muzika","Beograd",parse("15.08.2026 18:00"),"FESTIVAL",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Classical Night","Betoven i Mocart","Novi Sad",parse("12.06.2026 19:30"),"CONCERT",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Tech Talk","Budućnost IT-a","Novi Sad",parse("25.06.2026 17:00"),"EXHIBITION",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Wine Tasting","Najbolja vina","Fruška Gora",parse("01.07.2026 14:00"),"FESTIVAL",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Jazz Night","Džez u bašti","Beograd",parse("05.07.2026 21:00"),"CONCERT",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Cinema Open Air","Film pod zvezdama","Novi Sad",parse("10.07.2026 21:30"),"STAND-UP & THEATER",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Dance Battle","Breakdance takmičenje","Niš",parse("15.07.2026 18:00"),"PARTY",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Gallery Opening","Novi talenti","Beograd",parse("20.07.2026 19:00"),"EXHIBITION",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Punk Rock Show","Energija na bini","Kragujevac",parse("25.07.2026 22:00"),"CONCERT",R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Magic Show","Trikovi i iluzije","Subotica",parse("30.07.2026 20:00"),"STAND-UP & THEATER",R.drawable.pictures));

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

    // vraca sve događaje sortirane tako da promoted događaji dolaze prvi
    public static List<Event> getSortedEvents() {
        List<Event> sorted = new ArrayList<>(allEvents);
        sorted.sort(Comparator.comparing(Event::isPromoted).reversed());
        return sorted;
    }

    // Filtriranje i sortiranje (promoted idu na vrh)
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
    // Metoda za pronalaženje događaja prema nazivu
    public static Event findByName(String name) {
        for (Event e : allEvents) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

}