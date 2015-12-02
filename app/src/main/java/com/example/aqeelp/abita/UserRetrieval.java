package com.example.aqeelp.abita;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by aqeelp on 11/10/15.
 */
public class UserRetrieval extends AsyncTask<String, Void, String> {
    private Pin pin;
    private Description description;
    private boolean pinCalled;

    public UserRetrieval(Pin p, Description d) {
        if (p != null) {
            pin = p;
            Log.v("Async_task", "Instantiated user retrieval for pin " + p.getPinId());
            pinCalled = true;
        } else {
            description = d;
            Log.v("Async_task", "Instantiated user retrieval for description " + d.getDescriptionId());
            pinCalled = false;
        }

        trustEveryone();
    }

    @Override
    protected String doInBackground(String... params) {
        Log.v("Async_task", "Do in background - Attempting to get data url " + params[0]);
        try {
            return get(new URL(params[0]));
        } catch (Exception e) {
            Log.v("Async_task", "Do in background - Failed to retrieve properly" + e.toString());
            return null;
        }
    }

    protected void onPostExecute(String data) {
        Log.v("Async_task", "On Post Execute - Attempting to create pins from data");
        Log.v("Async_task", data);
        try {
            InputStream stream = new ByteArrayInputStream(data.getBytes("UTF-8"));
            User userReceived = readJsonStream(stream);
            if (pinCalled)
                pin.setPinUser(userReceived);
            else
                description.setDescriptionUser(userReceived);
        } catch (IOException e) {
            Log.v("Async_task", "On Post Execute - Failed to parse JSON properly");
        }
    }

    public User readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        User user = null;
        try {
            user = readMessage(reader);
        } catch (Exception e) {
            Log.v("Async_task", "Read JSON Stream - Failed");
        } finally {
            reader.close();
            return user;
        }
    }

    public User readMessage(JsonReader reader) throws IOException {
        int userId = -1;
        String email = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("userId")) {
                userId = reader.nextInt();
            } else if (name.equals("email")) {
                email = reader.nextString();
            }
        }
        reader.endObject();

        return new User(userId, email, email);
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

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
