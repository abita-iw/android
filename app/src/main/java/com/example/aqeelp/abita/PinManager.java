package com.example.aqeelp.abita;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by aqeelp on 10/24/15.
 */
public class PinManager {
    public static void getSurroundingPins(Location location) {
        //Todo: api call to get all pins in ~1000ft radius
        //Todo: issue an async image loader on the nearest pins (using whichWithin())
    }

    public static ArrayList<Pin> whichWithin(double radius, ArrayList<Pin> pins) {
        //Todo: brute force might be fine here...only like ~100 pins at a time
        return null;
    }
}
