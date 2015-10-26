package com.example.aqeelp.abita;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by aqeelp on 10/24/15.
 */
public class Pin {
    final private int pinId;
    final private int pinType;
    final private LatLng pinLocation;
    final private String pinTitle;
    final private String pinCaption;
    final private MapsActivity parent;

    /**
     * Images:
     * - get icon bitmap upon retrieving pin
     * - get full bitmap when in close enough range to
     */

    public Pin (int pid, int ptype, LatLng loc, String title, String cap, MapsActivity p) {
        pinId = pid;
        pinType = ptype;
        pinLocation = loc;
        pinTitle = title;
        pinCaption = cap;
        Log.v("Test", "got the rest of the metadata");
        parent = p;

        Log.v("Test", "Made a new pin, pid: "+pid);
    }

    public void show(GoogleMap map) {
        Log.v("Pin", "Showing the pin now");
        map.setOnMarkerClickListener(PinClickListener); // Set custom marker click listener

        Bitmap fullSizePin = BitmapFactory.decodeResource(parent.getResources(), R.drawable.wildlife_pin);
        Bitmap smallPin = Bitmap.createScaledBitmap(fullSizePin, 150, 150, false);

        map.addMarker(new MarkerOptions()
                .position(pinLocation)
                .title(pinId + "")
                .icon(BitmapDescriptorFactory.fromBitmap(smallPin)));
    }

    public int getPinType() {
        return pinType;
    }

    public int getPinId() {
        return pinId;
    }

    public LatLng getPinLocation() {
        return pinLocation;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public String getPinCaption() {
        return pinCaption;
    }

    private GoogleMap.OnMarkerClickListener PinClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            parent.loadPinDetail(Integer.parseInt(marker.getTitle()));
            return true;
        }
    };
}
