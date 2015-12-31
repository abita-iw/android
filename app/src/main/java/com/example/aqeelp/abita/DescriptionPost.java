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
public class DescriptionPost extends AsyncTask<String, Void, String> {
    JSONObject json;
    Pin pin;
    String text;

    public DescriptionPost(Pin p, String t) {
        pin = p;
        text = t;

        trustEveryone();

        // Create the JSON object for this pin
        json = makeDescriptionJSON();
        Log.v("DescriptionPost", "Description JSON constructed");
        if (json == null) return;
    }

    protected String doInBackground(String... params) {
        // Issue post request
        try {

            Log.v("DescriptionPost", "Issuing description post request...");

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            HttpRequest.post(params[0])
                    .contentType(HttpRequest.CONTENT_TYPE_JSON)
                    .send(json.toString())
                    .receive(result);

            Log.v("DescriptionPost", "Completed.");
            return result.toString();

        } catch (HttpRequest.HttpRequestException e) {
            Log.v("DescriptionPost", "Exception: " + e.toString());
        }
        return null;
    }

    protected void onPostExecute(String response) {
        Log.v("DescriptionPost", "Response received: " + response);

        try {
            JSONObject receivedJSON = new JSONObject(response);

            Description description = new Description(receivedJSON.getInt("descriptionId"),
                    receivedJSON.getInt("userId"),
                    receivedJSON.getInt("pinId"),
                    receivedJSON.getString("text"),
                    receivedJSON.getString("dateCreated"),
                    receivedJSON.getString("dateModified"),
                    pin.getParent());

            Description[] descriptions = new Description[1];
            descriptions[0] = description;
            pin.setPinDescriptions(descriptions);

            Log.v("DescriptionPost", "Made description and added it");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject makeDescriptionJSON() {
        JSONObject pinJSON = new JSONObject();

        try {
            pinJSON.put("userId", 18); // TODO: global user
            pinJSON.put("pinId", pin.getPinId());
            pinJSON.put("text", text);

            return pinJSON;
        } catch (JSONException e) {
            Log.v("DescriptionPost", "JSON Exception");
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
