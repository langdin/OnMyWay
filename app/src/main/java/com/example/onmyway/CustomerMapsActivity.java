package com.example.onmyway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private DatabaseReference driverWorkingDB;

    private Marker driverMark, customerMark;
    private int radius;
    private Boolean driverFound;
    private boolean requestType;
    private String driverFoundId;

    private ValueEventListener driverListener;

    GeoQuery geoQuery;

    // Driver INFO bar
    private TextView txtDriverName;
    private TextView txtDriverPhone;
    private TextView txtDriverCar;
    private CircleImageView driverProfilePic;
    private RelativeLayout driverInfoBar;

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
        driverAvailableDB = FirebaseDatabase.getInstance().getReference()
                .child("Drivers Availability");
        driverWorkingDB = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        radius = 1;
        driverFound = false;
        requestType = false;
        driverFoundId = "";

        // Driver Info
        txtDriverName = findViewById(R.id.txtDriverName);
        txtDriverPhone = findViewById(R.id.txtDriverPhone);
        txtDriverCar = findViewById(R.id.txtDriverCar);
        driverProfilePic = findViewById(R.id.profile_imageDriver);
        driverInfoBar = findViewById(R.id.driverInfo);



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

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, this);
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
            if (driverListener != null) {
                driverWorkingDB.removeEventListener(driverListener);
            }
            if (driverFound != null) {
                driverRefDB = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child("Drivers").child(driverFoundId).child("CustomerId");
                driverRefDB.removeValue();

                driverFoundId = null;
            }

            driverFound = false;
            radius = 1;

            //remove customer location from db
            userId = auth.getCurrentUser().getUid();
            GeoFire geoFire = new GeoFire(customerRefDB);
            geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });

            // remove markers
            if (customerMark != null) {
                customerMark.remove();
            }
            if (driverMark != null) {
                driverMark.remove();
            }

            btnFindCar.setText("Find a car");

            driverInfoBar.setVisibility(View.GONE);
        } else {
            requestType = true;

            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            GeoFire geoFire = new GeoFire(customerRefDB);
            geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(),
                    lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });

            customerPickUpLocation = new LatLng(lastLocation.getLatitude(),
                    lastLocation.getLongitude());
            customerMark = mMap.addMarker(new MarkerOptions()
                    .position(customerPickUpLocation).title("My location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.customer)));

            btnFindCar.setText("Searching for a car...");
            getClosestDriver();
        }
    }

    private void getClosestDriver() {
        GeoFire geoFire = new GeoFire(driverAvailableDB);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude,
                customerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundId = key;

                    driverRefDB = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(driverFoundId);
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
         driverListener = driverWorkingDB.child(driverFoundId).child("l")
                 .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestType) {
                    List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    btnFindCar.setText("Car found");

                    getAssignedDriverInfo();
                    driverInfoBar.setVisibility(View.VISIBLE);

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
                        driverMark.setTitle("Driver has arrived.");
                    } else {
                        btnFindCar.setText("Cancel");
                    }

                    driverMark = mMap.addMarker(new MarkerOptions().position(driverLatLng)
                            .title("Your Driver is" + (int)distance + " m away")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void goToSettings(View view) {
        Intent i = new Intent(CustomerMapsActivity.this, SettingsActivity.class);
        i.putExtra("isCustomer", true);
        startActivity(i);
    }

    private void getAssignedDriverInfo() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverFoundId);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    txtDriverName.setText(name);
                    txtDriverPhone.setText(phone);
                    String car = dataSnapshot.child("car").getValue().toString();
                    txtDriverCar.setText(car);


                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(driverProfilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void phoneCall(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + txtDriverPhone.getText()));

        if (ActivityCompat.checkSelfPermission(CustomerMapsActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }
}