package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.RestaurantCategory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class RestaurantSearchActivity extends AppCompatActivity {
    
    private static final String TAG = "RestaurantSearchActvty";
    
    private List<Restaurant> restaurants;
    
    private DatabaseReference dbRef;
    
    private EditText etSearch;
    
    private RestaurantCategory restaurantCategory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_search);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
    
        getReferencesToViews();
        
        Intent launchIntent = getIntent();
        
        restaurantCategory = (RestaurantCategory) launchIntent.getSerializableExtra(EAHCONST.RESTAURANT_CATEGORY_EXTRA);
        
        if (restaurantCategory == null) {
            // no category selected
            
        } else {
            // a category has been selected for search
            String prefix = getString(R.string.search_restaurant_in_category);
            etSearch.setHint(prefix + " " + restaurantCategory.getName());
        }
        
        ActionBar actionBar = getSupportActionBar();
        
        if (actionBar != null) {
            actionBar.setTitle(R.string.restaurant_search);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    
        // automatically put focus on search edit text and open the keyboard
        etSearch.requestFocus();
    }
    
    private void getReferencesToViews() {
        etSearch = findViewById(R.id.et_search);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        
        return true;
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
