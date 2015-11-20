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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by aqeelp on 10/24/15.
 */
public class Pin {
    final private String[] PINTYPESTRINGS = { "Wildlife", "Foliage", "Scenery", "Landmark" };
    final private ArrayList<String> PINTYPES =
            new ArrayList<String>(Arrays.asList(PINTYPESTRINGS));
    final private int[] PINCOLORS = { Color.parseColor("#fffbbc05"),
            Color.parseColor("#ff34a853"), Color.parseColor("#ff2c59a3"),
            Color.parseColor("#ffea4335") };

    final private int pinId;
    final private String pinType;
    final private int pinTypeIndex;
    final private LatLng pinLocation;
    final private String pinTitle;
    final private String pinDescription;
    final private MapsActivity parent;

    private Description[] descriptions;
    private Bitmap thumbnail;
    private Bitmap fullSize;

    /**
     * Images:
     * - get icon bitmap upon retrieving pin
     * - get full bitmap when in close enough range to
     */

    public Pin(int pid, String ptype, LatLng loc, String ti, String desc, MapsActivity p) {
        pinId = pid;
        pinType = ptype;
        pinTypeIndex = PINTYPES.indexOf(pinType);
        pinLocation = loc;
        pinTitle = ti;
        pinDescription = desc;
        parent = p;

        getDescriptions();
    }

    private void getDescriptions() {
        //TODO: async call for the descriptions using the pin id
        descriptions = new Description[1];
        descriptions[0] = new Description(0, 0, 0, "Hey! an interesting comment.",
                "2015-10-21T03:55:06.000Z", "2015-10-21T03:55:06.000Z");
    }

    public void show(GoogleMap map) {
        map.setOnMarkerClickListener(PinClickListener); // Set custom marker click listener

        Bitmap[] pinIcons = {
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.wildlife_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.foliage_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.landscape_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.landmark_pin)
        };

        Bitmap fullSizePin = pinIcons[pinTypeIndex];
        Bitmap smallPin = Bitmap.createScaledBitmap(fullSizePin, 150, 150, false);

        map.addMarker(new MarkerOptions()
                .position(pinLocation)
                .title(pinId + "")
                .icon(BitmapDescriptorFactory.fromBitmap(smallPin)));
    }

    public String getPinType() {
        return pinType;
    }

    public int getPinTypeIndex() {
        return pinTypeIndex;
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

    public String getPinDescription() {
        return pinDescription;
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
                String title = pin.getPinTitle() + " - " + PINTYPES.get(pin.getPinTypeIndex());
                titleView.setText(title);

                View header = dialog.findViewById(R.id.PinInfoHeader);
                View readMore = dialog.findViewById(R.id.PinInfoReadMore);
                header.setBackgroundColor(PINCOLORS[pin.getPinTypeIndex()]);
                readMore.setBackgroundColor(PINCOLORS[pin.getPinTypeIndex()]);

                //TODO: fill section wtih descriptions
                TextView caption = (TextView) dialog.findViewById(R.id.PinInfoBody);
                caption.setText(pin.getPinDescription());

                // Force size
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.show();
            }
            return true;
        }
    };
}
