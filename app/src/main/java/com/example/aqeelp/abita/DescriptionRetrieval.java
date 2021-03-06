package com.example.aqeelp.abita;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
public class DescriptionRetrieval extends AsyncTask<String, Void, String> {
    private Pin pin;
    private MapsActivity activity;

    public DescriptionRetrieval(Pin p, MapsActivity m) {
        Log.v("Async_task", "Instantiated description retrieval for pin " + p.getPinId());
        pin = p;
        activity = m;
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
        Log.v("Async_task", "On Post Execute - Attempting to create descriptions from data");
        Log.v("Async_task", data);
        try {
            InputStream stream = new ByteArrayInputStream(data.getBytes("UTF-8"));
            ArrayList<Description> descriptionsRetrieved = readJsonStream(stream);
            Description[] pinDescriptions = new Description[descriptionsRetrieved.size()];
            for (int i = 0; i < descriptionsRetrieved.size(); i++) {
                Description description = descriptionsRetrieved.get(i);
                User user = activity.findUserById(description.getUserId());
                if (user != null) {
                    description.setPinUser(user);
                    Log.v("Async_task", "User was found.. " + user.getUserId());
                } else {
                    description.fetchUser();
                }

                pinDescriptions[i] = description;
            }
            Log.v("Async_task", "Array made from descriptions");
            pin.setPinDescriptions(pinDescriptions);
        } catch (IOException e) {
            Log.v("Async_task", "On Post Execute - Failed to parse JSON properly");
        }
    }

    public ArrayList<Description> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        ArrayList<Description> descriptions = null;
        try {
            descriptions = readMessagesArray(reader);
        } catch (Exception e) {
            Log.v("Async_task", "Description: Read JSON Stream Failed");
        } finally {
            Log.v("Async_task", descriptions.size() + " descriptions found");
            reader.close();
            Log.v("Async_task", "description reader close");
            return descriptions;
        }
    }

    public ArrayList<Description> readMessagesArray(JsonReader reader) throws IOException {
        ArrayList<Description> messages = new ArrayList<Description>();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readMessage(reader));
        }
        reader.endArray();
        return messages;
    }

    public Description readMessage(JsonReader reader) throws IOException {
        int descriptionId = -1;
        int userId = -1;
        int pinId = pin.getPinId();
        String text = null;
        String createdAt = null;
        String modifiedAt = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("descriptionId")) {
                descriptionId = reader.nextInt();
            } else if (name.equals("userId")) {
                userId = reader.nextInt();
            } else if (name.equals("pinId")) {
                pinId = reader.nextInt();
            } else if (name.equals("text")) {
                text = reader.nextString();
            } else if (name.equals("dateCreated")) {
                createdAt = reader.nextString();
            } else if (name.equals("dateModified")) {
                modifiedAt = reader.nextString();
            }
        }
        reader.endObject();

        return new Description(descriptionId, userId, pinId,
                text, createdAt, modifiedAt, activity);
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
