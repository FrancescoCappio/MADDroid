package it.polito.maddroid.lab3.user;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.Utility;

public class TrackingRider extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "TrackingRider";

    public static final String ORIGIN_LOCATION_KEY = "ORIGIN_LOCATION_KEY";
    public static final String DESTINATION_LOCATION_KEY = "DESTINATION_LOCATION_KEY";
    public static final String ROUTE_KEY = "ROUTE_KEY";
    public static final String ORDER_KEY = "ORDER_KEY";

    private DatabaseReference dbRef;

    private GoogleMap mMap;
    private List<List<HashMap<String, String>>> directionRoute;

    private LatLng origin;
    private LatLng destination;
    private LatLng lastLocation;

    private Order currentOrder;

    MarkerOptions currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentLocationMarker = new MarkerOptions();


        dbRef = FirebaseDatabase.getInstance().getReference();


        Intent intent = getIntent();
        Serializable orderExtra = intent.getSerializableExtra(ORDER_KEY);

        if (orderExtra != null) {
            currentOrder = (Order) orderExtra;
        } else {
            Log.e(TAG, "Cannot open detail for a null order");
            finish();
        }

        origin = intent.getExtras().getParcelable(ORIGIN_LOCATION_KEY);
        destination =intent.getExtras().getParcelable(DESTINATION_LOCATION_KEY);
        directionRoute = (List<List<HashMap<String, String>>>) intent.getExtras().getSerializable(ROUTE_KEY);

        getCurrentLocation();

    }

    private void getCurrentLocation() {


        dbRef.child(EAHCONST.RIDERS_POSITIONS_SUBTREE).child(currentOrder.getRiderId()).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    Utility.showAlertToUser(TrackingRider.this, R.string.alert_error_downloading_info);
                    return;
                }
                double lat = (double) dataSnapshot.child("0").getValue();
                double lng = (double) dataSnapshot.child("1").getValue();
                lastLocation = new LatLng(lat,lng);
                if (mMap != null){
                    mMap.clear();

                    drawMarkerAndRoute();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void drawMarkerAndRoute() {

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

        if (lastLocation != null) {

            currentLocationMarker.position(lastLocation);
            Bitmap bb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_rider_green);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bb, 150, 150, false);

            currentLocationMarker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

            mMap.addMarker(currentLocationMarker);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            drawRoute(directionRoute);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMarkerAndRoute();


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

}
