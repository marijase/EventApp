package marija.sekulic.eventsapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;

public class RatingRepository {

    private final RatingDAO ratingDAO;
    private final String baseUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RatingRepository(Context context) {
        this.ratingDAO = new RatingDAO(context);
        this.baseUrl = ServerConfig.BASE_URL;
    }

    // ===== LEAVE RATING =====
    // Online only
    public void leaveRating(String serverUserId, String serverEventId,
                            int localUserId, int localEventId,
                            int rating, RepositoryCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("userId", serverUserId);
                body.put("eventId", serverEventId);
                body.put("rating", rating);

                JSONObject response = HttpHelper.postJSONObjectFromURL(baseUrl + "/ratings", body);

                if (response == null) {
                    mainHandler.post(() -> callback.onError("Nema konekcije. Ocenjivanje nije moguće offline."));
                    return;
                }

                int status = response.optInt("_httpStatus", 0);

                if (status == 409) {
                    mainHandler.post(() -> callback.onError("Već ste ocenili ovaj događaj."));
                    return;
                }

                if (status < 200 || status >= 300) {
                    mainHandler.post(() -> callback.onError("Greška pri ocenjivanju."));
                    return;
                }

                // server vraca azurirani event sa novim avgRating i numberOfRatings
                // kesiramo lokalno
                ratingDAO.leaveRating(localUserId, localEventId, rating);

                mainHandler.post(() -> callback.onResult(true));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Greška: " + e.getMessage()));
            }
        }).start();
    }
}