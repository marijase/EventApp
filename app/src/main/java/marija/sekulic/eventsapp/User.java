package marija.sekulic.eventsapp;

import java.util.ArrayList;

public class User {
    public String username;
    public String email;
    public String password;

    public static ArrayList<User> global_list = new ArrayList<>();

    static {
        global_list.add(new User("admin", "admin@abc", "admin123"));
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}