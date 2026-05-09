package marija.sekulic.eventsapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
//test
public class AppData {
    public static List<Event> allEvents = new ArrayList<>();
    public static List<Event> interestedEvents = new ArrayList<>();
    public static List<Event> attendingEvents = new ArrayList<>();

    static {
        // POPUNJAVANJE LISTA RUCNO:


 // 1. ALL EVENTS
        // promoted dogadjaji
        allEvents.add(EventFactory.createPromotedEvent("EXIT Festival", "Muzički festival", "Novi Sad", "15.07.2026 18:00", "Festival", R.drawable.pictures, 50000));
        allEvents.add(EventFactory.createPromotedEvent("NEON Party", "Tehno žurka", "Beograd", "10.05.2026 22:00", "Party", R.drawable.pictures, 500));
        // prosli dogadjaji
        allEvents.add(EventFactory.createRegularEvent("Winter Fest", "Zimska idila", "Kopaonik", "01.01.2024 20:00", "Festival", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("StandUp Veče", "Smeha do suza", "Novi Sad", "15.02.2024 21:00", "Stand-Up & Theater", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Art Exhibition", "Moderna umetnost", "Beograd", "10.03.2024 10:00", "Exhibition", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Rock Gig", "Lokalni bendovi", "Subotica", "20.03.2024 21:00", "Concert", R.drawable.pictures));
        // regularni dogadjaji (dopuna do 18)
        allEvents.add(EventFactory.createRegularEvent("Rooftop Summer", "Party na krovu", "Beograd", "20.06.2026 21:00", "Party", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Comedy Show", "Stand up komedija", "Novi Sad", "05.06.2026 20:00", "Stand-Up & Theater", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Beer Fest", "Pivo i muzika", "Beograd", "15.08.2026 18:00", "Festival", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Classical Night", "Betoven i Mocart", "Novi Sad", "12.06.2026 19:30", "Concert", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Tech Talk", "Budućnost IT-a", "Novi Sad", "25.06.2026 17:00", "Exhibition", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Wine Tasting", "Najbolja vina", "Fruška Gora", "01.07.2026 14:00", "Festival", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Jazz Night", "Džez u bašti", "Beograd", "05.07.2026 21:00", "Concert", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Cinema Open Air", "Film pod zvezdama", "Novi Sad", "10.07.2026 21:30", "Stand-Up & Theater", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Dance Battle", "Breakdance takmičenje", "Niš", "15.07.2026 18:00", "Party", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Gallery Opening", "Novi talenti", "Beograd", "20.07.2026 19:00", "Exhibition", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Punk Rock Show", "Energija na bini", "Kragujevac", "25.07.2026 22:00", "Concert", R.drawable.pictures));
        allEvents.add(EventFactory.createRegularEvent("Magic Show", "Trikovi i iluzije", "Subotica", "30.07.2026 20:00", "Stand-Up & Theater", R.drawable.pictures));

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