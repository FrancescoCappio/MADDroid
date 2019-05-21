package it.polito.maddroid.lab3.common;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class ChooseRiderActivity extends AppCompatActivity {
    
    public static final String TAG = "ChooseRiderActivity";
    public static final String RIDER_RESULT = "RIDER_RESULT";
    public static final String RESTAURANT_ID_KEY = "RESTAURANT_ID_KEY";
    private static final String RESTAURANT_LAT_KEY = "RESTAURANT_LAT_KEY";
    private static final String RESTAURANT_LONG_KEY = "RESTAURANT_LONG_KEY";
    private static final String CURRENT_RADIUS_KEY = "CURRENT_RADIUS_KEY";
    
    private RecyclerView rvRiders;
    private ImageButton ibIncrease;
    private ImageButton ibDecrease;
    private TextView tvRadius;
    
    private int currentRadius = 1;
    
    private RidersListAdapter adapter;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    
    private List<Rider> riders;
    
    private DatabaseReference dbRef;
    
    private GeoLocation currentRestaurantLocation;
    private String restaurantId;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_rider);
    
        getReferencesToViews();
        
        tvRadius.setText(String.valueOf(currentRadius));
    
        rvRiders.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        
        adapter = new RidersListAdapter(new RiderDiffUtil(), rider -> {
            Intent data = new Intent();
            data.putExtra(RIDER_RESULT, rider);
            setResult(RESULT_OK, data);
            finish();
        });
        
        rvRiders.setAdapter(adapter);
        
        addOnClickListeners();
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(dbRef.child(EAHCONST.RIDERS_POSITIONS_SUBTREE));
    
        riders = new ArrayList<>();
        
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            restaurantId = intent.getStringExtra(RESTAURANT_ID_KEY);
    
            getRestaurantLocation();
            
        } else {
            restaurantId = savedInstanceState.getString(RESTAURANT_ID_KEY);
            float restLat = savedInstanceState.getFloat(RESTAURANT_LAT_KEY, -500);
            float restLong = savedInstanceState.getFloat(RESTAURANT_LONG_KEY, -500);
            
            currentRadius = savedInstanceState.getInt(CURRENT_RADIUS_KEY);
            if (restLat < -450 || restLong < -450) {
                getRestaurantLocation();
            } else {
                currentRestaurantLocation = new GeoLocation(restLat, restLong);
                updateListCurrentRadius();
            }
        }
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.choose_rider);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(RESTAURANT_ID_KEY, restaurantId);
        outState.putInt(CURRENT_RADIUS_KEY, currentRadius);
        
        if (currentRestaurantLocation != null) {
            outState.putFloat(RESTAURANT_LAT_KEY, (float) currentRestaurantLocation.latitude);
            outState.putFloat(RESTAURANT_LONG_KEY, (float) currentRestaurantLocation.longitude);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        
        return false;
    }
    
    private void getRestaurantLocation() {
        DatabaseReference dbRef1 = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(restaurantId);
        GeoFire geof = new GeoFire(dbRef1);
        geof.getLocation(EAHCONST.RESTAURANT_POSITION, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    Log.d(TAG, "Downloaded restaurant location");
                    currentRestaurantLocation = location;
                    updateListCurrentRadius();
                } else {
                    Log.e(TAG, "Cannot download restaurant location");
                    Toast.makeText(getApplicationContext(), R.string.alert_restaurant_location, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Cannot download restaurant location. DatabaseError: " + databaseError.getMessage());
                Toast.makeText(getApplicationContext(), R.string.alert_restaurant_location, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void getReferencesToViews() {
        ibIncrease = findViewById(R.id.ib_increase);
        rvRiders = findViewById(R.id.rv_riders);
        ibDecrease = findViewById(R.id.ib_decrease);
        tvRadius = findViewById(R.id.tv_radius);
    }
    
    private void addOnClickListeners() {
        ibDecrease.setOnClickListener(v -> {
            if (currentRadius > 1) {
                currentRadius--;
                tvRadius.setText(String.valueOf(currentRadius));
                updateListCurrentRadius();
            }
        });
        
        ibIncrease.setOnClickListener(v -> {
            currentRadius++;
            tvRadius.setText(String.valueOf(currentRadius));
            updateListCurrentRadius();
        });
    }
    
    private void updateListCurrentRadius() {
        
        // remove any previous listener
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
        
        geoQuery = geoFire.queryAtLocation(currentRestaurantLocation, currentRadius);
        
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "Key entered");
                addRider(key, location);
            }
    
            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, "Key exited");
                removeRider(key);
            }
    
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG, "Rider moved");
                addRider(key, location);
            }
    
            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "Geo query ready");
                
                riders = new ArrayList<>();
            }
    
            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, "GeoQueryError");
            }
        });
    
    }
    
    private void removeRider(String riderId) {
        Rider r = new Rider(riderId);
        riders.remove(r);
        riders = new ArrayList<>(riders);
        Collections.sort(riders);
        adapter.submitList(riders);
    }
    
    private void addRider(String riderId, GeoLocation location) {
        
        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(riderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(EAHCONST.RIDER_EMAIL).getValue() == null) {
                    Log.e(TAG, "Null snapshot!");
                    return;
                }
                
                String riderName = dataSnapshot.child(EAHCONST.RIDER_NAME).getValue(String.class);
                String riderEmail = dataSnapshot.child(EAHCONST.RIDER_EMAIL).getValue(String.class);
    
                Location me   = new Location("");
                Location dest = new Location("");
    
                me.setLatitude(currentRestaurantLocation.latitude);
                me.setLongitude(currentRestaurantLocation.longitude);
    
                dest.setLatitude(location.latitude);
                dest.setLongitude(location.longitude);
    
                float dist = me.distanceTo(dest);
                
                //dist is in meters
                dist = dist/1000;
                
                Rider rider = new Rider(riderId, riderName, riderEmail, dist);
    
                if (riders.isEmpty())
                    riders = new ArrayList<>();
                else
                    riders = new ArrayList<>(riders);
                riders.remove(rider);
                
                riders.add(rider);
    
                Collections.sort(riders);
                
                adapter.submitList(riders);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }
    
    
}
