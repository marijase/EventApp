package marija.sekulic.eventsapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;

public class UserRepository {

    private final UserDAO userDAO;
    private final String baseUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UserRepository(Context context) {
        this.userDAO = new UserDAO(context);
        this.baseUrl = ServerConfig.BASE_URL;
    }

    // ===== REGISTER =====
    // Online only - ne moze offline
    public void register(String username, String email, String password,
                         boolean isAdmin, RepositoryCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password); // server cuva plaintext, nasa lokalna baza hesuira
                body.put("email", email);
                body.put("isAdmin", isAdmin);

                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/users", body);

                if (response == null) {
                    mainHandler.post(() -> callback.onError("Nema konekcije sa serverom. Registracija nije moguća offline."));
                    return;
                }

                int status = response.optInt("_httpStatus", 0);

                if (status == 409) {
                    mainHandler.post(() -> callback.onError("Korisnik sa tim username-om već postoji."));
                    return;
                }

                if (status < 200 || status >= 300) {
                    mainHandler.post(() -> callback.onError("Greška pri registraciji."));
                    return;
                }

                // server vratio uspeh - citamo _id koji je MongoDB dodelio
                String serverId = response.optString("_id", "");

                // kesiramo lokalno (lozinka se hesuira pre upisa u SQLite)
                boolean localSaved = userDAO.registerUser(username, email, password);

                // cuvamo server _id u SharedPreferences (treba nam za attendance/ratings)
                // aktiviti ce ga sam sacuvati iz SharedPreferences nakon login-a
                // ovde ga prosledjujemo kroz callback kao deo poruke
                mainHandler.post(() -> callback.onResult(localSaved));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // ===== LOGIN =====
    // Online: server proverava -> kesira lokalno
    // Offline: pada na lokalnu bazu
    public void login(String username, String password, RepositoryCallback<JSONObject> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password);

                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/login", body);

                if (response == null) {
                    // OFFLINE - padamo na lokalnu bazu
                    User localUser = Validation.findByUsername(username); // koristimo Validation klasu
                    boolean localOk = localUser != null &&
                            PasswordHasher.verifyPassword(password, localUser.password);

                    if (localOk) {
                        // pravimo lazni JSON objekat sa lokalnim podacima za offline
                        JSONObject offlineResult = new JSONObject();
                        offlineResult.put("username", localUser.username);
                        offlineResult.put("email", localUser.email);
                        offlineResult.put("isAdmin", false); // offline ne znamo, default false
                        offlineResult.put("_id", "");
                        offlineResult.put("_offline", true);
                        mainHandler.post(() -> callback.onResult(offlineResult));
                    } else {
                        mainHandler.post(() -> callback.onError("Offline: pogrešni podaci ili korisnik ne postoji lokalno."));
                    }
                    return;
                }

                int status = response.optInt("_httpStatus", 0);

                if (status == 401) {
                    mainHandler.post(() -> callback.onError("Pogrešno korisničko ime ili lozinka."));
                    return;
                }

                if (status < 200 || status >= 300) {
                    mainHandler.post(() -> callback.onError("Greška pri prijavi."));
                    return;
                }

                // uspesno online - vracamo ceo response (sadrzi _id, username, email, isAdmin)
                mainHandler.post(() -> callback.onResult(response));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // ===== CHANGE PASSWORD =====
    // Online only
    public void changePassword(String username, String oldPassword, String newPassword,
                               RepositoryCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("oldPassword", oldPassword);
                body.put("newPassword", newPassword);

                JSONObject response = HttpHelper.putJSONObjectFromURL(baseUrl + "/password", body);

                if (response == null) {
                    mainHandler.post(() -> callback.onError("Nema konekcije. Promena lozinke nije moguća offline."));
                    return;
                }

                int status = response.optInt("_httpStatus", 0);

                if (status == 401) {
                    mainHandler.post(() -> callback.onError("Stara lozinka nije ispravna."));
                    return;
                }

                if (status < 200 || status >= 300) {
                    mainHandler.post(() -> callback.onError("Greška pri promeni lozinke."));
                    return;
                }

                // kesiramo novu lozinku lokalno
                userDAO.updateUserPassword(username, newPassword);
                mainHandler.post(() -> callback.onResult(true));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }
}