package com.example.aqeelp.abita;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

/**
 * Created by aqeelp on 12/29/15.
 */
public class Camera {
    static final int READ_WRITE_PERMISSIONS_CALLBACK = 2;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final MapsActivity CONTEXT;

    public Camera(MapsActivity c) {
        CONTEXT = c;

        if (Build.VERSION.SDK_INT >= 23)
            ActivityCompat.requestPermissions(CONTEXT,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_WRITE_PERMISSIONS_CALLBACK);
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(CONTEXT.getPackageManager()) != null) {
            CONTEXT.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void activityResult(Intent data) {
        // TODO: use read/write permissions to get the full image rather than the thumbnail

        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        PinMaker.makePin(CONTEXT, CONTEXT.getMostRecentLocation(), imageBitmap);
    }
}
