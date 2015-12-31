package com.example.aqeelp.abita;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by aqeelp on 12/28/15.
 */
public class PicturePost extends AsyncTask<String, Void, String> {
    JSONObject json;
    Pin pin;
    Bitmap image;

    public PicturePost(Pin p, Bitmap i) {
        pin = p;
        image = i;

        trustEveryone();

        // Create the JSON object for this pin
        json = makePictureJSON();
        Log.v("PicturePost", "Description JSON constructed");
        if (json == null) return;
    }

    protected String doInBackground(String... params) {
        // Issue post request
        try {

            Log.v("PicturePost", "Issuing description post request...");

            //create a file to write bitmap data
            File pictureFile = new File(pin.getParent().getCacheDir(), "tmpfile.png");
            pictureFile.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            HttpRequest.post(params[0] + "?userId=" + pin.getUserId() + "&pinId=" + pin.getPinId())
                    .send(pictureFile)
                    .receive(result);

            pictureFile.delete();

            Log.v("PicturePost", "Completed.");
            return result.toString();

        } catch (HttpRequest.HttpRequestException e) {
            Log.v("PicturePost", "Exception: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String response) {
        Log.v("PicturePost", "Response received: " + response);
    }

    private JSONObject makePictureJSON() {
        JSONObject pinJSON = new JSONObject();

        try {
            pinJSON.put("userId", pin.getParent().getCurrentUser().getUserId()); // TODO: global user
            pinJSON.put("pinId", pin.getPinId());

            return pinJSON;
        } catch (JSONException e) {
            Log.v("PicturePost", "JSON Exception");
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
