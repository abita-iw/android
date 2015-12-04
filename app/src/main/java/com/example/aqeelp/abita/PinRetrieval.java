package com.example.aqeelp.abita;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
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
import java.nio.charset.StandardCharsets;
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
public class PinRetrieval extends AsyncTask<String, Void, String> {
    private MapsActivity mapsActivity;

    public PinRetrieval(MapsActivity m) {
        Log.v("Async_task", "Instantiated unbounded pin retrieval");
        mapsActivity = m;
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
            ArrayList<Pin> pinsRetrieved = readJsonStream(stream);
            //mapsActivity.clearPins();
            for (int i = 0; i < pinsRetrieved.size(); i++) {
                mapsActivity.addNewPin(pinsRetrieved.get(i));
            }
        } catch (IOException e) {
            Log.v("Async_task", "On Post Execute - Failed to parse JSON properly");
        }
    }

    public ArrayList<Pin> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        ArrayList<Pin> pins = null;
        try {
            pins = readPinsArray(reader);
            Log.v("Async_task", "post readPinsArray");
        } catch (Exception e) {
            Log.v("Async_task", "Pin: 75 " + e.toString());
        } finally {
            reader.close();
            Log.v("Async_task", "reader close");
            return pins;
        }
    }

    public ArrayList<Pin> readPinsArray(JsonReader reader) throws IOException {
        ArrayList<Pin> pins = new ArrayList<Pin>();

        reader.beginArray();
        while (reader.hasNext()) {
            Pin nextPin = readPin(reader);
            if (nextPin != null) // Will be null if the Pin is already found
                pins.add(nextPin);
        }

        reader.endArray();
        return pins;
    }

    public Pin readPin(JsonReader reader) throws IOException {
        int pinId = -1;
        int userId = -1;
        String pinType = null;
        String title = null;
        double latitude = 0.0;
        double longitude = 0.0;
        String dateCreated = null;
        String dateModified = null;
        Description[] descriptions = new Description[0];

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            Log.v("Async_task", name);
            if (name.equals("pinId")) {
                pinId = reader.nextInt();
            } else if (name.equals("userId")) {
                userId = reader.nextInt();
            } else if (name.equals("pinType")) {
                pinType = reader.nextString();
            } else if (name.equals("title")) {
                title = reader.nextString();
            } else if (name.equals("latitude")) {
                latitude = reader.nextDouble();
            } else if (name.equals("longitude")) {
                longitude = reader.nextDouble();
            } else if (name.equals("dateCreated")) {
                dateCreated = reader.nextString();
            } else if (name.equals("dateModified")) {
                dateModified = reader.nextString();
            } /*else if (name.equals("descriptions")) {
                descriptions = readDescriptionArray(reader);
            } else {
                reader.beginArray();
                while (reader.hasNext()) {
                    Log.v("Async_task", "Actually we're caught in a loop");
                }
                reader.endArray();
            }*/
        }
        reader.endObject();

        if (mapsActivity.findPinById(pinId) != null)
            return new Pin(pinId, userId, pinType,
                new LatLng(latitude, longitude), title, mapsActivity);
        else
            return null;
        //newPin.setPinDescriptions(descriptions);
    }

    public Description[] readDescriptionArray(JsonReader reader) throws IOException {
        ArrayList<Description> descriptions = new ArrayList<Description>();

        reader.beginArray();
        while (reader.hasNext()) {
            descriptions.add(readDescription(reader));
        }
        reader.endArray();
        return (Description[]) descriptions.toArray();
    }

    public Description readDescription(JsonReader reader) throws IOException {
        int descriptionId = -1;
        int userId = -1;
        int pinId = -1;
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
                text, createdAt, modifiedAt, mapsActivity);
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
