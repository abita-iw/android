package com.example.aqeelp.abita;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aqeelp on 10/26/15.
 */
public class PinMaker {
    final static private int[] PINCOLORS = { Color.parseColor("#fffbbc05"),
            Color.parseColor("#ff34a853"), Color.parseColor("#ff2c59a3"),
            Color.parseColor("#ffea4335") };

    static private MapsActivity parent;
    static private LatLng location;
    static private Bitmap preview;
    static private int newPinTypeIndex;
    static private String newPinType;
    static private String newPinTitle;
    static private String newPinCaption;

    public static void makePin(MapsActivity p, Location loc) {
        newPinType = null;
        newPinTitle = null;
        newPinCaption = null;

        parent = p;
        location = new LatLng(loc.getLatitude(), loc.getLongitude());
        preview = null;

        choosePinType();
    }

    public static void makePin(MapsActivity p, Location loc, Bitmap pic) {
        newPinType = null;
        newPinTitle = null;
        newPinCaption = null;

        parent = p;
        location = new LatLng(loc.getLatitude(), loc.getLongitude());
        preview = pic;

        choosePinType();
    }

    private static void generatePin() {
        if (newPinType != null && newPinTitle != null && newPinCaption != null) {
            // Create local Pin
            // TODO: Pin ID has to come from the post
            Pin pin = new Pin(1, 18, newPinType, location,
                    newPinTitle, parent);

            // Upload the pin:
            Log.v("PinPost", "Starting pin post");
            // TODO PinPost poster = new PinPost(pin);
            // TODO poster.execute("https://api.abitatech.net:5000/api/pins");
            Log.v("PinPost", "Pin posted, added to parent");

            // Set and upload image (if necessary)
            if (preview != null) {
                // TODO: upload image
                pin.setThumbnail(preview);
            }

            Log.v("PinPost", "Found a thumbnail and set it");

            // Set and upload caption (if necessary)
            if (newPinCaption != null) {
                Description description = new Description(1, 18, pin.getPinId(),
                        newPinCaption, "", "", parent);
                Description[] descriptions = new Description[1];
                descriptions[0] = description;
                pin.setPinDescriptions(descriptions);
                // TODO upload description
            }

            Log.v("PinPost", "Made descriptions and added them");

            parent.addNewPin(pin);

            Log.v("PinPost", "Pin creation complete");
        }
    }

    private static void choosePinType() {
        final Dialog dialog = new Dialog(parent);
        dialog.setTitle("What type of pin is this?");

        // Set the layout view of the dialog
        dialog.setContentView(R.layout.choose_pin_type);

        // Force size
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        View[] responses = { dialog.findViewById(R.id.new_pin_is_wildlife),
                dialog.findViewById(R.id.new_pin_is_foliage),
                dialog.findViewById(R.id.new_pin_is_scenery),
                dialog.findViewById(R.id.new_pin_is_landmark)};

        for (int i = 0; i < responses.length; i++) {
            final int type = i;
            responses[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (type) {
                        case 0:
                            newPinType = "Wildlife";
                            break;
                        case 1:
                            newPinType = "Foliage";
                            break;
                        case 2:
                            newPinType = "Scenery";
                            break;
                        case 3:
                            newPinType = "Architecture";
                    }
                    dialog.cancel();
                    newPinTypeIndex = type;
                    specifyPinDetails();
                }
            });
        }

        dialog.show();
    }

    private static void specifyPinDetails() {
        final Dialog dialog = new Dialog(parent);
        // dialog.setTitle("Give this pin a title:");

        // Set the layout view of the dialog
        dialog.setContentView(R.layout.specify_pin_details);

        dialog.findViewById(R.id.new_pin_type).setBackgroundColor(PINCOLORS[newPinTypeIndex]);
        ((TextView) dialog.findViewById(R.id.new_pin_type)).setText("New " + newPinType + " Pin:");

        if (preview != null) {
            ImageView previewView = (ImageView) dialog.findViewById(R.id.new_pin_image_preview);
            previewView.setImageBitmap(preview);
        }

        // Force size
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.new_pin_title_finished).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: validate these
                EditText title_field = (EditText) dialog.findViewById(R.id.new_pin_title);
                newPinTitle = title_field.getText().toString();

                EditText caption_field = (EditText) dialog.findViewById(R.id.new_pin_description);
                newPinCaption = caption_field.getText().toString();

                dialog.cancel();
                generatePin();
            }
        });

        dialog.show();
    }
}
