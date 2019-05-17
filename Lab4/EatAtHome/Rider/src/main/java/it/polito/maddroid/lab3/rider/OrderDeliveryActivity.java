package it.polito.maddroid.lab3.rider;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class OrderDeliveryActivity extends AppCompatActivity {
    
    private static final String TAG = "OrderDeliveryActivity";
    public static final String ORDER_DELIVERY_KEY = "ORDER_DELIVERY_KEY";
    public static final String RIDER_LOCATION = "RIDER_LOCATION";
    
    private RiderOrderDelivery currentOrder;
    private Location lastLocation;
    
    private TextView tvDeliveryTime;
    private TextView tvCostDelivery;
    private TextView tvTotalCost;
    private TextView tvRestaurantAdress;
    private TextView tvDeliveryAdress;
    private ProgressBar pbLoading;
    private TextView tvCustomerName;
    private TextView tvRestaurantName;
    
    private Button btGetFood;
    private Button btDeliverFood;
    private Button btDirectionToRestaurant;
    private Button btDirectionToCustomer;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private int waitingCount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_delivery);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        
        if (intent.getSerializableExtra(ORDER_DELIVERY_KEY) == null) {
            Log.e(TAG, "Cannot show null order delivery");
            finish();
            return;
        }
        
        currentOrder = (RiderOrderDelivery) intent.getSerializableExtra(ORDER_DELIVERY_KEY);

        //to get last location from MainActivity
        lastLocation = MainActivity.getInstance().getLastLocation();
        
        getReferencesToViews();
        
        setDataToView();
        
        setOnClickListeners();
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.order_delivery);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        getCustomerName();
        
        setupButtonsEnable();
    }
    
    private void setupButtonsEnable() {
        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.CONFIRMED) {
            btDeliverFood.setEnabled(false);
            btGetFood.setEnabled(true);
            btDeliverFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            btGetFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_green_accept));
        } else if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.ONGOING) {
            btGetFood.setEnabled(false);
            btDeliverFood.setEnabled(true);
            btGetFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            btDeliverFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_green_accept));
        } else {
            btGetFood.setEnabled(false);
            btDeliverFood.setEnabled(false);
            btGetFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            btDeliverFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
        }
    }
    
    private void getReferencesToViews(){
        
        tvDeliveryTime = findViewById(R.id.tv_time);
        tvCostDelivery = findViewById(R.id.tv_cost_delivery);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvRestaurantAdress = findViewById(R.id.tv_restaurant_address);
        tvDeliveryAdress = findViewById(R.id.tv_delivery_address);
        pbLoading = findViewById(R.id.pb_loading);
        btGetFood = findViewById(R.id.bt_get_food);
        btDeliverFood = findViewById(R.id.bt_deliver_food);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        btDirectionToCustomer = findViewById(R.id.bt_direction_toCustomer);
        btDirectionToRestaurant = findViewById(R.id.bt_direction_toRestaurant);
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
    
    private void setDataToView() {
        
        tvDeliveryTime.setText(currentOrder.getDeliveryTime());
        tvTotalCost.setText(currentOrder.getTotalCost());
        tvCostDelivery.setText(String.format(Locale.US,"%.02f", EAHCONST.DELIVERY_COST) + " €");
        tvRestaurantAdress.setText(currentOrder.getRestaurantAddress());
        tvDeliveryAdress.setText(currentOrder.getDeliveryAddress());
        tvRestaurantName.setText(currentOrder.getRestaurantName());
        
    }
    
    private void setOnClickListeners() {
        
        btDeliverFood.setOnClickListener(v -> deliverFoodAction());
        
        btGetFood.setOnClickListener(v -> getFoodAction());

        btDirectionToRestaurant.setOnClickListener(v -> getDirectionToRestaurant());

        btDirectionToCustomer.setOnClickListener(v -> getDirectionToCustomer());
    }

    private void getDirectionToRestaurant() {
        Intent intent = new Intent(getApplicationContext(), RoutingActivity.class);
        LatLng originLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        LatLng destinationLocation = new LatLng(45.07,7.68);
        intent.putExtra(RoutingActivity.ORIGIN_LOCATION_KEY,originLocation);
        intent.putExtra(RoutingActivity.DESTINATION_LOCATION_KEY,destinationLocation);
        startActivity(intent);
    }

    private void getDirectionToCustomer() {
        Intent intent = new Intent(getApplicationContext(), RoutingActivity.class);
        LatLng originLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        LatLng destinationLocation = new LatLng(45.07,7.6591);
        intent.putExtra(RoutingActivity.ORIGIN_LOCATION_KEY,originLocation.toString());
        intent.putExtra(RoutingActivity.DESTINATION_LOCATION_KEY,destinationLocation.toString());
        startActivity(intent);
    }

    private void getFoodAction() {
        
        setActivityLoading(true);
        
        Map<String,Object> updateMap = new HashMap<>();
        
        //update Rider SubTree
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.ONGOING;
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                currentUser.getUid(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
    
        //update customer subtree
        String custOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                currentOrder.getCustomerId(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_STATUS), orderStatus);
    
        //update Restaurant SubTree
        orderStatus = EAHCONST.OrderStatus.COMPLETED;
        String restaurantOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE,
                currentOrder.getRestaurantId(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(restaurantOrderPath, EAHCONST.REST_ORDER_STATUS), orderStatus);
        
        
        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            currentOrder.setOrderStatus(EAHCONST.OrderStatus.ONGOING);
            setupButtonsEnable();
            Utility.showAlertToUser(this, R.string.get_food_note);
            
        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(this, R.string.alert_error_get_food);
        });
        
    }
    
    
    private void deliverFoodAction() {
        
        setActivityLoading(true);
        
        Map<String,Object> updateMap = new HashMap<>();
        
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.COMPLETED;
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                currentUser.getUid(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
        
        //update customer subtree
        String custOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                currentOrder.getCustomerId(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_STATUS), orderStatus);
        
        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            currentOrder.setOrderStatus(EAHCONST.OrderStatus.COMPLETED);
            setupButtonsEnable();
            Utility.showAlertToUser(this, R.string.deliver_food_note);
        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(this, R.string.alert_error_deliver_food);
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
    
    private void getCustomerName() {
        
        setActivityLoading(true);
        
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(currentOrder.getCustomerId()).child(EAHCONST.CUSTOMER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Utility.showAlertToUser(OrderDeliveryActivity.this, R.string.alert_error_downloading_info);
                    tvCustomerName.setText(R.string.alert_error_downloading_info);
                    setActivityLoading(false);
                    return;
                }
                
                tvCustomerName.setText(dataSnapshot.getValue(String.class));
                
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
    }
}
