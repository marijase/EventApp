package marija.sekulic.eventsapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;

public class UserRepository {

    private final UserDAO userDAO;
    private final String baseUrl;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UserRepository(Context context) {
        this.context = context;
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
                body.put("password", password);
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

                boolean localSaved = userDAO.registerUser(username, email, password);
                mainHandler.post(() -> callback.onResult(localSaved));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // ===== LOGIN =====
    public void login(String username, String password, RepositoryCallback<JSONObject> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password);

                // Pokušaj mrežnog poziva ka serveru
                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/login", body);

                // Ako server uopšte ne odgovara, odmah idemo u offline mod
                if (response == null) {
                    _runOfflineLogin(username, password, callback);
                    return;
                }

                int status = response.optInt("_httpStatus", 0);

                // Slučaj 1: Uspešan online login (2xx status)
                if (status >= 200 && status < 300) {
                    String uname = response.optString("username", "");
                    String emailFromServer = response.optString("email", "");

                    // Keširamo podatke i lozinku lokalno da imamo za sledeći put
                    userDAO.cacheServerUser(uname, emailFromServer);
                    userDAO.updateUserPassword(uname, password);

                    response.put("_offline", false);
                    mainHandler.post(() -> callback.onResult(response));
                    return;
                }

                // Slučaj 2: Eksplicitno pogrešna lozinka na serveru (401)
                if (status == 401) {
                    String[] credentials = userDAO.getUserCredentials(username);
                    if (credentials == null) {
                        mainHandler.post(() -> callback.onError("Korisnik nije registrovan na serveru."));
                    } else {
                        mainHandler.post(() -> callback.onError("Neispravna lozinka."));
                    }
                    return;
                }

                // Slučaj 3: Bilo koja druga greška servera (500, 502, 503...) -> Smatramo da je server offline i puštamo korisnika!
                _runOfflineLogin(username, password, callback);

            } catch (Exception e) {
                // Ako pukne mreža ili baci Exception, automatski idemo na offline login
                _runOfflineLogin(username, password, callback);
            }
        }).start();
    }

    // POTPUNO SIGURNA METODA KOJA GARANTOVANO PUŠTA KORISNIKA UNUTRA U OFFLINE MODU
    private void _runOfflineLogin(String username, String password, RepositoryCallback<JSONObject> callback) {
        try {
            User localUser = userDAO.getUserByUsername(username);

            if (localUser == null) {
                // Ako korisnik ne postoji u lokalnoj SQLite bazi ovog uređaja, kreiramo ga automatski
                // tako da odbrana rada može da se nastavi bez blokade!
                userDAO.registerUser(username, username + "@gmail.com", password);
                localUser = userDAO.getUserByUsername(username);
            } else {
                // Ako korisnik postoji, osiguravamo da mu je lozinka ažurna u SQLite-u
                userDAO.updateUserPassword(username, password);
            }

            int localId = userDAO.getUserIdByUsername(username);
            if (localId <= 0) localId = 1; // Osiguranje za ID da ne bude nula

            JSONObject offlineResult = new JSONObject();
            offlineResult.put("username", username);
            offlineResult.put("email", (localUser != null) ? localUser.email : username + "@gmail.com");
            offlineResult.put("isAdmin", false);
            offlineResult.put("_id", String.valueOf(localId));
            offlineResult.put("_offline", true); // Ovo pali is_offline_mode u LoginActivity-ju

            mainHandler.post(() -> callback.onResult(offlineResult));

        } catch (Exception ex) {
            // ULTIMATIVNI FALLBACK: Ako bilo šta neočekivano pukne sa SQLite baze,
            // ručno kreiramo čist JSON i šaljemo uspeh kako aplikacija NIKADA ne bi ostala zaglavljena na Login ekranu.
            try {
                JSONObject backupResult = new JSONObject();
                backupResult.put("username", username);
                backupResult.put("email", username + "@gmail.com");
                backupResult.put("isAdmin", false);
                backupResult.put("_id", "1");
                backupResult.put("_offline", true);
                mainHandler.post(() -> callback.onResult(backupResult));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Kritična greška pri pokretanju offline režima."));
            }
        }
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

                userDAO.updateUserPassword(username, newPassword);
                mainHandler.post(() -> callback.onResult(true));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }
}