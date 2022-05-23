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
import com.example.taxiappswm.databinding.ActivityCustomerMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityCustomerMapsBinding binding;
    GoogleApiClient googleApiClient;
    Location lostLocation;
    LocationRequest locationRequest;
    private String customerId;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDatabaseRef;
    private LatLng CustomerPosition;
    private Button callDriver;
    private DatabaseReference DriversAvailableRef;
    private int radius = 1;
    private String driverFoundId;
    private boolean driverFound = false, requestType;
    private DatabaseReference DriversRef;
    private DatabaseReference DriversLocationRef;
    Marker driverMarker, PickUpMarker;
    private ValueEventListener DriverLocationRefListener;
    GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        callDriver = (Button) findViewById(R.id.callcar);
     //   customerId
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        DriversAvailableRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        DriversLocationRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");

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
        lostLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void CustomerSettings(View view) {
        Intent goSettings = new Intent(CustomerMapsActivity.this, SettingsActivity.class);
        goSettings.putExtra("type", "customers");
        startActivity(goSettings);

    }

    public void clickExitDriver(View view) {
        LogoutCustomer();
    }

    public void CustomerCallDriver(View view) {

        if(requestType){
            requestType = false;
            geoQuery.removeAllListeners();
            DriversLocationRef.removeEventListener(DriverLocationRefListener);
            if(driverFound != false) {
                DriversRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Drivers").child(driverFoundId)
                        .child("CustomerRideID");
                DriversRef.removeValue();
                //DriversRef.setValue(true);
                driverFoundId = null;
            }
            driverFound = false;
            radius = 1;
            GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
            geoFire.removeLocation(customerId);

            if(PickUpMarker != null) {
                PickUpMarker.remove();
            }
            if(driverMarker != null) {
                driverMarker.remove();
            }

            callDriver.setText("Вызвать такси");


        } else {
            requestType = true;
            GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
            geoFire.setLocation(customerId, new GeoLocation(lostLocation.getLatitude(), lostLocation.getLongitude()));

            CustomerPosition = new LatLng(lostLocation.getLatitude(), lostLocation.getLongitude());
            PickUpMarker = mMap.addMarker(new MarkerOptions().position(CustomerPosition).title("Я тут").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

            callDriver.setText("Поиск водителя...");

            getNearbyDrivers();

        }




    }

    private void getNearbyDrivers() {

        GeoFire geoFire = new GeoFire(DriversAvailableRef);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPosition.latitude, CustomerPosition.longitude), radius);
        geoQuery.removeAllListeners();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundId = key; //если нашли водителя передали ключ

                    DriversRef = FirebaseDatabase.getInstance()
                                .getReference().child("Users")
                                .child("Drivers").child(driverFoundId);

                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", customerId);
                    DriversRef.updateChildren(driverMap);
                    GetDriverLocation();

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
                if(!driverFound) {
                    radius = radius + 1;
                    getNearbyDrivers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GetDriverLocation() {
        DriverLocationRefListener = DriversLocationRef.child(driverFoundId).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && requestType){
                            List<Object> driverLocationMap = (List<Object>) snapshot.getValue();

                            double LocationLatitude = 0;
                            double LocationLng = 0;

                            callDriver.setText("Водитель найден...");

                            if (driverLocationMap.get(0) != null) {
                                LocationLatitude = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if (driverLocationMap.get(1) != null) {
                                LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            LatLng DriverLatLong = new LatLng(LocationLatitude, LocationLng);

                            if(driverMarker != null) {
                                driverMarker.remove();
                            }

                            Location location1 = new Location("");
                            location1.setLatitude(CustomerPosition.latitude);
                            location1.setLatitude(CustomerPosition.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLong.latitude);
                            location2.setLatitude(DriverLatLong.longitude);

                            float Distance = location1.distanceTo(location2);

                            if(Distance > 100) {
                                callDriver.setText("Ваше такси подъезжает");
                            } else {
                                callDriver.setText("Расстояние до такси..." + String.valueOf(Distance));
                            }



                            driverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLong).title("Ваш водитель тут.").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

   /* private void DisconnectCustomer() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference DriverAvalablityRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        GeoFire geoFire = new GeoFire(DriverAvalablityRef);
        geoFire.removeLocation(userID);
    }*/

    private void LogoutCustomer() {

        Intent welcomeIntent = new Intent(CustomerMapsActivity.this, WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();

    }

}