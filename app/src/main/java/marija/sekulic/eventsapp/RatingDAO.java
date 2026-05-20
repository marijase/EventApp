package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RatingDAO {
    private final DatabaseHelper dbHelper;

    public RatingDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public boolean leaveRating(int userId, int eventId, int rating) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("eventId", eventId);
        values.put("rating", rating);

        long result = db.insert("ratings", null, values);
        if (result == -1) {
            db.close();
            return false; // Korisnik je već ocenio ovaj događaj (UNIQUE aktiviran)
        }

        // Formula za proračun prosečne ocene i ažuriranje tabele events
        Cursor cursor = db.rawQuery("SELECT COUNT(*), AVG(rating) FROM ratings WHERE eventId = ?",
                new String[]{String.valueOf(eventId)});

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            double avg = cursor.getDouble(1);

            ContentValues updateValues = new ContentValues();
            updateValues.put("brojOcena", count);
            updateValues.put("prosecnaOcena", avg);

            db.update("events", updateValues, "id = ?", new String[]{String.valueOf(eventId)});
        }
        cursor.close();
        db.close();
        return true;
    }
}