package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class RestaurantSearchActivity extends AppCompatActivity {
    
    private static final String TAG = "RestaurantSearchActivity";
    
    private List<Restaurant> restaurants;
    
    private DatabaseReference dbRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_search);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
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
//                setupAdapter();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }
}
