package it.polito.maddroid.lab3.restaurateur;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChooseRiderActivity extends AppCompatActivity {
    
    public static final String TAG = "ChooseRiderActivity";
    public static final String RIDER_RESULT = "RIDER_RESULT";
    
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
    
    private float currentLat = 37.421998333333335f;
    private float currentLong = -122.08400000000002f;
    
    
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
        
        updateListCurrentRadius();
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.choose_rider);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
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
        
        geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLat, currentLong), currentRadius);
        
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
    
                me.setLatitude(currentLat);
                me.setLongitude(currentLong);
    
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
                adapter.submitList(riders);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }
    
    
}
