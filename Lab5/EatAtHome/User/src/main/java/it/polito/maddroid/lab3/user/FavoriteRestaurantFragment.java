package it.polito.maddroid.lab3.user;

import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.Utility;


public class FavoriteRestaurantFragment extends Fragment {

    private static final String TAG = "TopRestaurantFragment";

    private List<Restaurant> restaurants;
    RestaurantListAdapter adapter;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    private TextView tvNoOrdersPlaceHolder;
    private ProgressBar pbLoading;
    private RecyclerView rvRestaurants;
    private HashMap<String,Integer> restaurantsCount;

    private int waitingCount = 0;


    public FavoriteRestaurantFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_favorite_restaurant, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        rvRestaurants = view.findViewById(R.id.rv_favorite_restaurant);
        tvNoOrdersPlaceHolder = view.findViewById(R.id.tv_no_order);
        pbLoading = view.findViewById(R.id.pb_loading);

        restaurants = new ArrayList<>();
        restaurantsCount = new HashMap<>();

        downloadRestaurantsCount();

        // setup list
        adapter = new RestaurantListAdapter(new RestaurantDiffUtilCallback(), restaurant -> {
            // open restaurant detail activity and close current activity
            Intent i = new Intent(getContext(), RestaurantDetailActivity.class);
            i.putExtra(RestaurantDetailActivity.RESTAURANT_KEY, restaurant);
            startActivity(i);
        },RestaurantListAdapter.MODE_HORIZONTAL);
        
        rvRestaurants.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        rvRestaurants.setAdapter(adapter);

        return view;
    }

    private void downloadRestaurantsCount() {
        setActivityLoading(true);
        Query queryRef;
        queryRef = dbRef.child(EAHCONST.ORDERS_CUST_SUBTREE).child(currentUser.getUid())
                .orderByChild(EAHCONST.CUST_ORDER_RESTAURATEUR_ID);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    // do something with the individual restaurant
                    String restaurantId = (String) ds.child(EAHCONST.CUST_ORDER_RESTAURATEUR_ID).getValue();
                    EAHCONST.OrderStatus orderStatus = ds.child(EAHCONST.CUST_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);
                    if (orderStatus == EAHCONST.OrderStatus.COMPLETED) {
                        if (restaurantsCount.containsKey(restaurantId))
                            restaurantsCount.put(restaurantId, restaurantsCount.get(restaurantId) + 1);
                        else
                            restaurantsCount.put(restaurantId, 1);
                    }
                }
                if(restaurantsCount.size() == 0 ) {
                    setActivityLoading(false);
                    manageVisibility();
                }
                else{
                    restaurantsCount = sortByComparator(restaurantsCount);
                    downloadRestaurantsInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }

    private void downloadRestaurantsInfo() {

        for(Map.Entry<String,Integer> entry : restaurantsCount.entrySet()){

            Query queryRef;
            queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(entry.getKey());

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange Called");
                    if (dataSnapshot != null ){
                        // do something with the individual restaurant
                        String restaurantId = dataSnapshot.getKey();
                        String name = (String) dataSnapshot.child(EAHCONST.RESTAURANT_NAME).getValue();
                        String description = (String) dataSnapshot.child(EAHCONST.RESTAURANT_DESCRIPTION).getValue();
                        String address = (String) dataSnapshot.child(EAHCONST.RESTAURANT_ADDRESS).getValue();
                        String phone = (String) dataSnapshot.child(EAHCONST.RESTAURANT_PHONE).getValue();
                        String email = (String) dataSnapshot.child(EAHCONST.RESTAURANT_EMAIL).getValue();
                        String categoriesIds = (String) dataSnapshot.child(EAHCONST.RESTAURANT_CATEGORIES).getValue();
                        String timetable = (String) dataSnapshot.child(EAHCONST.RESTAURANT_TIMETABLE).getValue();

                        Restaurant r = new Restaurant(restaurantId, name, description, address, phone, email, categoriesIds, timetable);

                        if (dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_COUNT).getValue() != null &&
                                dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_AVG).getValue() != null) {
                            Long reviewCount = dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_COUNT).getValue(Long.class);
                            Double reviewAvg = dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_AVG).getValue(Double.class);

                            r.setReviewAvg(reviewAvg.floatValue());
                            r.setReviewCount(reviewCount.intValue());
                        }
                        if (dataSnapshot.child(EAHCONST.RESTAURANT_AVG_ORDER_TIME).getValue() != null) {
                            int avgOrderTime = dataSnapshot.child(EAHCONST.RESTAURANT_AVG_ORDER_TIME).getValue(Long.class).intValue();
                            r.setAvgOrderTime(avgOrderTime);
                        }

                        if (dataSnapshot.child(EAHCONST.RESTAURANT_POSITION).getValue() != null) {
                            double lat = dataSnapshot.child(EAHCONST.RESTAURANT_POSITION).child("l").child("0").getValue(Double.class);
                            double longit = dataSnapshot.child(EAHCONST.RESTAURANT_POSITION).child("l").child("1").getValue(Double.class);
                            EAHCONST.GeoLocation geoLocation = new EAHCONST.GeoLocation(lat, longit);
                            r.setGeoLocation(geoLocation);
                        }
                        restaurants.add(r);

                        if (restaurantsCount.size() == restaurants.size()) {
                            manageVisibility();
                            adapter.submitList(restaurants);
                            setActivityLoading(false);
                        }

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled called");
                    setActivityLoading(false);
                }
            });
        }
    }

    private static HashMap<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap)
    {

        List<HashMap.Entry<String, Integer>> list = new LinkedList<HashMap.Entry<String, Integer>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<HashMap.Entry<String, Integer>>()
        {
            public int compare(HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2)
            {
                    return o2.getValue().compareTo(o1.getValue());
            }
        });

        // to show just 5 Favorite restaurant
        if(list.size() > 5)
            list = list.subList(0,4);

        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (HashMap.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private void manageVisibility() {

        if (restaurants.size() == 0) {
            tvNoOrdersPlaceHolder.setVisibility(View.VISIBLE);
            tvNoOrdersPlaceHolder.setText(R.string.no_order);
            rvRestaurants.setVisibility(View.GONE);
        } else {

            tvNoOrdersPlaceHolder.setVisibility(View.GONE);
            rvRestaurants.setVisibility(View.VISIBLE);
        }
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
