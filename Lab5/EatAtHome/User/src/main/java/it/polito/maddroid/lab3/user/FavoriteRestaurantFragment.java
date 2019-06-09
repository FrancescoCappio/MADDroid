package it.polito.maddroid.lab3.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;


public class FavoriteRestaurantFragment extends Fragment {

    private static final String TAG = "TopRestaurantFragment";

    private List<Restaurant> mostOrderedRestaurants;
    private List<Restaurant> favoriteRestaurants;
    RestaurantListAdapter mostOrderedAdapter;
    RestaurantListAdapter favoriteAdapter;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    private TextView tvMostOrderedTitle;
    private TextView tvFavoriteTitle;
    private ProgressBar pbLoading;
    private RecyclerView rvFavoriteRestaurants;
    private RecyclerView rvMostOrderedRestaurants;
    
    private List<String> favoriteIds;
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
    
        rvFavoriteRestaurants = view.findViewById(R.id.rv_favorite_restaurant);
        rvMostOrderedRestaurants = view.findViewById(R.id.rv_most_ordered_restaurant);
        tvMostOrderedTitle = view.findViewById(R.id.tv_most_ordered_restaurants_title);
        tvFavoriteTitle = view.findViewById(R.id.tv_favorite_restaurants_title);
        pbLoading = view.findViewById(R.id.pb_loading);

        mostOrderedRestaurants = new ArrayList<>();
        restaurantsCount = new HashMap<>();

        downloadRestaurantsCount();

        // setup list of most ordered
        mostOrderedAdapter = new RestaurantListAdapter(new RestaurantDiffUtilCallback(), restaurant -> {
            // open restaurant detail activity and close current activity
            Intent i = new Intent(getContext(), RestaurantDetailActivity2.class);
            i.putExtra(RestaurantDetailActivity2.RESTAURANT_KEY, restaurant);
            startActivity(i);
        },RestaurantListAdapter.MODE_HORIZONTAL);
        
        rvMostOrderedRestaurants.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        rvMostOrderedRestaurants.setAdapter(mostOrderedAdapter);
    
        // setup list of favorites
        favoriteAdapter = new RestaurantListAdapter(new RestaurantDiffUtilCallback(), restaurant -> {
            // open restaurant detail activity and close current activity
            Intent i = new Intent(getContext(), RestaurantDetailActivity2.class);
            i.putExtra(RestaurantDetailActivity2.RESTAURANT_KEY, restaurant);
            startActivity(i);
        },RestaurantListAdapter.MODE_HORIZONTAL);
    
        rvFavoriteRestaurants.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        rvFavoriteRestaurants.setAdapter(favoriteAdapter);
        
        downloadFavoriteRestaurantsIds();

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
                    manageTopRestaurantsVisibility();
                } else{
                    restaurantsCount = sortByComparator(restaurantsCount);
                    downloadMostOrderedRestaurantsInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }
    
    private void downloadFavoriteRestaurantsIds() {
        setActivityLoading(true);
        
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(currentUser.getUid()).child(EAHCONST.CUSTOMER_FAVORITE_RESTAURANT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favoriteIds = new ArrayList<>();
                
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    favoriteIds.add(id);
                }
                
                downloadFavoriteRestaurantsInfo();
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
    }

    private void downloadMostOrderedRestaurantsInfo() {

        for(Map.Entry<String,Integer> entry : restaurantsCount.entrySet()){

            Query queryRef;
            queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(entry.getKey());

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange Called");
                    if (dataSnapshot.getValue() != null ){
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
                        mostOrderedRestaurants.add(r);

                        if (restaurantsCount.size() == mostOrderedRestaurants.size()) {
                            manageTopRestaurantsVisibility();
                            mostOrderedAdapter.submitList(mostOrderedRestaurants);
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
    
    private void downloadFavoriteRestaurantsInfo() {
    
        favoriteRestaurants = new ArrayList<>();
        
        if (favoriteIds.isEmpty()) {
            setActivityLoading(false);
            manageFavoriteRestaurantsVisibility();
            return;
        }
        
        for(String id : favoriteIds){

            dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange Called");
                    if (dataSnapshot.getValue() != null){
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
                        favoriteRestaurants.add(r);
                    }
                    
                    if (favoriteRestaurants.size() == favoriteIds.size()) {
                        favoriteAdapter.submitList(favoriteRestaurants);
                        setActivityLoading(false);
                    }
                    manageFavoriteRestaurantsVisibility();
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

        List<HashMap.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

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

    private void manageTopRestaurantsVisibility() {

        if (mostOrderedRestaurants.size() == 0) {
            rvMostOrderedRestaurants.setVisibility(View.GONE);
            tvMostOrderedTitle.setText(R.string.no_top_restaurants);
        } else {
            rvMostOrderedRestaurants.setVisibility(View.VISIBLE);
            tvMostOrderedTitle.setText(R.string.most_ordered_from);
        }
    }
    
    private void manageFavoriteRestaurantsVisibility() {
        
        if (favoriteRestaurants.size() == 0) {
            rvFavoriteRestaurants.setVisibility(View.GONE);
            tvFavoriteTitle.setText(R.string.no_favorite_restaurants);
        } else {
            rvFavoriteRestaurants.setVisibility(View.VISIBLE);
            tvFavoriteTitle.setText(R.string.favorite_title);
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
