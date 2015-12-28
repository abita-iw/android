package com.example.aqeelp.abita;

import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
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
    private SparseArray<Pin> pinsInRange;
    private SparseArray<Pin> pinsOutOfRange;
    private SparseArray<User> users;
    private MapsActivity thisActivity;
    private final int LOCATION_PERMISSIONS_CALLBACK = 0;

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
                getLocation();
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
        Log.v("Main", "Map ready, proceeding");

        if (Build.VERSION.SDK_INT >= 23)
            ActivityCompat.requestPermissions(thisActivity,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSIONS_CALLBACK);

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // Sets to satellite view w/ road names, etc.
        mMap.getUiSettings().setMapToolbarEnabled(false); // Removes default buttons

        Log.v("Main", "Map initialized");

        pinsInRange = new SparseArray<Pin>();
        pinsOutOfRange = new SparseArray<Pin>();
        users = new SparseArray<User>();

        lastKnownLoc = null;
        lastQueryLoc = null;

        Log.v("Main", "Pin and User caching initialized");
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.v("Main", "Permissions request result received");
        switch (requestCode) {
            case LOCATION_PERMISSIONS_CALLBACK: {
                Log.v("Main", "Location request received");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initLocationServices();
                    getLocation();
                    Log.v("Main", "Location listener initialized");
                } else {
                    exit();
                }
                return;
            }
        }
    }

    public void exit() {
        thisActivity.finish();
        System.exit(0);
    }

    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationServices() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(thisActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(thisActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.v("Main", "InitLocationServices(): Permissions not currently granted");
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS_CALLBACK);
            return;
        }

        try {
            LocationManager locationManager = (LocationManager) thisActivity.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    lastKnownLoc = location;
                    updateCoordinates();
                    PinManager.getSurroundingPins(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (Exception ex) {
            Log.v("Main", "InitLocationServices(): Error creating location service: " + ex.getMessage());
        }
    }

    /**
     * Sets up location service after permissions is granted
     */
    private void getLocation() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(thisActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(thisActivity,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.v("Main", "GetLocation(): Permissions not granted");
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS_CALLBACK);
            return;
        }

        try {
            double longitude = 0.0;
            double latitude = 0.0;
            LocationManager locationManager = (LocationManager) thisActivity.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean locationServiceAvailable;

            if (!isGPSEnabled) {
                // cannot get location
                Toast.makeText(this, "Please enable GPS and restart app!", Toast.LENGTH_LONG);
            } else {
                if (locationManager != null) {
                    lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateCoordinates();
                }
            }
        } catch (Exception ex) {
            Log.v("Main", "GetLocation(): Error creating location service: " + ex.getMessage());
        }
    }

    private void updateCoordinates() {
        Log.v("UpdateCoordinates", "Updating coordinates...");
        LatLng here = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());

        if (lastQueryLoc != null) {
            Log.v("UpdateCoordinates", "LastQueryLocation init");
            if (lastKnownLoc.distanceTo(lastQueryLoc) > 100) {
                PinRetrieval pinGetter = new PinRetrieval(thisActivity);
                pinGetter.execute("https://www.abitatech.net:5000/api/pins?latitude="
                        + here.latitude + "&longitude=" + here.longitude + "&radius=100000");
                lastQueryLoc = lastKnownLoc;
            }
        } else {
            Log.v("UpdateCoordinates", "LastQueryLocation not yet init");
            PinRetrieval pinGetter = new PinRetrieval(thisActivity);
            pinGetter.execute("https://www.abitatech.net:5000/api/pins?latitude="
                    + here.latitude + "&longitude=" + here.longitude + "&radius=100000");
            lastQueryLoc = lastKnownLoc;
        }

        updatePinsInRange();

        // TODO: setMarker(this, here);

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(here, 19.0f, 0f, 0f)));
    }

    // TODO: Currently making all pins 'in range'
    private void updatePinsInRange() {
        for (int i = 0; i < pinsOutOfRange.size(); i++) {
            Pin pin = pinsOutOfRange.valueAt(i);
            pinsInRange.put(pin.getPinId(), pin);
            pinsOutOfRange.remove(pin.getPinId());
        }

        for (int i = 0; i < pinsInRange.size(); i++) {
            Pin pin = pinsInRange.valueAt(i);
            pin.fetchDescriptions(false);
            // pin.fetchUser(false);
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

    // Accessor and setter methods for Pin and User SparseArrays:
    public void addNewPins(List<Pin> pins) {
        for (Pin pin : pins) {
            if (pinsOutOfRange.get(pin.getPinId()) == null
                    && pinsInRange.get(pin.getPinId()) == null) { // avoid duplicates
                pinsOutOfRange.put(pin.getPinId(), pin);
                pin.show(mMap);
            }
        }

        updatePinsInRange();

        Log.v("Main", "Number of pins: " + (pinsInRange.size() + pinsOutOfRange.size()));
    }

    public void addNewUser(User user) {
        if (users.get(user.getUserId()) != null) return; // avoid duplicates
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
