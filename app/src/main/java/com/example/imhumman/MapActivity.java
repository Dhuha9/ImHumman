package com.example.imhumman;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap mMap;
    SearchView searchView;
    LocationManager locationManager;
    LocationListener locationListener;
    SupportMapFragment mapFragment;
    Button btnAddLatLong;
    LatLng finalLocation;
    private View.OnClickListener sendLatLong = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (finalLocation != null) {
                Intent intent = new Intent(); //(MapActivity.this, AddPostActivity.class);
                intent.putExtra("latlng", finalLocation);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(MapActivity.this, "لم يتم اختيار موقع لاضافته", Toast.LENGTH_SHORT).show();
            }

        }
    };

    private SearchView.OnQueryTextListener searchTheMap = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            String locations = searchView.getQuery().toString();
            List<Address> addresses = null;
            if (!locations.equals("")) {

                Geocoder geocoder = new Geocoder(MapActivity.this);
                try {
                    addresses = geocoder.getFromLocationName(locations, 1);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null) {
                    if (addresses.size() == 0) {
                        Toast.makeText(MapActivity.this, "address not found", Toast.LENGTH_SHORT).show();
                    } else {
                        Address address = addresses.get(0);
                        mMap.clear();
                        LatLng userLocation = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(userLocation).title(locations));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
                        finalLocation = userLocation;

                    }
                } else {
                    Toast.makeText(MapActivity.this, "please inter a place to search for", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MapActivity.this, "الرجاء قم بادخال موقع للبحث عنه", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setToolbar();
        showTheMap();

        searchView = findViewById(R.id.searchView);
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnQueryTextListener(searchTheMap);

        btnAddLatLong = findViewById(R.id.btnAddLatLong);
        btnAddLatLong.setVisibility(View.VISIBLE);
        btnAddLatLong.setOnClickListener(sendLatLong);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private void showTheMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void permissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {

                // we have permission! Opening the app without asking for permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                //will start the app from the last location the user entered
                Location lastKnowingLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnowingLocation != null) {
                    LatLng userLocation = new LatLng(lastKnowingLocation.getLatitude(), lastKnowingLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation)
                            .title("Marker in your location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    finalLocation = userLocation;
                }
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation)
                        .title("Marker in your location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 5));
                finalLocation = userLocation;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        permissionGranted();
    }


}
