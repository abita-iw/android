package com.example.aqeelp.abita;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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
public class PinPost {
    final private String[] PINTYPESTRINGS = { "Wildlife", "Foliage", "Scenery", "Architecture" };
    final private ArrayList<String> PINTYPES =
            new ArrayList<String>(Arrays.asList(PINTYPESTRINGS));

    public PinPost(Pin pin) {
        trustEveryone();

        // Create the JSON object for this pin
        JSONObject json = makePinJSON(pin);
        Log.v("PinPost", "Pin JSON constructed");
        if (json == null) return;

        URL url;
        URLConnection urlConn;
        DataOutputStream printout;
        DataInputStream input;
        try {
            // Setup connection
            url = new URL("https://api.abitatech.net:5000/api/pins");
            urlConn = url.openConnection();
            Log.v("PinPost", "URL opened");
            // urlConn.setDoInput(true);
            // urlConn.setDoOutput(true);
            // urlConn.setUseCaches(false);
            // urlConn.setRequestProperty("Content-Type", "application/json");
            //urlConn.setRequestProperty("Host", "android.schoolportal.gr");
            urlConn.connect();
            Log.v("PinPost", "URL connected");

            // Send POST output.
            printout = new DataOutputStream(urlConn.getOutputStream ());
            byte[] jsonByteStream = URLEncoder.encode(json.toString(), "UTF-8").getBytes();
            printout.write(jsonByteStream, 0, jsonByteStream.length);
            Log.v("PinPost", "Written");
            printout.flush();
            printout.close();
            Log.v("PinPost", "Closed");
        } catch (MalformedURLException e) {
            Log.v("PinPost", "Malformed URL Exception");
        } catch (IOException e) {
            Log.v("PinPost", "IO Exception");
        }
    }

    private JSONObject makePinJSON(Pin pin) {
        JSONObject pinJSON = new JSONObject();

        try {
            pinJSON.put("userId", 1);
            pinJSON.put("typeId", PINTYPES.indexOf(pin.getPinType()));
            pinJSON.put("latitude", pin.getPinLocation().latitude);
            pinJSON.put("longitude", pin.getPinLocation().longitude);
            pinJSON.put("title", pin.getPinTitle());

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
