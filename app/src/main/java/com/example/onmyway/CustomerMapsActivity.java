package com.example.onmyway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

public class CustomerMapsActivity extends FragmentActivity implements
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
    private Button btnFindCar;
    private LatLng customerPickUpLocation;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private DatabaseReference customerRefDB;
    private DatabaseReference driverAvailableDB;
    private DatabaseReference driverRefDB;
    private DatabaseReference driverLocationDB;

    private Marker driverMark, customerMark;
    private int radius;
    private boolean driverFound, requestType;
    private String driverFoundId;

    private ValueEventListener driverListener;

    GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);

        btnLogout = findViewById(R.id.btnLogout);
        btnSettings = findViewById(R.id.btnSettings);
        btnFindCar = findViewById(R.id.btnFindCar);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        customerRefDB = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        driverAvailableDB = FirebaseDatabase.getInstance().getReference().child("Drivers Availability");
        driverLocationDB = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        radius = 1;
        driverFound = false;
        requestType = false;
        driverFoundId = "";

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void logout(View view) {
        auth.signOut();
        // go to WelcomeActivity
        navigateToWelcome();
    }

    private void navigateToWelcome() {
        Intent i = new Intent(CustomerMapsActivity.this, WelcomeActivity.class);
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
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


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

    }

    public void findCar(View view) {
        if (requestType) {
            requestType = false;
            geoQuery.removeAllListeners();
            driverLocationDB.removeEventListener(driverListener);
            if (driverFoundId != null) {
                driverRefDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                driverRefDB.setValue(true);
                driverFoundId = null;
            }

            driverFound = false;
            radius = 1;

            //remove customer location from db
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            GeoFire geoFire = new GeoFire(customerRefDB);
            geoFire.removeLocation(userId);

            // remove markers
            if (customerMark != null) {
                customerMark.remove();
            }
            if (driverMark != null) {
                driverMark.remove();
            }

            btnFindCar.setText("Find a car");
        } else {
            requestType = true;

            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            GeoFire geoFire = new GeoFire(customerRefDB);
            geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });

            customerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            customerMark = mMap.addMarker(new MarkerOptions().position(customerPickUpLocation).title("Pickup customer here"));

            btnFindCar.setText("Searching for a car...");
            getClosestDriver();
        }
    }

    private void getClosestDriver() {
        GeoFire geoFire = new GeoFire(driverAvailableDB);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude, customerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundId = key;

                    driverRefDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerId", userId);
                    driverRefDB.updateChildren(driverMap);

                    getDriverLocation();
                    //btnFindCar.setText("Searching for a car...");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void getDriverLocation() {
         driverListener = driverLocationDB.child(driverFoundId).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestType) {
                    List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    btnFindCar.setText("Car found");

                    if (driverLocationMap.get(0) != null) {
                        locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                    }
                    if (driverLocationMap.get(1) != null) {
                        locationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (driverMark != null) {
                        driverMark.remove();

                    }

                    Location locationCustomerLoc = new Location("");
                    locationCustomerLoc.setLatitude(customerPickUpLocation.latitude);
                    locationCustomerLoc.setLongitude(customerPickUpLocation.longitude);

                    Location locationDrive = new Location("");
                    locationDrive.setLatitude(driverLatLng.latitude);
                    locationDrive.setLongitude(driverLatLng.longitude);

                    float distance = locationCustomerLoc.distanceTo(locationDrive);

                    if (distance < 60) {
                        btnFindCar.setText("Driver has arrived.");
                    } else {
                        btnFindCar.setText("Car found: " + (int)distance + " m away");
                    }

                    driverMark = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver is here."));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}