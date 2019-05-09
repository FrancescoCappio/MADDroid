package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.RestaurantCategory;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class RestaurantSearchActivity extends AppCompatActivity {
    
    private static final String TAG = "RestaurantSearchActvty";
    
    private List<Restaurant> restaurants;
    private List<String> restaurantsInCategory;
    
    private DatabaseReference dbRef;
    
    private EditText etSearch;
    private ImageView ivSearch;
    private RecyclerView rvRestaurants;
    
    private RestaurantCategory restaurantCategory;
    
    RestaurantListAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_search);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
    
        getReferencesToViews();
        
        restaurants = new ArrayList<>();
        
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
        
        // setup list
        adapter = new RestaurantListAdapter(new RestaurantDiffUtilCallback(), restaurant -> {
            // open restaurant detail activity and close current activity
            Intent i = new Intent(getApplicationContext(), RestaurantDetailActivity.class);
            i.putExtra(RestaurantDetailActivity.RESTAURANT_KEY, restaurant);
            startActivity(i);
            finish();
        });
        rvRestaurants.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvRestaurants.setAdapter(adapter);
        
        setupClickListeners();
        
        downloadRestaurantsInfo("");
    }
    
    private void getReferencesToViews() {
        etSearch = findViewById(R.id.et_search);
        ivSearch = findViewById(R.id.iv_search);
        rvRestaurants = findViewById(R.id.rv_restaurants);
    }
    
    private void setupClickListeners() {
        ivSearch.setOnClickListener(v -> downloadRestaurantsInfo(etSearch.getText().toString()));
    
        // also search on enter pressed
        etSearch.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                downloadRestaurantsInfo(etSearch.getText().toString());
                return true;
            }
            return false;
        });
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
    
    private void downloadRestaurantsInfo(String query) {
        
        if (query == null) {
            query = "";
        }
    
        Query queryRef;
        
        if (query.isEmpty()) {
            if (restaurantCategory == null) {
                queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).orderByChild(EAHCONST.RESTAURANT_NAME).startAt(query).endAt(query + "\uf8ff").limitToFirst(10);
            } else {
                queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).orderByChild(EAHCONST.RESTAURANT_NAME).startAt(query).endAt(query + "\uf8ff").limitToFirst(100);
            }
        } else {
            queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).orderByChild(EAHCONST.RESTAURANT_NAME).startAt(query).endAt(query + "\uf8ff");
        }
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                restaurants = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    // do somethind with the individual restaurant
                    String restaurantId = ds.getKey();
                    String name = (String) ds.child(EAHCONST.RESTAURANT_NAME).getValue();
                    String description = (String) ds.child(EAHCONST.RESTAURANT_DESCRIPTION).getValue();
                    String address = (String) ds.child(EAHCONST.RESTAURANT_ADDRESS).getValue();
                    String phone = (String) ds.child(EAHCONST.RESTAURANT_PHONE).getValue();
                    String email = (String) ds.child(EAHCONST.RESTAURANT_EMAIL).getValue();
                    String categoriesIds = (String) ds.child(EAHCONST.RESTAURANT_CATEGORIES).getValue();
                    
                    Restaurant r = new Restaurant(restaurantId, name, description, address, phone, email, categoriesIds);
                    
                    restaurants.add(r);
                }
                // now that we have downloaded all the restaurants matching the name we need to filter them considering the chosen category
                filterRestaurantsByCategory();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }
    
    private void filterRestaurantsByCategory() {
        
        if (restaurants.isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_no_restaurant_found);
            return;
        }
        
        if (restaurantCategory == null) {
            // no category filter: submit the list as is
            adapter.submitList(restaurants);
            return;
        }
        
        List<Restaurant> newRests = new ArrayList<>();
    
        for (Restaurant r : restaurants) {
            if (r.matchesCategoryId(restaurantCategory.getId()))
                newRests.add(r);
        }
    
        if (newRests.isEmpty())
            Utility.showAlertToUser(this, R.string.alert_no_restaurant_found);
        adapter.submitList(newRests);
    }
}
