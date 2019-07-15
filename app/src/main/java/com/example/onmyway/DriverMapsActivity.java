package com.example.onmyway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class DriverMapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button btnLogout;
    private Button btnSettings;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean driverLogoutStatus;

    private DatabaseReference customerRefDB, customerLocationDB;
    private String driverId, customerId="";
    private Marker customerMark;

    private ValueEventListener customerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);

        btnLogout = findViewById(R.id.btnLogout);
        btnSettings = findViewById(R.id.btnSettings);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        driverId = auth.getCurrentUser().getUid();

        driverLogoutStatus = false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        getAssignedCustomerRequest();
    }

    private void getAssignedCustomerRequest() {
        customerRefDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("CustomerId");
        customerRefDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();

                    getAssignedCustomerLocation();
                } else {
                    customerId = "";

                    if (customerMark != null) {
                        customerMark.remove();
                    }

                    if (customerListener != null) {
                        customerLocationDB.removeEventListener(customerListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedCustomerLocation() {
        customerLocationDB = FirebaseDatabase.getInstance().getReference().child("Customer Requests").child(customerId).child("l");
        customerListener = customerLocationDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if customer found
                if (dataSnapshot.exists()) {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if (customerLocationMap.get(0) != null) {
                        locationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) != null) {
                        locationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    LatLng customerLatLng = new LatLng(locationLat, locationLng);
                    // put marker
                    customerMark = mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Your Customer is here."));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void logout(View view) {
        driverLogoutStatus = true;

        disconnectDriver();

        auth.signOut();
        // go to WelcomeActivity
        navigateToWelcome();
    }

    private void navigateToWelcome() {
        Intent i = new Intent(DriverMapsActivity.this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
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

        buildGoogleClient();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
       if (getApplicationContext() != null) {
           lastLocation = location;
           LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
           mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
           mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

           String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
           //Log.i("MSG", "UserID" + userId);

           // Available drivers
           DatabaseReference driverRefDB = FirebaseDatabase.getInstance().getReference().child("Drivers Availability");
           GeoFire geoFireA = new GeoFire(driverRefDB);

           // Drivers that already have clients
           DatabaseReference driverWorkdingDB = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
           GeoFire geoFireW = new GeoFire(driverWorkdingDB);

           switch (customerId) {
               case "":
                   geoFireW.removeLocation(userId, new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   geoFireA.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   break;

               default:
                   geoFireA.removeLocation(userId, new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   geoFireW.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   break;
           }
       }
    }

    protected synchronized void buildGoogleClient() {
        // build
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // connect
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!driverLogoutStatus) {
            disconnectDriver();
        }
    }

    private void disconnectDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Drivers Availability");

        GeoFire geoFire = new GeoFire(driverRef );
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }
}
