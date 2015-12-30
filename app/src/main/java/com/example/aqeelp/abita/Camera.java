package com.example.aqeelp.abita;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

/**
 * Created by aqeelp on 12/29/15.
 */
public class Camera {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final MapsActivity CONTEXT;

    public Camera(MapsActivity c) {
        CONTEXT = c;
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(CONTEXT.getPackageManager()) != null) {
            CONTEXT.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void activityResult(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        PinMaker.makePin(CONTEXT, CONTEXT.getMostRecentLocation(), imageBitmap);
    }
}
