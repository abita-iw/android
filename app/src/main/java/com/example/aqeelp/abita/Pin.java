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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by aqeelp on 10/24/15.
 */
public class Pin {
    final private String[] PINTYPESTRINGS = { "Wildlife", "Foliage", "Landscape", "Architecture" };
    final private ArrayList<String> PINTYPES =
            new ArrayList<String>(Arrays.asList(PINTYPESTRINGS));
    final private int[] PINCOLORS = { Color.parseColor("#fffbbc05"),
            Color.parseColor("#ff34a853"), Color.parseColor("#ff2c59a3"),
            Color.parseColor("#ffea4335") };

    // Static attributes, received on creation:
    final private int pinId;
    final private int userId;
    final private String pinType;
    final private int pinTypeIndex;
    final private LatLng pinLocation;
    final private String pinTitle;
    final private MapsActivity parent;
    final private String createdAt;
    final private String modifiedAt;

    // Attributes changed as needed:
    private boolean inRange;
    private Description[] pinDescriptions;
    private User pinUser;
    private Bitmap thumbnail;
    private Bitmap fullSize;

    /**
     * Images:
     * - get icon bitmap upon retrieving pin
     * - get full bitmap when in close enough range to
     */

    public Pin(int pid, int uid, String ptype, LatLng loc, String ti, String ca, String ma, MapsActivity p) {
        pinId = pid;
        userId = uid;
        pinType = ptype;
        pinTypeIndex = PINTYPES.indexOf(pinType);
        pinLocation = loc;
        pinTitle = ti;
        createdAt = ca;
        modifiedAt = ma;
        parent = p;

        inRange = true;

        pinDescriptions = null;
        pinUser = null;

        thumbnail = null;

        // fetchDescriptions();
        // fetchUser();

        Log.v("Creation", this.toString());
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

    /**
     * Issues an async_call to get Descriptions for this Pin
     * @param override if false, won't fetch Descriptions if they have been found already
     */
    public void fetchDescriptions(boolean override) {
        if (!override)
            if (pinDescriptions != null)
                return;
        DescriptionRetrieval descGetter = new DescriptionRetrieval((Pin) this, parent);
        descGetter.execute("https://www.abitatech.net:5000/api/pins/" + pinId + "/descriptions");
    }

    public void setPinDescriptions(Description[] descriptions) {
        Log.v("Pin", "Descriptions received, adding to pin");
        if (descriptions != null) {
            pinDescriptions = new Description[descriptions.length];
            for (int i = 0; i < descriptions.length; i++) {
                // Make a defensive copy of all descriptions?
                pinDescriptions[i] = descriptions[i];
            }
        }
    }

    /**
     * Issues an async_call to get the User that created this Pin
     * @param override if false, won't fetch User if they have been found already
     */
    public void fetchUser(boolean override) {
        if (!override)
            if (pinUser != null)
                return;
        UserRetrieval userGetter = new UserRetrieval((Pin) this, null, parent);
        userGetter.execute("https://www.abitatech.net:5000/api/users/" + userId);
    }

    public void setPinUser(User user) {
        // Defensive copy? probably not
        this.pinUser = user;
        parent.addNewUser(user);
    }

    public void setThumbnail(Bitmap bitmap) {
        thumbnail = bitmap;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    /**
     * Getter and setter methods follow:
     */
    public String getPinType() {
        return pinType;
    }

    public int getPinTypeIndex() {
        return pinTypeIndex;
    }

    public int getPinId() {
        return pinId;
    }

    public int getUserId() {
        return userId;
    }

    public LatLng getPinLocation() {
        return pinLocation;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public Description[] getPinDescriptions() {
        return pinDescriptions;
    }

    public void setInRange(boolean newVal) {
        inRange = newVal;
    }

    public boolean getInRange() {
        return inRange;
    }

    public User getPinUser() {
        return pinUser;
    }

    public boolean hasDescriptions() {
        return (pinDescriptions != null);
    }

    public boolean hasUser() {
        return (pinUser != null);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Pin! - ");
        s.append("Id: " + getPinId() + " - ");
        s.append("Title: " + getPinTitle() + " - ");
        s.append("Location: " + getPinLocation());
        if (pinDescriptions != null) {
            for (int i = 0; i < pinDescriptions.length; i++) {
                s.append(" - ");
                s.append("Description #" + i + ": ");
                s.append(pinDescriptions[i].toString());
            }
        }

        return s.toString();
    }

    public MapsActivity getParent() {
        return parent;
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
                Log.v("PinDisplay", "inflated");

                // Set title (default previous disabled)
                TextView titleView = (TextView) dialog.findViewById(R.id.PinInfoTitle);
                String title = pin.getPinTitle() + " - " + PINTYPES.get(pin.getPinTypeIndex());
                titleView.setText(title);
                Log.v("PinDisplay", "title set");

                // Set user (default previous disabled)
                TextView userView = (TextView) dialog.findViewById(R.id.PinInfoUser);
                if (pin.getPinUser() != null)
                    userView.setText("Spotted by " + pin.getPinUser().getDisplayName());
                else
                    userView.setText("Spotted by an anonymous user");
                Log.v("PinDisplay", "user set");

                // Set image (if possible)
                Bitmap thumbnail = pin.getThumbnail();
                if (thumbnail != null) {
                    ImageView thumbnailView = (ImageView) dialog.findViewById(R.id.PinInfoImage);
                    thumbnailView.setImageBitmap(thumbnail);
                    Log.v("PinDisplay", "image set");
                }

                // Set pin display colors
                View header = dialog.findViewById(R.id.PinInfoHeader);
                View readMore = dialog.findViewById(R.id.PinInfoReadMore);
                header.setBackgroundColor(PINCOLORS[pin.getPinTypeIndex()]);
                readMore.setBackgroundColor(PINCOLORS[pin.getPinTypeIndex()]);
                Log.v("PinDisplay", "colors set");

                // Fill descriptions
                LinearLayout descriptionSection = (LinearLayout)
                        dialog.findViewById(R.id.pin_description_section);
                Description[] descriptions = pin.getPinDescriptions();

                Log.v("PinDisplay", "descriptions being set if necessary...");
                if (descriptions != null) {
                    for (int i = 0; i < descriptions.length; i++) {
                        Log.v("PinDisplay", "chill");
                        Context context = pin.getParent();
                        DescriptionView descriptionView = new DescriptionView(context, descriptions[i]);
                        Log.v("PinDisplay", "cool");
                        descriptionSection.addView(descriptionView.getView());
                        Log.v("PinDisplay", "sick");

                        if (descriptions.length - i > 1) {
                            View horizontalLine = new View(pin.getParent());
                            Log.v("PinDisplay", "nice");
                            horizontalLine.setBackgroundColor(0xFF999999);
                            horizontalLine.setPadding(10, 10, 10, 10);
                            descriptionSection.addView(horizontalLine,
                                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3));
                            Log.v("PinDisplay", "tight");
                        }
                    }
                }

                Log.v("PinDisplay", "descriptions set");

                // Force size
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.show();
            }
            return true;
        }
    };

    public int distanceTo(LatLng other) {
        return (int) Math.max(Math.abs(pinLocation.latitude - other.latitude),
                Math.abs(pinLocation.longitude - other.longitude));
    }
}
