package com.example.aqeelp.abita;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
public class PinPost extends AsyncTask<String, Void, Integer> {
    final private String[] PINTYPESTRINGS = { "Wildlife", "Foliage", "Scenery", "Architecture" };
    final private ArrayList<String> PINTYPES =
            new ArrayList<String>(Arrays.asList(PINTYPESTRINGS));
    JSONObject json;
    Pin pin;

    public PinPost(Pin p) {
        pin = p;

        trustEveryone();

        // Create the JSON object for this pin
        json = makePinJSON(pin);
        Log.v("PinPost", "Pin JSON constructed");
        if (json == null) return;
    }

    protected Integer doInBackground(String... params) {
        // Issue post request
        int response = -1;
        try {

            Log.v("PinPost", "Issuing request...");
            //Log.v("PinPost", URLEncoder.encode(json.toString(), "UTF-8"));
            //response = HttpRequest.post(params[0])
             //       .send(URLEncoder.encode(json.toString(), "UTF-8")).code();

            // TODO String sendString = "userId=" + pin.getUserId();
            String sendString = "userId=" + 18;
            sendString += "&typeId=" + PINTYPES.indexOf(pin.getPinType());
            sendString += "&latitude=" + pin.getPinLocation().latitude;
            sendString += "&longitude=" + pin.getPinLocation().longitude;
            sendString += "&title=\"" + pin.getPinTitle() + "\"";

            Log.v("PinPost", sendString);

            response = HttpRequest.post(params[0]).send(sendString).code();
            Log.v("PinPost", "Completed.");
            return response;

        } catch (Exception e) {

        }
        return response;

        /*try {
            // Setup connection
            URL url = new URL("https://api.abitatech.net:5000/api/pins");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            Log.v("PinPost", "URL opened");

            // Send POST output
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            //OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            Log.v("PinPost", "Writer init'd");
            byte[] jsonString = URLEncoder.encode(json.toString(), "UTF-8").getBytes();
            Log.v("PinPost", "Created byte stream");
            out.write(jsonString, 0, jsonString.length);
            Log.v("PinPost", "Written");

            out.flush();
            out.close();
            Log.v("PinPost", "Flushed and closed");
        } catch (MalformedURLException e) {
            Log.v("PinPost", "Malformed URL Exception");
        } catch (IOException e) {
            Log.v("PinPost", "IO Exception");
        }*/
    }

    protected void onPostExecute(Integer response) {
        Log.v("PinPost", "Response received: " + response);
    }

    private JSONObject makePinJSON(Pin pin) {
        JSONObject pinJSON = new JSONObject();

        try {
            pinJSON.put("userId", 18); // TODO: dynamic user
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
