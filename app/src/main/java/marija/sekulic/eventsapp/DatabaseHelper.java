package marija.sekulic.eventsapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String db_name = "EventsApp.db";
    private static final int db_version = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, db_name, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table users (id integer primary key autoincrement, " +
                "                  username text unique not null, " +
                "                   email   text unique not null, " +
                "                   lozinka text not null )");          // hesirana lozinka

        db.execSQL("create table events (id integer primary key autoincrement, " +
                "                       naziv text not null, " +
                "                       opis text, " +
                "                    lokacija text not null, " +
                "                  datumVreme text not null, " +
                "                  kategorija text not null, " +
                "               promoted INTEGER DEFAULT 0 CHECK (promoted IN (0, 1)), " +
                "              kapacitet INTEGER DEFAULT 0 CHECK (kapacitet >= 0), " +
                "          brojPrisutnih INTEGER DEFAULT 0 CHECK (brojPrisutnih >= 0), " +
                "             prosecnaOcena REAL DEFAULT 0 CHECK (prosecnaOcena BETWEEN 0 AND 5), " +
                "              brojOcena INTEGER DEFAULT 0 CHECK (brojOcena >= 0), " +

                // Ova dva pravila moraju ostati na kraju jer porede dve različite kolone

                "       CHECK (promoted = 0 OR kapacitet > 0), " +
                "       CHECK (promoted = 0 OR brojPrisutnih <= kapacitet));");

        db.execSQL("CREATE TABLE attendance (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "   userId INTEGER NOT NULL, " +
                "  eventId INTEGER NOT NULL, " +
                "   prisustvo TEXT NOT NULL CHECK (prisustvo IN ('ZAINTERESOVAN', 'PRISUSTVUJE')), " +

 // Ova pravila se razlikuju od CHECK-a jer se bave povezivanjem tabela i sprečavanjem dupliranja, a ne proverom tačnosti

                "   FOREIGN KEY(userId) REFERENCES users(id), " +   // služi kao "pokazivac" ka primary key (id)
                "   FOREIGN KEY(eventId) REFERENCES events(id), " +
                "   UNIQUE(userId, eventId));");

        db.execSQL("CREATE TABLE ratings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER NOT NULL, " +
                "eventId INTEGER NOT NULL, " +
                "rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5), " +
                "FOREIGN KEY(userId) REFERENCES users(id), " +
                "FOREIGN KEY(eventId) REFERENCES events(id), " +
                "UNIQUE(userId, eventId));");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop TABLE IF EXISTS ratings");
        db.execSQL("drop TABLE IF EXISTS attendance");
        db.execSQL("drop TABLE IF EXISTS events");
        db.execSQL("drop TABLE IF EXISTS users");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

}
