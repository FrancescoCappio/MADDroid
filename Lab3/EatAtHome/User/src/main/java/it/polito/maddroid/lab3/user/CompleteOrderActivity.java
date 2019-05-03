package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.TimePickerFragment;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CompleteOrderActivity extends AppCompatActivity {
    
    public static final String TAG = "CompleteOrderActivity";
    public static final String DISHES_KEY = "DISHES_KEY";
    public static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    // general purpose attributes
    private int waitingCount = 0;
    private List<Dish> selectedDishes;
    private Restaurant currentRestaurant;
    
    private String currentUserDefaultAddress;
    private String randomRiderId;
    
    private TextView tvTotalCost;
    private EditText etDeliveryTime;
    private EditText etDeliveryAddress;
    private Button btConfirmOrder;
    private TextView tvDeliveryCost;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);
        
        ActionBar actionBar = getSupportActionBar();
        
        if (actionBar != null) {
            actionBar.setTitle(R.string.complete_order);
            
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        getReferencesToViews();
        
        downloadCurrentUserInfo();
        
        Intent i = getIntent();
        Serializable dishesExtra = i.getSerializableExtra(DISHES_KEY);
        Serializable restaurantExtra = i.getSerializableExtra(RESTAURANT_KEY);
        if (dishesExtra == null || restaurantExtra == null) {
            Utility.showAlertToUser(this, R.string.alert_order_problem);
            finish();
            return;
        }
        
        currentRestaurant = (Restaurant) restaurantExtra;
        selectedDishes = (List<Dish>) dishesExtra;
        String totalCost = computeTotalCost();
        tvTotalCost.setText(totalCost);
        tvDeliveryCost.setText(String.format("%.02f", EAHCONST.DELIVERY_COST) + " €");
    
        etDeliveryTime.setFocusable(false);
        etDeliveryTime.setClickable(true);
        etDeliveryTime.setOnClickListener(v -> showTimePickerDialog());
        
        btConfirmOrder.setOnClickListener(v -> actionConfirmOrder());
        
        setActivityLoading(true);
        Utility.generateRandomRiderId(dbRef, riderId -> {
            randomRiderId = riderId;
            setActivityLoading(false);
        });
        
    }
    
    private void getReferencesToViews() {
        
        tvTotalCost = findViewById(R.id.tv_payment_total);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etDeliveryTime = findViewById(R.id.et_time);
        btConfirmOrder = findViewById(R.id.bt_confirm);
        tvDeliveryCost = findViewById(R.id.tv_delivery_cost);
        
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
    
    private String computeTotalCost() {
        float totalCost = 0;
        
        for (Dish d : selectedDishes) {
            totalCost += d.getQuantity() * d.getPrice();
        }
        
        // consider delivery cost
        totalCost += EAHCONST.DELIVERY_COST;
        
        return String.format("%.02f", totalCost) + " €";
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
    
    private void downloadCurrentUserInfo() {
        setActivityLoading(true);
        
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(currentUser.getUid()).child(EAHCONST.CUSTOMER_ADDRESS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserDefaultAddress = (String) dataSnapshot.getValue();
                etDeliveryAddress.setText(currentUserDefaultAddress);
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setActivityLoading(false);
            }
        });
    }
    
    private void actionConfirmOrder() {
        
        //checks
        
        if (selectedDishes == null || selectedDishes.isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_order_no_dishes);
            return;
        }
        
        if (etDeliveryTime.getText().toString().isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_order_no_time);
            return;
        }
        
        if (!checkValidDeliveryTime()) {
            Utility.showAlertToUser(this, R.string.alert_order_time_not_valid);
            return;
        }
        
        if (etDeliveryAddress.getText().toString().isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_order_no_address);
            return;
        }
        
        if (randomRiderId == null || randomRiderId.isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_order_no_rider);
            return;
        }
        
        setActivityLoading(true);
    
        Map<String,Object> updateMap = new HashMap<>();
        
        String orderId = Utility.generateUUID();
    
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String deliveryTime = etDeliveryTime.getText().toString();
        
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.PENDING;
        
        // put everything related to order from point of view of restaurateur
        String restOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE,
                currentRestaurant.getRestaurantID(),
                orderId);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_STATUS),orderStatus);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DATE),date);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DELIVERY_TIME),deliveryTime);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_CUSTOMER_ID),currentUser.getUid());
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_RIDER_ID),randomRiderId);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_TOTAL_COST),computeTotalCost());
        
        for (Dish d : selectedDishes) {
            updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DISHES_SUBTREE, String.valueOf(d.getDishID())), d.getQuantity());
        }
        
        // now from point of view of user
        String custOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                currentUser.getUid(),
                orderId);
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_STATUS), orderStatus);
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_RESTAURATEUR_ID), currentRestaurant.getRestaurantID());
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_RIDER_ID), randomRiderId);
        
        // now from point of view of rider
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                randomRiderId,
                orderId);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_RESTAURATEUR_ID), currentRestaurant.getRestaurantID());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_CUSTOMER_ID), currentUser.getUid());
    
        
        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            Toast.makeText(CompleteOrderActivity.this, R.string.order_completed, Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(CompleteOrderActivity.this, R.string.alert_error_ordering);
        });
    
    }
    
    public boolean checkValidDeliveryTime() {
        String time = etDeliveryTime.getText().toString();
        
        String[] splits = time.split(":");
        
        if (splits.length != 2)
            return false;
        
        int timeHour = Integer.parseInt(splits[0]);
        int timeMinutes = Integer.parseInt(splits[1]);
    
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int currentMinutes = calendar.get(Calendar.MINUTE);
        
        
        // we want to leave the restaurateur + the rider at least 1 hour to deliver
        
        if (currentHour >= timeHour)
            return false;
        
        if (currentHour < timeHour - 1)
            return true;
        
        if (currentMinutes <= timeMinutes)
            return true;
        
        return false;
    }
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment(etDeliveryTime);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
}
