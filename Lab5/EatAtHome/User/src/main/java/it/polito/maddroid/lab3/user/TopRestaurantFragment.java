package it.polito.maddroid.lab3.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.Utility;


public class TopRestaurantFragment extends Fragment {

    private static final String TAG = "TopRestaurantFragment";

    private List<Restaurant> restaurants;
    RestaurantListAdapter adapter;

    private DatabaseReference dbRef;

    private ProgressBar pbLoading;
    private RecyclerView rvRestaurants;

    private int waitingCount = 0;


    public TopRestaurantFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_top_restaurant, container, false);
        dbRef = FirebaseDatabase.getInstance().getReference();
        rvRestaurants = view.findViewById(R.id.rv_top_restaurant);
        pbLoading = view.findViewById(R.id.pb_loading);

        restaurants = new ArrayList<>();

        downloadRestaurantsInfo();

        // setup list
        adapter = new RestaurantListAdapter(new RestaurantDiffUtilCallback(), restaurant -> {
            // open restaurant detail activity and close current activity
            Intent i = new Intent(getContext(), RestaurantDetailActivity.class);
            i.putExtra(RestaurantDetailActivity.RESTAURANT_KEY, restaurant);
            startActivity(i);
        });

        if (Utility.isTablet(getContext()))
            rvRestaurants.setLayoutManager(new GridLayoutManager(getContext(), 2));
        else
            rvRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRestaurants.setAdapter(adapter);


        return view;
    }

    private void downloadRestaurantsInfo() {
        setActivityLoading(true);

        Query queryRef;
        queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).orderByChild(EAHCONST.RESTAURANT_REVIEW_AVG).limitToLast(10);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                restaurants = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    // do something with the individual restaurant
                    String restaurantId = ds.getKey();
                    String name = (String) ds.child(EAHCONST.RESTAURANT_NAME).getValue();
                    String description = (String) ds.child(EAHCONST.RESTAURANT_DESCRIPTION).getValue();
                    String address = (String) ds.child(EAHCONST.RESTAURANT_ADDRESS).getValue();
                    String phone = (String) ds.child(EAHCONST.RESTAURANT_PHONE).getValue();
                    String email = (String) ds.child(EAHCONST.RESTAURANT_EMAIL).getValue();
                    String categoriesIds = (String) ds.child(EAHCONST.RESTAURANT_CATEGORIES).getValue();
                    String timetable = (String) ds.child(EAHCONST.RESTAURANT_TIMETABLE).getValue();

                    Restaurant r = new Restaurant(restaurantId, name, description, address, phone, email, categoriesIds, timetable);

                    if (ds.child(EAHCONST.RESTAURANT_REVIEW_COUNT).getValue() != null &&
                            ds.child(EAHCONST.RESTAURANT_REVIEW_AVG).getValue() != null) {
                        Long reviewCount = ds.child(EAHCONST.RESTAURANT_REVIEW_COUNT).getValue(Long.class);
                        Double reviewAvg = ds.child(EAHCONST.RESTAURANT_REVIEW_AVG).getValue(Double.class);

                        r.setReviewAvg(reviewAvg.floatValue());
                        r.setReviewCount(reviewCount.intValue());
                    }

                    if (ds.child(EAHCONST.RESTAURANT_AVG_ORDER_TIME).getValue() != null) {
                        int avgOrderTime = ds.child(EAHCONST.RESTAURANT_AVG_ORDER_TIME).getValue(Long.class).intValue();
                        r.setAvgOrderTime(avgOrderTime);
                    }

                    if (ds.child(EAHCONST.RESTAURANT_POSITION).getValue() != null) {
                        double lat = ds.child(EAHCONST.RESTAURANT_POSITION).child("l").child("0").getValue(Double.class);
                        double longit = ds.child(EAHCONST.RESTAURANT_POSITION).child("l").child("1").getValue(Double.class);
                        EAHCONST.GeoLocation geoLocation = new EAHCONST.GeoLocation(lat, longit);
                        r.setGeoLocation(geoLocation);
                    }
                    restaurants.add(r);
                }
                Collections.reverse(restaurants);
                adapter.submitList(restaurants);
                setActivityLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }

    private synchronized void setActivityLoading(boolean loading) {
        // this method is necessary to show the user when the activity is doing a network operation
        // as downloading data or uploading data
        // how to use: call with loading = true to notify that a new transmission has been started
        // call with loading = false to notify end of transmission


        if (loading) {
            if (waitingCount == 0)
                pbLoading.setVisibility(View.VISIBLE);
            waitingCount++;
        } else {
            waitingCount--;
            if (waitingCount == 0)
                pbLoading.setVisibility(View.INVISIBLE);
        }
    }



}
