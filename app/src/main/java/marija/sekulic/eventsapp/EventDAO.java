package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class EventDAO {
    private final DatabaseHelper dbHelper;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public EventDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Unos jednog događaja uz prebacivanje Date objekta u TEXT String za SQLite
    public boolean createEvent(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // proveravamo da li event vec postoji po nazivu i datumu
        Cursor check = db.rawQuery("SELECT id FROM events WHERE naziv = ? AND datumVreme = ?",
                new String[]{event.getName(), event.getDateTime() != null ? sdf.format(event.getDateTime()) : ""});
        if (check.getCount() > 0) {
            check.close();
            db.close();
            return false; // vec postoji, preskacemo
        }
        check.close();

        ContentValues values = new ContentValues();

        values.put("mongo_id", event.getMongoId()); // IZMENA: Upisujemo mongo_id string
        values.put("naziv", event.getName());
        values.put("opis", event.getDescription());
        values.put("lokacija", event.getLocation());
        values.put("kategorija", event.getCategory());
        values.put("promoted", event.isPromoted() ? 1 : 0);
        values.put("kapacitet", event.getCapacity());

        if (event.getDateTime() != null) {
            values.put("datumVreme", sdf.format(event.getDateTime()));
        } else {
            values.put("datumVreme", "");
        }

        long result = db.insert("events", null, values);
        db.close();
        return result != -1;
    }

    // Puni bazu početnim događajima iz AppData ukoliko je prazna
    public void populateIfEmpty(List<Event> existingEvents) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM events", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();

        if (count == 0 && existingEvents != null) {
            for (Event e : existingEvents) {
                createEvent(e);
            }
        }
    }

    // Čita sve događaje i sortira ih tako da promovisani idu na vrh (ORDER BY promoted DESC)
    public List<Event> getAllEvents() {
        return getEventsFromCursor("SELECT * FROM events ORDER BY promoted DESC", null);
    }

    // Filtrira po kategoriji i sortira da promovisani idu na vrh
    public List<Event> getEventsByCategory(String category) {
        if (category.equalsIgnoreCase("SVE")) {
            return getAllEvents();
        }
        return getEventsFromCursor("SELECT * FROM events WHERE kategorija = ? ORDER BY promoted DESC", new String[]{category});
    }

    // Prolazak kroz Cursor iterator
    private List<Event> getEventsFromCursor(String sql, String[] selectionArgs) {
        List<Event> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                // Koristimo fabričke metode za kreiranje objekata na osnovu baze
                // IZMENA: Čitamo mongo_id iz baze
                String mongoId = cursor.getString(cursor.getColumnIndexOrThrow("mongo_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("naziv"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("opis"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("lokacija"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("kategorija"));
                boolean isPromoted = cursor.getInt(cursor.getColumnIndexOrThrow("promoted")) == 1;
                int capacity = cursor.getInt(cursor.getColumnIndexOrThrow("kapacitet"));
                int attendingCount = cursor.getInt(cursor.getColumnIndexOrThrow("brojPrisutnih"));
                double averageRating = cursor.getDouble(cursor.getColumnIndexOrThrow("prosecnaOcena"));
                int ratingCount = cursor.getInt(cursor.getColumnIndexOrThrow("brojOcena"));

                Date date;
                try {
                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("datumVreme"));
                    date = sdf.parse(dateStr);
                } catch (Exception e) {
                    date = null;
                }

                Event event = new Event(name, description, location, date, category,
                        EventFactory.getImageForCategory(category), isPromoted, capacity,
                        attendingCount, averageRating, ratingCount);

                // Obavezno dodeljujemo ID koji je dodelila baza podataka
                event.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                list.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
    public Event getEventById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String upit = "SELECT * FROM events WHERE id = " + id;
        Cursor cursor = db.rawQuery(upit, null);

        if (cursor.moveToFirst()) {
            // IZMENA: Čitamo mongo_id iz baze
            String mongoId = cursor.getString(cursor.getColumnIndexOrThrow("mongo_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("naziv"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("opis"));
            String loc = cursor.getString(cursor.getColumnIndexOrThrow("lokacija"));
            String cat = cursor.getString(cursor.getColumnIndexOrThrow("kategorija"));
            //long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date_time"));
            //int img = cursor.getInt(cursor.getColumnIndexOrThrow("image_res_id"));
            int isPromotedInt = cursor.getInt(cursor.getColumnIndexOrThrow("promoted"));
            boolean isPromoted = (isPromotedInt == 1);
            int capacity = cursor.getInt(cursor.getColumnIndexOrThrow("kapacitet"));
            int attendingCount = cursor.getInt(cursor.getColumnIndexOrThrow("brojPrisutnih"));
            double averageRating = cursor.getDouble(cursor.getColumnIndexOrThrow("prosecnaOcena"));
            int ratingCount = cursor.getInt(cursor.getColumnIndexOrThrow("brojOcena"));


            Date date;
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("datumVreme"));
                date = sdf.parse(dateStr);
            } catch (Exception e) {
                date = null;
            }

            cursor.close();
            db.close();

            Event e = new Event(name, desc, loc, date, cat,
                    EventFactory.getImageForCategory(cat), isPromoted, capacity,
                    attendingCount, averageRating, ratingCount);
            e.setId(id);
            e.setMongoId(mongoId); // IZMENA: Dodeljujemo mongoId objektu
            return e;
        }

        cursor.close();
        db.close();
        return null;
    }
    public boolean updateEventRating(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("prosecnaOcena", event.getAverageRating());
        values.put("brojOcena", event.getRatingCount());

        int rowsAffected = db.update("events", values, "id = " + event.getId(), null);
        db.close();

        return rowsAffected > 0;
    }
    public int getLocalIdByMongoId(String mongoId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM events WHERE mongo_id = ?", new String[]{mongoId});
        int id = 0;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return id;
    }
}