package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AttendanceDAO {
    private final DatabaseHelper dbHelper;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public AttendanceDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public boolean setInterested(int userId, int eventId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT prisustvo FROM attendance WHERE userId = ? AND eventId = ?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)});

        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false; // Korisnik već ima zabeležen status za ovaj događaj
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("eventId", eventId);
        values.put("prisustvo", "ZAINTERESOVAN");

        long result = db.insert("attendance", null, values);
        db.close();
        return result != -1;
    }

    public String setAttending(int userId, int eventId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor eventCursor = db.rawQuery("SELECT promoted, kapacitet, brojPrisutnih FROM events WHERE id = ?",
                new String[]{String.valueOf(eventId)});

        if (eventCursor.moveToFirst()) {
            int promoted = eventCursor.getInt(0);
            int kapacitet = eventCursor.getInt(1);
            int brojPrisutnih = eventCursor.getInt(2);

            if (promoted == 1 && brojPrisutnih >= kapacitet) {
                eventCursor.close();
                db.close();
                return "Kapacitet je popunjen!";
            }
        }
        eventCursor.close();

        // Provera postojećeg zapisa u attendance (Prelazak sa INTERESTED na ATTENDING)
        Cursor attCursor = db.rawQuery("SELECT prisustvo FROM attendance WHERE userId = ? AND eventId = ?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)});

        ContentValues values = new ContentValues();
        values.put("prisustvo", "PRISUSTVUJE");

        if (attCursor.moveToFirst()) {
            String trenutniStatus = attCursor.getString(0);
            if (trenutniStatus.equals("PRISUSTVUJE")) {
                attCursor.close();
                db.close();
                return "Već ste prijavljeni za ovaj događaj!";
            }
            // Update postojećeg zapisa
            db.update("attendance", values, "userId = ? AND eventId = ?",
                    new String[]{String.valueOf(userId), String.valueOf(eventId)});
        } else {
            // Čisti unos
            values.put("userId", userId);
            values.put("eventId", eventId);
            db.insert("attendance", null, values);
        }
        attCursor.close();

        db.execSQL("UPDATE events SET brojPrisutnih = brojPrisutnih + 1 WHERE id = ?", new Object[]{eventId});
        db.close();
        return "Uspešno ste se prijavili!";
    }

    public List<Event> getAttendingEvents(int userId) {
        String query = "SELECT e.* FROM events e INNER JOIN attendance a ON e.id = a.eventId " +
                "WHERE a.userId = ? AND a.prisustvo = 'PRISUSTVUJE'";
        return getEventsFromJoin(query, new String[]{String.valueOf(userId)});
    }

    public List<Event> getInterestedEvents(int userId) {
        String query = "SELECT e.* FROM events e INNER JOIN attendance a ON e.id = a.eventId " +
                "WHERE a.userId = ? AND a.prisustvo = 'ZAINTERESOVAN'";
        return getEventsFromJoin(query, new String[]{String.valueOf(userId)});
    }

    private List<Event> getEventsFromJoin(String sql, String[] args) {
        List<Event> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("naziv"));
                String opis = cursor.getString(cursor.getColumnIndexOrThrow("opis"));
                String lokacija = cursor.getString(cursor.getColumnIndexOrThrow("lokacija"));
                String kategorija = cursor.getString(cursor.getColumnIndexOrThrow("kategorija"));
                boolean isPromoted = cursor.getInt(cursor.getColumnIndexOrThrow("promoted")) == 1;
                int kapacitet = cursor.getInt(cursor.getColumnIndexOrThrow("kapacitet"));
                int brojPrisutnih = cursor.getInt(cursor.getColumnIndexOrThrow("brojPrisutnih"));
                double prosecnaOcena = cursor.getDouble(cursor.getColumnIndexOrThrow("prosecnaOcena"));
                int brojOcena = cursor.getInt(cursor.getColumnIndexOrThrow("brojOcena"));

                Date date;
                try {
                    date = sdf.parse(cursor.getString(cursor.getColumnIndexOrThrow("datumVreme")));
                } catch (Exception e) {
                    date = null;
                }

                Event event = new Event(name, opis, lokacija, date, kategorija,
                        EventFactory.getImageForCategory(kategorija), isPromoted,
                        kapacitet, brojPrisutnih, prosecnaOcena, brojOcena);
                event.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                list.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}