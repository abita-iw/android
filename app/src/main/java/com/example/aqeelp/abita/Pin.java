package com.example.aqeelp.abita;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;
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
    private String[] PinTypes = { "Wildlife", "Foliage", "Scenery", "Landmark" };
    private int[] PinColors = { Color.parseColor("fffbbc05"),
            Color.parseColor("ff34a853"), Color.parseColor("ff2c59a3"),
            Color.parseColor("ffea4335") };

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

    public Pin(int pid, int ptype, LatLng loc, String title, String cap, MapsActivity p) {
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

        Bitmap[] pinIcons = {
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.wildlife_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.foliage_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.landscape_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.landmark_pin)
        };

        Bitmap fullSizePin = pinIcons[pinType];
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

    /**
     * Show a pin's detail when it is tapped
     */
    private GoogleMap.OnMarkerClickListener PinClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            // Resolve the pin object from its ID
            Pin pin = parent.findPinById(Integer.parseInt(marker.getTitle()));

            if (pin != null) {
                // Inflate the pin detail
                Dialog dialog = new Dialog(parent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                // Set the layout view of the dialog
                dialog.setContentView(R.layout.pin_info);

                // Set title (default previous disabled)
                TextView titleView = (TextView) dialog.findViewById(R.id.PinInfoTitle);
                String title = pin.getPinTitle() + " - " + PinTypes[pin.getPinType()];
                titleView.setText(title);

                View header = dialog.findViewById(R.id.PinInfoHeader);
                View readMore = dialog.findViewById(R.id.PinInfoReadMore);
                header.setBackgroundColor(PinColors[pin.getPinType()]);
                readMore.setBackgroundColor(PinColors[pin.getPinType()]);

                TextView caption = (TextView) dialog.findViewById(R.id.PinInfoBody);
                caption.setText(pin.getPinCaption());

                // Force size
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.show();
            }
            return true;
        }
    };
}
