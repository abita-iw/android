package com.example.aqeelp.abita;

import android.app.Dialog;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aqeelp on 10/26/15.
 */
public class PinMaker {
    static private MapsActivity parent;
    static private LatLng location;
    static private int newPinType;
    static private String newPinTitle;
    static private String newPinCaption;

    public static void makePin(MapsActivity p, Location loc) {
        newPinType = -1;
        newPinTitle = null;
        newPinCaption = null;

        parent = p;
        location = new LatLng(loc.getLatitude(), loc.getLongitude());

        choosePinType();
    }

    private static void generatePin() {
        if (newPinType != -1 && newPinTitle != null && newPinCaption != null) {
            Pin pin = new Pin((int) (100000 * Math.random()), newPinType, location, newPinTitle, newPinCaption, parent);
            parent.addNewPin(pin);
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
                    newPinType = type;
                    dialog.cancel();
                    specifyPinTitle();
                }
            });
        }

        dialog.show();
    }

    private static void specifyPinTitle() {
        final Dialog dialog = new Dialog(parent);
        dialog.setTitle("Give this pin a title:");

        // Set the layout view of the dialog
        dialog.setContentView(R.layout.specify_pin_title);

        // Force size
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.new_pin_title_finished).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText title_field = (EditText) dialog.findViewById(R.id.new_pin_title);
                newPinTitle = title_field.getText().toString();
                dialog.cancel();
                specifyPinCaption();
            }
        });

        dialog.show();
    }

    private static void specifyPinCaption() {
        final Dialog dialog = new Dialog(parent);
        dialog.setTitle("Give this pin a caption:");

        // Set the layout view of the dialog
        dialog.setContentView(R.layout.specify_pin_caption);

        // Force size
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.new_pin_caption_finished).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText caption_field = (EditText) dialog.findViewById(R.id.new_pin_caption);
                newPinCaption = caption_field.getText().toString();
                dialog.cancel();
                generatePin();
            }
        });

        dialog.show();
    }
}
