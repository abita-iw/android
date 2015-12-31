package com.example.aqeelp.abita;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by aqeelp on 12/28/15.
 */
public class PinPost extends AsyncTask<String, Void, String> {
    final private String[] PINTYPESTRINGS = { "Wildlife", "Foliage", "Scenery", "Architecture" };
    final private ArrayList<String> PINTYPES =
            new ArrayList<String>(Arrays.asList(PINTYPESTRINGS));
    JSONObject json;
    String newPinType;
    int newPinTypeIndex;
    LatLng location;
    String newPinTitle;
    String newPinCaption;
    Bitmap preview;
    MapsActivity CONTEXT;

    public PinPost(String npt, int npti, LatLng loc, String nptit, String npc, Bitmap prev, MapsActivity c) {
        newPinType = npt;
        newPinTypeIndex = npti;
        location = loc;
        newPinTitle = nptit;
        newPinCaption = npc;
        preview = prev;
        CONTEXT = c;

        trustEveryone();

        // Create the JSON object for this pin
        json = makePinJSON();
        Log.v("PinPost", "Pin JSON constructed");
        if (json == null) return;
    }

    protected String doInBackground(String... params) {
        // Issue post request
        try {

            Log.v("PinPost", "Issuing pin post request...");

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            HttpRequest.post(params[0])
                    .contentType(HttpRequest.CONTENT_TYPE_JSON)
                    .send(json.toString())
                    .receive(result);

            Log.v("PinPost", "Completed.");
            return result.toString();

        } catch (HttpRequest.HttpRequestException e) {
            Log.v("PinPost", "Exception: " + e.toString());
        }
        return null;
    }

    protected void onPostExecute(String response) {
        Log.v("PinPost", "Response received: " + response);

        try {
            JSONObject newPinReceivedJSON = new JSONObject(response);

            Pin pin = new Pin(newPinReceivedJSON.getInt("pinId"),
                    newPinReceivedJSON.getInt("userId"),
                    newPinReceivedJSON.getString("pinType"),
                    new LatLng(newPinReceivedJSON.getDouble("latitude"), newPinReceivedJSON.getDouble("longitude")),
                    newPinReceivedJSON.getString("title"),
                    newPinReceivedJSON.getString("dateCreated"),
                    newPinReceivedJSON.getString("dateModified"),
                    CONTEXT);

            // Set and upload image (if necessary)
            if (preview != null) {
                // TODO: upload image
                pin.setThumbnail(preview);
            }

            Log.v("PinPost", "Found a thumbnail and set it");

            // Set and upload caption (if necessary)
            if (newPinCaption != null) {
                Description description = new Description(pin.getPinId(), 18, pin.getPinId(),
                        newPinCaption, "", "", CONTEXT);
                Description[] descriptions = new Description[1];
                descriptions[0] = description;
                pin.setPinDescriptions(descriptions);
                // TODO upload description
            }

            Log.v("PinPost", "Made descriptions and added them");

            CONTEXT.addNewPin(pin);

            Log.v("PinPost", "Pin creation complete");

            Toast.makeText(pin.getParent(), "Pin posted to database", Toast.LENGTH_LONG);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject makePinJSON() {
        JSONObject pinJSON = new JSONObject();

        try {
            pinJSON.put("userId", 18); // TODO: global user
            pinJSON.put("typeId", newPinTypeIndex + 1);
            pinJSON.put("latitude", location.latitude);
            pinJSON.put("longitude", location.longitude);
            pinJSON.put("title", newPinTitle);

            return pinJSON;
        } catch (JSONException e) {
            Log.v("PinPost", "JSON Exception");
        }

        return null;
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
