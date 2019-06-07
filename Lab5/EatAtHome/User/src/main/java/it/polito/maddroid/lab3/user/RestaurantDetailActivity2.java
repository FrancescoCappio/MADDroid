package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;


public class RestaurantDetailActivity2 extends AppCompatActivity implements DishesListFragment.TotalCostListener {
    
    public static final String TAG = "RestaurantDetailActivity2";
    
    public static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    public static final String CURRENT_ITEM_KEY = "CURRENT_ITEM_KEY";
    
    private RestaurantSwipeViewAdapter swipeViewAdapter;
    
    private Restaurant currentRestaurant;
    
    private MenuItem menuOrder;
    
    private ViewPager viewPager;
    
    private List<Dish> chosenDishes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail2);
    
        if (savedInstanceState == null) {
            currentRestaurant = (Restaurant) getIntent().getSerializableExtra(RESTAURANT_KEY);
        } else {
            currentRestaurant = (Restaurant) savedInstanceState.getSerializable(RESTAURANT_KEY);
        }
    
        swipeViewAdapter = new RestaurantSwipeViewAdapter(getSupportFragmentManager(), currentRestaurant);
    
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(swipeViewAdapter);
        viewPager.setSaveFromParentEnabled(false);
    
        viewPager.setOffscreenPageLimit(6);
    
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    
        if (savedInstanceState != null) {
            viewPager.setCurrentItem(savedInstanceState.getInt(CURRENT_ITEM_KEY));
        }
    
        ActionBar actionBar = getSupportActionBar();
    
        if (actionBar != null) {
            actionBar.setTitle(currentRestaurant.getName());
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RESTAURANT_KEY, currentRestaurant);
        
        outState.putInt(CURRENT_ITEM_KEY, viewPager.getCurrentItem());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            
            case R.id.menu_order:
                actionCompleteOrder();
                return true;
        }
        
        return false;
    }
    
    private void actionCompleteOrder() {
        Intent intent = new Intent(getApplicationContext(), CompleteOrderActivity.class);
    
//        List<Dish> chosenDishes = swipeViewAdapter.getChosenDishes();
        
        if (chosenDishes != null && !chosenDishes.isEmpty()) {
            intent.putExtra(CompleteOrderActivity.DISHES_KEY, (Serializable) chosenDishes);
            intent.putExtra(CompleteOrderActivity.RESTAURANT_KEY, currentRestaurant);
            startActivity(intent);
        }
    }
    
    @Override
    public void updateTotalCost(List<Dish> chosenDishes) {
        if (chosenDishes == null || chosenDishes.isEmpty()) {
            if (menuOrder != null)
                menuOrder.setVisible(false);
            return;
        }
        this.chosenDishes = chosenDishes;
    
        String completeOrder = getString(R.string.complete_order);
    
        String total = computeTotalCost(chosenDishes);
    
        String msg = completeOrder + " " + total;
        
        if (menuOrder != null) {
            menuOrder.setVisible(true);
            menuOrder.setTitle(msg);
        }
    }
    
    
    private String computeTotalCost(List<Dish> chosenDishes) {
        float totalCost = 0;
        
        for (Dish d : chosenDishes) {
            totalCost += d.getQuantity() * d.getPrice();
        }
        
        return String.format(Locale.US, "%.02f", totalCost) + " €";
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.restaurant_detail_menu, menu);
        
        menuOrder = menu.findItem(R.id.menu_order);
    
        updateTotalCost(chosenDishes);
        
        return true;
    }
}
