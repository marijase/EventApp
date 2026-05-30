package marija.sekulic.eventsapp;

import java.util.regex.Pattern;

public class Validation {

    // ---------- REGISTER ----------

    public static boolean isUsernameValid(String username) {
        return username.length() > 0;
    }

    public static boolean isUsernameTaken(String username) {
        for (User u : User.global_list) {
            if (u.username.equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public static boolean isEmailValid(String email) {
        String pattern = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        return Pattern.matches(pattern, email);
    }

    public static boolean isEmailTaken(String email) {
        for (User u : User.global_list) {
            if (u.email.equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 8;
    }

    // vraca 0 = weak, 1 = good, 2 = strong
    public static int getPasswordStrength(String password) {
        if (password.length() == 0) return -1; // nista nije uneseno
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*");
        boolean longEnough = password.length() > 8;

        int score = 0;
        if (longEnough) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;

        if (score <= 1) return 0; // weak
        if (score == 2 || score == 3) return 1; // good
        return 2; // strong
    }

    // ---------- LOGIN ----------

    // vraca User objekat ako postoji, null ako ne postoji
    public static User findByUsername(String username) {
        for (User u : User.global_list) {
            if (u.username.equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public static boolean isPasswordCorrect(User user, String password) {
        return user.password.equals(password);
    }
}