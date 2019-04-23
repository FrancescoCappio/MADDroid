package it.polito.maddroid.lab3.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.polito.maddroid.lab3.common.EAHCONST;


public class RestaurantFragment extends Fragment {

    private ListView lvResturansts;

    private static final String TAG = "RestaurantFragment";

    RestaurantAdapter adapter;

    private DatabaseReference dbRef;

    private List<Restaurant> restaurants;


    public RestaurantFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);

        lvResturansts = view.findViewById(R.id.lv_Restaurant);

        restaurants = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference();

        downloadRestaurantsInfo();



        //adapter = new RestaurantAdapter(new ArrayList<>(restaurants), getContext());

        lvResturansts.setAdapter(adapter);



        return view;
    }

    private void downloadRestaurantsInfo() {
        Query queryRef = dbRef
                .child(EAHCONST.RESTAURANTS_SUB_TREE)
                .orderByChild(EAHCONST.RESTAURANT_NAME);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    // do somethind with the individual restaurant
                    String restaurantId = (String) ds.getKey();
                    String name = (String) ds.child(EAHCONST.RESTAURANT_NAME).getValue();
                    String description = (String) ds.child(EAHCONST.RESTAURANT_DESCRIPTION).getValue();
                    String address = (String) ds.child(EAHCONST.RESTAURANT_ADDRESS).getValue();
                    String phone = (String) ds.child(EAHCONST.RESTAURANT_PHONE).getValue();

                    Restaurant r = new Restaurant(restaurantId, name, description, address, phone);

                    restaurants.add(r);
                }
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }

    private void setupAdapter() {
        RestaurantAdapter adapter = new RestaurantAdapter(restaurants, getContext());
        lvResturansts.setAdapter(adapter);

    }




}
