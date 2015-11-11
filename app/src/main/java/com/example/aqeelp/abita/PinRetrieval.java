package com.example.aqeelp.abita;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.GridView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by aqeelp on 11/10/15.
 */
public class PinRetrieval extends AsyncTask<String, Void, String> {
    private MapsActivity mapsActivity;

    public PinRetrieval(MapsActivity m) {
        Log.v("Async_task", "Instantiated");
        mapsActivity = m;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.v("Async_task", "Do in background - Attempting to get data url");
        try {
            return get(new URL(params[0]));
        } catch (Exception e) {
            Log.v("Async_task", "Do in background - Failed to retrieve properly" + e.toString());
            return null;
        }
    }

    protected void onPostExecute(String data) {
        Log.v("Async_task", "On Post Execute - Attempting to create pins from data");
        try {
            Pin[] pinsRetrieved = parseData(data);
            mapsActivity.clearPins();
            for (int i = 0; i < pinsRetrieved.length; i++) {
                mapsActivity.addNewPin(pinsRetrieved[i]);
            }
        } catch (Exception e) {
            Log.v("Async_task", "On Post Execute - Failed to parse JSON properly");
        }
    }

    private Pin[] parseData(String data) throws JSONException {
        try {
            // getting JSON string from URL
            JSONArray pinsRawJSON = new JSONArray(data);
            Pin[] pins = new Pin[pinsRawJSON.length()];

            for (int i = 0; i < pinsRawJSON.length(); i++) {
                JSONObject pin = pinsRawJSON.getJSONObject(i);
                int pinId = pin.getInt("pinId");
                int userId = pin.getInt("userId");
                String pinType = pin.getString("pinType");
                String caption = pin.getString("caption");
                double latitude = pin.getDouble("latitude");
                double longitude = pin.getDouble("longitude");
                String dateCreated = pin.getString("dateCreated");
                String dateModified = pin.getString("dateModified");

                // TODO: temporary for pins without real locations
                Location location = mapsActivity.getMostRecentLocation();

                pins[i] = new Pin(pinId, pinType,
                        new LatLng(location.getLatitude() + (Math.random() * 0.006),
                                location.getLongitude() + (Math.random() * 0.006)),
                        caption, mapsActivity);
            }

            return pins;
        } catch (Exception e) {
            Log.v("Async task", "On Post Execute - Failed to parse JSON properly");
        }
        return null;
    }

    private String get(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

        try {
            byte[] response = readStream(in);
            return new String(response, "UTF-8");
        } finally {
            in.close();
        }
    }

    private byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }
}
