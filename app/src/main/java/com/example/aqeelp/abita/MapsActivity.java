package com.example.aqeelp.abita;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // Sets to satellite view w/ road names, etc.
        mMap.getUiSettings().setMapToolbarEnabled(false); // Removes default buttons
        mMap.setOnMarkerClickListener(new PinClickListener(this)); // Set custom marker click listener

        getLocation(this);
    }

    public class PinClickListener implements GoogleMap.OnMarkerClickListener {
        Context context;

        public PinClickListener(Context context) {
            this.context = context;
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            Toast.makeText(context, marker.getTitle(), Toast.LENGTH_LONG);
            return false;
        }
    }

    /**
     * Sets up the options for the markers on mMap
     */
    private void setMarker(Context context, LatLng loc) {
        /**
         * Icon field - set the bitmap icon of the marker
         * generated from .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
         *
         * GoogleMap.setOnMarkerClickListener(OnMarkerClickListener) for maps
         * onMarkerClick(Marker)
         */
        mMap.addMarker(new MarkerOptions()
                .position(loc)
                .title("Here you are!"));
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
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateCoordinates(location);
                }
            }
        } catch (Exception ex)  {
            Log.v("Location", "Error creating location service: " + ex.getMessage());
        }
    }

    private void updateCoordinates(Location location) {
        LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
        setMarker(this, here);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(here, 19.0f, 0f, 0f)));
    }
}
