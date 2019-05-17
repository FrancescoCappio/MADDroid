package it.polito.maddroid.lab3.rider;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutingActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String ORIGIN_LOCATION_KEY = "ORIGIN_LOCATION_KEY";
    public static final String DESTINATION_LOCATION_KEY = "DESTINATION_LOCATION_KEY";
    public static final String ROUTE_KEY = "ROUTE_KEY";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private List<List<HashMap<String, String>>> directionRoute;

    private LocationManager locationManager;
    private LocationListener locationListener;
    
    private LatLng origin;
    private LatLng destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        origin = intent.getExtras().getParcelable(ORIGIN_LOCATION_KEY);
        destination =intent.getExtras().getParcelable(DESTINATION_LOCATION_KEY);
        directionRoute = (List<List<HashMap<String, String>>>) intent.getExtras().getSerializable(ROUTE_KEY);
    
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        //Add ORIGIN mark to map
        MarkerOptions options = new MarkerOptions();
        options.position(origin);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(options);

        //Add DESTINATION mark to map
        options = new MarkerOptions();
        options.position(destination);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(options);
        
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    
        drawRoute(directionRoute);
    }

    private void drawRoute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);

            Log.d("onPostExecute","onPostExecute lineoptions decoded");

        }

        // Drawing polyline in the Google Map for the i-th route
        if(lineOptions != null) {
            mMap.addPolyline(lineOptions);
        }
        else {
            Log.d("onPostExecute","without Polylines drawn");
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            // ///Criteria //////////
//            Criteria crta = new Criteria();
//            crta.setAccuracy(Criteria.ACCURACY_MEDIUM);
//            crta.setPowerRequirement(Criteria.POWER_LOW);
//
//            String provider = locationManager.getBestProvider(crta, true);
//            locationManager.requestLocationUpdates(provider, 30000, 0, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    locationListener = this;
//                    if (mCurrLocationMarker != null) {
//                        mCurrLocationMarker.remove();
//                    }
//
//                    //Place current location marker
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                    //move map camera
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//
//                }
//            });
//
//        }

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }
    
    @Override
    public void onConnectionSuspended(int i) {
    
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                mMap.setMyLocationEnabled(true);
            }
        }
    }
    
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    
    }
}
