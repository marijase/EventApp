package marija.sekulic.eventsapp;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    private static final int SUCCESS = 200;

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        Log.d("HTTP GET", "JSON obj- " + jsonString);
        int responseCode = urlConnection.getResponseCode();
        urlConnection.disconnect();
        return responseCode == SUCCESS ? new JSONObject(jsonString) : null;
    }

    public static JSONArray getJSONArrayFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        Log.d("HTTP GET", "JSON arr- " + jsonString);
        int responseCode = urlConnection.getResponseCode();
        urlConnection.disconnect();
        return responseCode == SUCCESS ? new JSONArray(jsonString) : null;
    }

    public static JSONObject postJSONObjectFromURL(String urlString, JSONObject jsonObject) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
        os.writeBytes(jsonObject.toString());
        os.flush();
        os.close();
        int responseCode = urlConnection.getResponseCode();
        Log.i("STATUS", String.valueOf(responseCode));
        Log.i("MSG", urlConnection.getResponseMessage());

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        Log.d("HTTP POST", "JSON obj- " + jsonString);
        urlConnection.disconnect();
        return responseCode == SUCCESS ? new JSONObject(jsonString) : null;
    }

    public static JSONObject putJSONObjectFromURL(String urlString, JSONObject jsonObject) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            return null;
        }
        DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
        os.writeBytes(jsonObject.toString());
        os.flush();
        os.close();
        int responseCode = urlConnection.getResponseCode();
        Log.i("STATUS", String.valueOf(responseCode));
        Log.i("MSG", urlConnection.getResponseMessage());

        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        Log.d("HTTP PUT", "JSON obj- " + jsonString);
        urlConnection.disconnect();
        return responseCode == SUCCESS ? new JSONObject(jsonString) : null;
    }
}