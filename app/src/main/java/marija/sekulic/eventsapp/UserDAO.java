package marija.sekulic.eventsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDAO {
    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

     //Metoda za registraciju novog korisnika.
     //Lozinka se hešira pre upisa u bazu podataka
    public boolean registerUser(String username, String email, String password) {
        //  proverava da li korisnik sa tim imenom ili emailom postoji u bazi
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String proveraUpit = "SELECT id FROM users WHERE username = '" + username + "' OR email = '" + email + "'";
        Cursor cursor = db.rawQuery(proveraUpit, null);

        if (cursor.getCount() > 0) { // Ako je u bazi pronađen barem jedan korisnik sa tim podacima
            cursor.close();
            db.close();
            return false; // Korisnik već postoji
        }
        cursor.close();

        // Heširanje lozinke
        String hashedPassword = PasswordHasher.hashPassword(password);

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", hashedPassword);

        long result = db.insert("users", null, values);
        db.close();

        if (result == -1) {
            return false; // Greška pri upisu
        } else {
            return true;  // Uspešno upisan
        }
    }

    // Metoda za proveru prijave (Login).
    // Uzima heš iz baze i poredi ga sa unetom lozinkom
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // traži lozinku za uneto korisničko ime
        String loginUpit = "SELECT password FROM users WHERE username = '" + username + "'";
        Cursor cursor = db.rawQuery(loginUpit, null);
        if (cursor.moveToFirst()) {
            // Izvlačimo hešovanu lozinku iz baze uz getColumnIndexOrThrow
            int indeksKolone = cursor.getColumnIndexOrThrow("password");
            String hashedPasswordFromDb = cursor.getString(indeksKolone);

            cursor.close();
            db.close();

            // Heširamo ono što je korisnik upravo ukucao u polje za login
            // String hashedInputPassword = PasswordHasher.hashPassword(password);

            // poredimo hesovanu sa unetom
            if (PasswordHasher.verifyPassword(password, hashedPasswordFromDb)) {
                return true;  // Lozinka je tačna
            } else {
                return false; // Lozinka je pogrešna
            }
        } else {
            // Ako cursor uopšte nije mogao da stane na prvi element, korisnik ne postoji
            cursor.close();
            db.close();
            return false;
        }
    }

    // Metoda koja pronalazi i vraća email korisnika iz baze.
    public String getEmailByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String upit = "SELECT email FROM users WHERE username = '" + username + "'";
        Cursor cursor = db.rawQuery(upit, null);

        if (cursor.moveToFirst()) {
            int indeksKolone = cursor.getColumnIndexOrThrow("email");
            String pronadjeniEmail = cursor.getString(indeksKolone);

            cursor.close();
            db.close();
            return pronadjeniEmail; // Vraćamo pravi email iz baze
        } else {
            cursor.close();
            db.close();
            return "Not Available";
        }
    }
    // Metoda koja hešira novu lozinku i upisuje je u bazu
    public boolean updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
        values.put("password", hashedNewPassword);

        // Menjamo red u tabeli users gde je username jednak ulogovanom korisniku
        int rows = db.update("users", values, "username = '" + username + "'", null);

        db.close();

        return rows > 0;
    }
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?",
                new String[]{username});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            db.close();
            return id;
        }
        cursor.close();
        db.close();
        return -1;
    }
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?",
                new String[]{username});
        if (cursor.moveToFirst()) {
            String uname = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            cursor.close();
            db.close();
            return new User(uname, email, password);
        }
        cursor.close();
        db.close();
        return null;
    }
}