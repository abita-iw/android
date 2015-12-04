package com.example.aqeelp.abita;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location lastKnownLoc;
    private Location lastQueryLoc;
    private HashMap<Integer, Pin> pinsInRange;
    private HashMap<Integer, Pin> pinsOutOfRange;
    private HashMap<Integer, User> users;
    private MapsActivity thisActivity;

    private String PinTypes[] = { "Wildlife", "Foliage", "Scenery", "Landmark" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        thisActivity = this;

        this.findViewById(R.id.pin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PinMaker.makePin(MapsActivity.this, lastKnownLoc);
            }
        });

        this.findViewById(R.id.recenter_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(MapsActivity.this);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Todo: drop pins (of static content)
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // Sets to satellite view w/ road names, etc.
        mMap.getUiSettings().setMapToolbarEnabled(false); // Removes default buttons

        pinsInRange = new HashMap<Integer, Pin>();
        pinsOutOfRange = new HashMap<Integer, Pin>();
        users = new HashMap<Integer, User>();

        lastKnownLoc = null;
        lastQueryLoc = null;
        initLocationServices(this);
        getLocation(this);
    }

    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationServices(Context context) {


        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try   {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    lastKnownLoc = location;
                    updateCoordinates();
                    PinManager.getSurroundingPins(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (Exception ex)  {
            Log.v("Location", "Error creating location service: " + ex.getMessage());
        }
    }

    /**
     * Sets up location service after permissions is granted
     */
    private void getLocation(Context context) {


        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            double longitude = 0.0;
            double latitude = 0.0;
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean locationServiceAvailable;

            if (!isGPSEnabled)    {
                // cannot get location
                Toast.makeText(this, "Please enable GPS and restart app!", Toast.LENGTH_LONG);
            }
            else
            {

                if (locationManager != null)  {
                    lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastQueryLoc == null) lastQueryLoc = lastKnownLoc;
                    updateCoordinates();
                }
            }
        } catch (Exception ex)  {
            Log.v("Location", "Error creating location service: " + ex.getMessage());
        }
    }

    // Todo: paint a little icon on the current location
    private void updateCoordinates() {
        LatLng here = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());

        if (lastKnownLoc.distanceTo(lastQueryLoc) > 100) {
            PinRetrieval pinGetter = new PinRetrieval(thisActivity);
            pinGetter.execute("https://www.abitatech.net:5000/api/pins?latitude="
                    + here.latitude + "&longitude=" + here.longitude + "&radius=1000");
            lastQueryLoc = lastKnownLoc;
        }

        updatePinsInRange();

        // setMarker(this, here);

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(here, 19.0f, 0f, 0f)));
    }

    private void updatePinsInRange() {
        for (Pin pin : pinsOutOfRange.values()) {
            pinsInRange.put(pin.getPinId(), pin);
            pinsOutOfRange.remove(pin.getPinId());
        }

        for (Pin pin : pinsInRange.values()) {
            pin.fetchDescriptions(false);
            pin.fetchUser(false);
        }
        // Pseudo-code:
        // get all of the pins from pins out of range in ascending order based on distance
        //      hashmap.values() returns a Collection of values
        //      then use Collections.sort on them
        // add those pins to the pins in range
        // get all of the pins from pins in range in descending order
        //      do the reverse of the first operation
    }

    public Location getMostRecentLocation() {
        return lastKnownLoc;
    }

    public void addNewPin(Pin pin) {
        pinsInRange.put(pin.getPinId(), pin);
    }

    // Accessor and setter methods for Pin and User hashmaps:
    public void addNewPins(List<Pin> pins) {
        for (Pin pin : pins) {
            if (!pinsOutOfRange.containsKey(pin.getPinId())
                    && !pinsInRange.containsKey(pin.getPinId())) { // avoid duplicates
                pinsOutOfRange.put(pin.getPinId(), pin);
                pin.show(mMap);
            }
        }

        updatePinsInRange();

        Log.v("Main", "Number of pins: " + (pinsInRange.size() + pinsOutOfRange.size()));
    }

    public void addNewUser(User user) {
        if (users.containsKey(user.getUserId())) return; // avoid duplicates
        users.put(user.getUserId(), user);
    }

    public Pin findPinById(int pinId) {
        Pin pin = null;
        pin = pinsInRange.get(pinId);
        if (pin != null) return pin;
        return pinsOutOfRange.get(pinId);
    }

    public User findUserById(int userId) {
        return users.get(userId);
    }
}
