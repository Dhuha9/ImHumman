package com.example.imhumman;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {


    GoogleMap mMap;
    SearchView searchView;

    SupportMapFragment mapFragment;
    Button btnAddLatLong;
    LatLng finalLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 90;
    ImageView refresh;
    ProgressBar mapProgress;

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
    OnSuccessListener<LocationSettingsResponse> settingSuccess = new OnSuccessListener<LocationSettingsResponse>() {
        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            requestCurrentLocation();
        }
    };
    OnFailureListener settingsFailure = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(MapActivity.this, "settingsFailure", Toast.LENGTH_LONG).show();
            mapProgress.setVisibility(View.GONE);

            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    resolvableApiException.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Toast.makeText(MapActivity.this, "Exception" + sendEx, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    View.OnClickListener refreshMap = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createLocationRequest();
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
                        Toast.makeText(MapActivity.this, "هذا العنوان غير موجود", Toast.LENGTH_SHORT).show();
                    } else {
                        Address address = addresses.get(0);

                        LatLng userLocation = new LatLng(address.getLatitude(), address.getLongitude());
                        applyLocationToMap(userLocation);
                    }
                } else {
                    Toast.makeText(MapActivity.this, "الرجاء قم بادخال موقع للبحث عنه", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MapActivity.this, "الرجاء قم بادخال موقع للبحث عنه", Toast.LENGTH_LONG).show();
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
        changeMapLanguage();
        setContentView(R.layout.activity_map);
        setToolbar();
        showTheMap();
        getLayoutViews();
        setToViews();
        // autoCompleteMap();
    }

    private void changeMapLanguage() {
        Locale locale = new Locale("ar_IQ");
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().createConfigurationContext(configuration);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private void getLayoutViews() {
        searchView = findViewById(R.id.searchView);
        btnAddLatLong = findViewById(R.id.btnAddLatLong);
        refresh = findViewById(R.id.refresh);

        mapProgress = findViewById(R.id.mapProgress);

    }

    private void setToViews() {
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnQueryTextListener(searchTheMap);
        btnAddLatLong.setVisibility(View.VISIBLE);
        btnAddLatLong.setOnClickListener(sendLatLong);
        refresh.setVisibility(View.VISIBLE);
        refresh.setOnClickListener(refreshMap);
    }

    private void showTheMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mapProgress.setVisibility(View.GONE);
                locationResult.getLastLocation();
                LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                applyLocationToMap(latLng);
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        };

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);
        checkPermission();
    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "noooooo permission granted", Toast.LENGTH_SHORT).show();
                mapProgress.setVisibility(View.GONE);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            } else {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                createLocationRequest();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest();
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSettings(locationRequest);
    }

    private void locationSettings(LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, settingSuccess);
        task.addOnFailureListener(this, settingsFailure);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    requestCurrentLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "لم يتم تفعيل خدمة الموقع , قم بالبحث عن الموقع في خانة البحث", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;

            }
        }
    }

    private void requestCurrentLocation() {
        mapProgress.setVisibility(View.VISIBLE);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void applyLocationToMap(LatLng latLng) {
        mMap.clear();
        MarkerOptions options = new MarkerOptions().position(latLng).title("يمكنك سحب وافلات الدبوس لتغيير الموقع").draggable(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        mMap.addMarker(options);
        finalLocation = latLng;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        finalLocation = marker.getPosition();
    }

  /*  private void getCurrentLocation() {

        createLocationRequest();
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    applyLocationToMap(latLng);

                } else {
                    createLocationRequest();
                }
            }

        });
    }*/



/*    private Boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }*/








    /*  private void autoCompleteMap() {
 add in xml
*
* <fragment android:id="@+id/autocomplete_fragment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:name="com.google.android.libraries.places.widget.AutocompleteSupportFragment"
        app:layout_constraintTop_toBottomOf="@id/header"
        app:layout_constraintBottom_toTopOf="@id/map"

        />
*
        Places.initialize(getApplicationContext(), getString(R.string.APIs_Map_Key));

        //PlacesClient placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Toast.makeText(MapActivity.this, "Place: " + place.getName() + ", " + place.getId(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(MapActivity.this, "status" + status, Toast.LENGTH_LONG).show();
            }
        });
    }*/


}
