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
    final private Context context;

    private String PinTypes[] = { "Wildlife", "Foliage", "Scenery", "Landmark" };

    /**
     * Images:
     * - get icon bitmap upon retrieving pin
     * - get full bitmap when in close enough range to
     */

    public Pin (int pid, int ptype, LatLng loc, String title, String cap, Context c) {
        pinId = pid;
        pinType = ptype;
        pinLocation = loc;
        pinTitle = title;
        pinCaption = cap;
        context = c;
    }

    public void show(GoogleMap map, Location currentLocation) {
        Bitmap fullSizePin = BitmapFactory.decodeResource(context.getResources(), R.drawable.wildlife_pin);
        Bitmap smallPin = Bitmap.createScaledBitmap(fullSizePin, 150, 150, false);

        LatLng loc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        map.addMarker(new MarkerOptions()
                .position(loc)
                .title("Canada Goose")
                .icon(BitmapDescriptorFactory.fromBitmap(smallPin)));
    }

    public int getPinType() {
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
        //Todo: probably make this its own class
        @Override
        public boolean onMarkerClick(Marker marker) {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            // Set the layout view of the dialog
            dialog.setContentView(R.layout.pin_info);

            // Set title (default previous disabled)
            TextView text = (TextView) dialog.findViewById(R.id.PinInfoTitle);
            text.setText(marker.getTitle() + " - " + PinTypes[pinType]);

            // Force size
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.show();
            return true;
        }
    };
}
