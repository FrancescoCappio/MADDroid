package it.polito.maddroid.lab3.rider;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.Utility;

public class ConfirmOrderActivity extends AppCompatActivity {
    
    public static final String TAG = "ConfirmOrderActivity";
    
    
    private int waitingCount = 0;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    private String riderUID;
    private String anotherRiderId;
    private String customerUID;
    private String restaurantUID;
    
    private String orderID;
    private String orderTime;
    private String orderTotalCost;
    private String orderCost;
    private String orderRestaurantAddress;
    private String orderDeliveryAddress;
    
    private TextView tvDeliveryTime;
    private TextView tvCostDelivery;
    private TextView tvTotalCost;
    private TextView tvRestaurantAdress;
    private TextView tvDeliveryAdress;
    
    private FloatingActionButton fabConfirm;
    private FloatingActionButton fabDecline;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        
        setActivityLoading(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        riderUID = currentUser.getUid();
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.confirm_order);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        getReferencesToViews();
        
        Intent i = getIntent();
        
        Order order = (Order) i.getSerializableExtra(CurrentOrderFragment.ORDER_KEY);
        orderID = order.getOrderId();
        orderTime = order.getDeliveryTime();
        orderTotalCost = order.getTotalCost();
        orderDeliveryAddress = order.getDeliveryAddress();
        restaurantUID = order.getRestaurantId();
        customerUID = order.getCustomerId();
        orderCost = i.getStringExtra(CurrentOrderFragment.ORDER_COST_DELIVERY);
        orderRestaurantAddress = i.getStringExtra(CurrentOrderFragment.ORDER_RESTAURANT_ADDRESS);
        
        if (orderID.isEmpty() || orderID == null)
            finish();
        else
            setDataToView();
        
        
        fabConfirm.setOnClickListener(v -> actionConfirmOrder());
        fabDecline.setOnClickListener(v -> actionDeclineOrder());
        
        Utility.generateRandomRiderId(dbRef, new Utility.RandomRiderCaller() {
            @Override
            public void generatedRiderId(String riderId) {
                if (riderId.equals(riderUID)) {
                    Utility.generateRandomRiderId(dbRef, this);
                } else
                    anotherRiderId = riderId;
            }
        });
        setActivityLoading(false);
    
        Utility.showAlertToUser(this, R.string.alert_confirm_decline_order);
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
    
    private void actionDeclineOrder() {
        setActivityLoading(true);
        
        Map<String,Object> updateMap = new HashMap<>();
        
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.DECLINED;
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                riderUID,
                orderID);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
        
        
        orderStatus = EAHCONST.OrderStatus.PENDING;
        riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                anotherRiderId,
                orderID);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_RESTAURATEUR_ID), restaurantUID);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_CUSTOMER_ID), customerUID);
        
        
        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            Toast.makeText(ConfirmOrderActivity.this, R.string.declivne_order_note, Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(ConfirmOrderActivity.this, R.string.alert_error_confirming);
        });
        
    }
    
    private void actionConfirmOrder() {
        setActivityLoading(true);
        
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.CONFIRMED;
        
        Map<String,Object> updateMap = new HashMap<>();
        // now from point of view of rider
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                riderUID,
                orderID);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_RESTAURATEUR_ID), restaurantUID);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_CUSTOMER_ID), customerUID);
    
        String restaurantOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE,
                restaurantUID,
                orderID);
        updateMap.put(EAHCONST.generatePath(restaurantOrderPath, EAHCONST.REST_ORDER_RIDER_ID), riderUID);
    
        String customerOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                customerUID,
                orderID);
        updateMap.put(EAHCONST.generatePath(customerOrderPath, EAHCONST.CUST_ORDER_RIDER_ID), riderUID);
        
        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            Toast.makeText(ConfirmOrderActivity.this, R.string.confirm_order_note, Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(ConfirmOrderActivity.this, R.string.alert_error_confirming);
        });
    }
    
    
    private void setDataToView() {
        tvDeliveryTime.setText(orderTime);
        tvTotalCost.setText(orderTotalCost);
        tvCostDelivery.setText(orderCost);
        tvRestaurantAdress.setText(orderRestaurantAddress);
        tvDeliveryAdress.setText(orderDeliveryAddress);
    }
    
    private void getReferencesToViews() {
        tvDeliveryTime = findViewById(R.id.tv_time);
        tvCostDelivery = findViewById(R.id.tv_cost_delivery);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvRestaurantAdress = findViewById(R.id.tv_restaurant_address);
        tvDeliveryAdress = findViewById(R.id.tv_delivery_address);
        
        fabConfirm = findViewById(R.id.fab_accept);
        fabDecline = findViewById(R.id.fab_decline);
        
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
}
