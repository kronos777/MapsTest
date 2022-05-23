package com.example.taxiappswm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import com.example.taxiappswm.databinding.ActivityDriverMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriverMapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityDriverMapsBinding binding;
    GoogleApiClient googleApiClient;
    Location lostLocation;
    LocationRequest locationRequest;
    Marker PicUpMarker;
    private Button LogoutDriverBtn, SettingsDriverBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogoutDriverStatus = false;
    private DatabaseReference assignedCustomerRef, AssignedCustomerPosition;
    private String driverID, customerID = "";
    private ValueEventListener AssignedCustomerPositionListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
*/
        /*test geo fire*/
    //    geoFire.setLocation("firebase-hq", new GeoLocation(37.7853889, -122.4056973));

        /*test geo fire*/
        LogoutDriverBtn = (Button) findViewById(R.id.logout);
        SettingsDriverBtn  = (Button) findViewById(R.id.settings);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();


        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
/*
        LogoutDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

            }
        });*/

        getAssignedCustomerRequest();


    }

    private void getAssignedCustomerRequest() {
        assignedCustomerRef = FirebaseDatabase.getInstance()
                .getReference().child("Users").child("Drivers")
                .child(driverID).child("CustomerRideID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        customerID = snapshot.getValue().toString();

                        getAssignedCustomerPosition();
                    } else {
                        customerID = "";

                        if (PicUpMarker != null){
                            PicUpMarker.remove();
                        }

                        if(AssignedCustomerPositionListener != null) {
                            AssignedCustomerPosition.removeEventListener(AssignedCustomerPositionListener);
                        }

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getAssignedCustomerPosition() {
        AssignedCustomerPosition = FirebaseDatabase.getInstance().getReference()
                                        .child("Customers Requests")
                .child(customerID).child("l");
        AssignedCustomerPositionListener = AssignedCustomerPosition.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<Object> customerPosition = (List<Object>) snapshot.getValue();

                    double LocationLatitude = 0;
                    double LocationLng = 0;

                    if (customerPosition.get(0) != null) {
                        LocationLatitude = Double.parseDouble(customerPosition.get(0).toString());
                    }
                    if (customerPosition.get(1) != null) {
                        LocationLng = Double.parseDouble(customerPosition.get(1).toString());
                    }

                    LatLng DriverLatLong = new LatLng(LocationLatitude, LocationLng);
                    PicUpMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLong).title("Забрать клиента тут.").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
    public void onLocationChanged(@NonNull Location location) {

        if (getApplicationContext() != null){
            lostLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DriverAvalablityRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
            DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");


            GeoFire geoFireAvailablity = new GeoFire(DriverAvalablityRef);
            GeoFire geoFireWorking = new GeoFire(DriverWorkingRef);


            //geoFireAvailablity.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
//            geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));

            switch (customerID) {
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailablity.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailablity.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }

        }
    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
               .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

    }

  /*  protected void onStop () {
        super.onStop();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference DriverAvalablityRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        GeoFire geoFire = new GeoFire(DriverAvalablityRef);
        geoFire.removeLocation(userID);

    }
    */
    protected void onStop() {
        super.onStop();

        if (!currentLogoutDriverStatus) {
            DisconnectDriver();
        }

    }

    private void DisconnectDriver() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference DriverAvalablityRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        GeoFire geoFire = new GeoFire(DriverAvalablityRef);
        geoFire.removeLocation(userID);
    }

    private void LogoutDriver() {

        Intent welcomeIntent = new Intent(DriverMapsActivity.this, WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();

    }

    public void clickExitDriver(View view) {
        currentLogoutDriverStatus = true;

        mAuth.signOut();


        LogoutDriver();
        DisconnectDriver();
    }

    public void DriverSettings(View view) {
        Intent goSettings = new Intent(DriverMapsActivity.this, SettingsActivity.class);
        goSettings.putExtra("type", "drivers");
        startActivity(goSettings);
    }
}