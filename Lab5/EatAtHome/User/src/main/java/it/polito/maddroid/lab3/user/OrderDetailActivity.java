package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.RatingActivity;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class OrderDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "OrderDetailActivity";
    
    public static final String ORDER_KEY = "ORDER_KEY";
    
    private Order currentOrder;
    private int waitingCount;
    private DatabaseReference dbRef;
    
    private MenuListAdapter adapter;
    
    //views
    private TextView tvOrderDate;
    private TextView tvOrderStatus;
    private TextView tvDeliveryTime;
    private TextView tvTotalCost;
    private TextView tvDeliveryCost;
    private TextView tvRestaurantName;
    private TextView tvRiderName;
    private TextView tvDeliveryAddress;
    private RecyclerView rvDishes;
    private Button btRateRider;
    private Button btRateRestaurant;
    
    private List<Dish> dishList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        
        Intent i = getIntent();
        
        Serializable orderExtra = i.getSerializableExtra(ORDER_KEY);
        
        if (orderExtra != null) {
            currentOrder = (Order) orderExtra;
        } else {
            Log.e(TAG, "Cannot open detail for a null order");
            finish();
        }
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        getReferencesToViews();
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.order_detail);
        }
        
        tvOrderDate.setText(currentOrder.getDate());
        tvDeliveryTime.setText(currentOrder.getDeliveryTime());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        tvDeliveryCost.setText(String.format(Locale.US,"%.02f", EAHCONST.DELIVERY_COST) + " €");
        tvTotalCost.setText(currentOrder.getTotalCost());
        tvRestaurantName.setText(currentOrder.getRestaurantName());
        tvOrderStatus.setText(currentOrder.getOrderStatus().toString());
        getRiderName(currentOrder.getRiderId());
        
        //recycler view
        rvDishes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        
        adapter = new MenuListAdapter(new DishDiffUtilCallBack(), dish -> Log.d(TAG, "Nothing to do"), currentOrder.getRestaurantId(), MenuListAdapter.MODE_ORDER_DISH_LIST);
        
        rvDishes.setAdapter(adapter);
        
        getDishesInfo();
        
        adaptToOrderStatus();
        
        setupClickListeners();
    }
    
    private void getReferencesToViews() {
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvDeliveryTime = findViewById(R.id.tv_time);
        tvRiderName = findViewById(R.id.tv_rider);
        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvTotalCost = findViewById(R.id.tv_payment_total);
        tvDeliveryCost = findViewById(R.id.tv_delivery_cost);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        rvDishes = findViewById(R.id.rv_order_dishes);
        btRateRestaurant = findViewById(R.id.bt_rate_restaurant);
        btRateRider = findViewById(R.id.bt_rate_rider);
    }
    
    private void adaptToOrderStatus() {
        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.COMPLETED) {
            btRateRider.setVisibility(View.VISIBLE);
            btRateRestaurant.setVisibility(View.VISIBLE);
        } else {
            btRateRider.setVisibility(View.GONE);
            btRateRestaurant.setVisibility(View.GONE);
        }
    }
    
    private void setupClickListeners() {
        btRateRider.setOnClickListener(v -> {
            Intent rateRiderIntent = new Intent(OrderDetailActivity.this, RatingActivity.class);
            rateRiderIntent.putExtra(RatingActivity.RATING_MODE_KEY, RatingActivity.RATING_MODE_RIDER);
            rateRiderIntent.putExtra(RatingActivity.RATED_UID_KEY, currentOrder.getRiderId());
            startActivity(rateRiderIntent);
        });
        
        btRateRestaurant.setOnClickListener(v -> {
            Intent rateRiderIntent = new Intent(OrderDetailActivity.this, RatingActivity.class);
            rateRiderIntent.putExtra(RatingActivity.RATING_MODE_KEY, RatingActivity.RATING_MODE_RESTAURANT);
            rateRiderIntent.putExtra(RatingActivity.RATED_UID_KEY, currentOrder.getRestaurantId());
            startActivity(rateRiderIntent);
        });
    }
    
    private synchronized void setActivityLoading(boolean loading) {
        // this method is necessary to show the user when the activity is doing a network operation
        // as downloading data or uploading data
        // how to use: call with loading = true to notify that a new transmission has been started
        // call with loading = false to notify end of transmission
        
        ProgressBar pbLoading = findViewById(R.id.pb_loading);
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
    
    private void getRiderName(String riderId) {
        
        if (riderId == null || riderId.isEmpty()) {
            tvRiderName.setText(R.string.rider_not_assigned);
            return;
        }
        
        setActivityLoading(true);
    
        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(riderId).child(EAHCONST.RIDER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    tvRiderName.setText((CharSequence) dataSnapshot.getValue());
                } else {
                    Utility.showAlertToUser(OrderDetailActivity.this, R.string.alert_error_downloading_info);
                    Log.e(TAG, "Cannot obtain rider's name");
                }
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
    }
    
    private void getDishesInfo() {
    
        setActivityLoading(true);
        
        Query queryRef = dbRef.child(EAHCONST.DISHES_SUB_TREE).child(currentOrder.getRestaurantId()).orderByChild(EAHCONST.DISH_NAME);
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        
                if (!dataSnapshot.hasChildren()) {
                    Utility.showAlertToUser(OrderDetailActivity.this, R.string.alert_error_downloading_info);
                    return;
                }
                
                dishList = new ArrayList<>();
                
                Map<String, Integer> currentOrderDishes = currentOrder.getDishesMap();
                
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String dishId = ds.getKey();
                    
                    if (dishId == null)
                        continue;
                    
                    if (currentOrderDishes.containsKey(dishId)) {
                        String dishName = (String) ds.child(EAHCONST.DISH_NAME).getValue();
                        String description = (String) ds.child(EAHCONST.DISH_DESCRIPTION).getValue();
    
                        float dishPrice = Float.parseFloat(ds.child(EAHCONST.DISH_PRICE).getValue().toString());
                        
                        Dish dish = new Dish(Integer.valueOf(dishId), dishName, dishPrice, description);
                        dish.setQuantity(currentOrderDishes.get(dishId));
                        
                        dishList.add(dish);
                    }
                }
                
                adapter.submitList(dishList);
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
    
        switch (item.getItemId()) {
        
            case android.R.id.home:
                //emulate back pressed
                onBackPressed();
                return true;
        }
        return false;
    }
}
