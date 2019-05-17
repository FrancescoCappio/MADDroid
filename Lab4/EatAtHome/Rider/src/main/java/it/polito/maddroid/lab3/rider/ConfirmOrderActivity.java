package it.polito.maddroid.lab3.rider;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Locale;
import java.util.Map;

import it.polito.maddroid.lab3.common.ChooseRiderActivity;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.Rider;
import it.polito.maddroid.lab3.common.Utility;

public class ConfirmOrderActivity extends AppCompatActivity {
    
    public static final String TAG = "ConfirmOrderActivity";
    public static final String RIDER_ORDER_DELIVERY_KEY = "RIDER_ORDER_DELIVERY_KEY";
    
    private int waitingCount = 0;
    
    private static final int CHOOSE_RIDER_REQUEST_CODE = 315153;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    private TextView tvDeliveryTime;
    private TextView tvCostDelivery;
    private TextView tvTotalCost;
    private TextView tvRestaurantAdress;
    private TextView tvDeliveryAdress;
    
    private String riderUID;
    private String nextRiderId;
    private RiderOrderDelivery currentDelivery;
    
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
        
        if (i.getSerializableExtra(RIDER_ORDER_DELIVERY_KEY) == null) {
            Log.e(TAG, "Cannot show null order for confirm");
            finish();
            return;
        }
        
        currentDelivery = (RiderOrderDelivery) i.getSerializableExtra(RIDER_ORDER_DELIVERY_KEY);
        
        setDataToView();
        
        fabConfirm.setOnClickListener(v -> actionConfirmOrder());
        fabDecline.setOnClickListener(v -> actionDeclineOrder());
        
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
        startChooseRiderActivity();
        
    }
    
    private void startChooseRiderActivity() {
        
        Intent intent = new Intent(getApplicationContext(), ChooseRiderActivity.class);
        intent.putExtra(ChooseRiderActivity.RESTAURANT_ID_KEY, currentDelivery.getRestaurantId());
        startActivityForResult(intent, CHOOSE_RIDER_REQUEST_CODE);
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }
        
        if (data == null) {
            Log.e(TAG, "Data extra null");
            return;
        }
        
        if (requestCode == CHOOSE_RIDER_REQUEST_CODE) {
            Rider rider = (Rider) data.getSerializableExtra(ChooseRiderActivity.RIDER_RESULT);
            
            if (rider == null) {
                Log.e(TAG, "The received rider is null");
            } else {
                nextRiderId = rider.getId();
                saveNewRidersInfo();
            }
        }
    }
    
    private void saveNewRidersInfo() {
        setActivityLoading(true);
    
        Map<String,Object> updateMap = new HashMap<>();
    
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.DECLINED;
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                riderUID,
                currentDelivery.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
    
    
        orderStatus = EAHCONST.OrderStatus.PENDING;
        riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                nextRiderId,
                currentDelivery.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_RESTAURATEUR_ID), currentDelivery.getRestaurantId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_CUSTOMER_ID), currentDelivery.getCustomerId());
    
    
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
                currentDelivery.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
    
        String restaurantOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE,
                currentDelivery.getRestaurantId(),
                currentDelivery.getOrderId());
        updateMap.put(EAHCONST.generatePath(restaurantOrderPath, EAHCONST.REST_ORDER_RIDER_ID), riderUID);
    
        String customerOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                currentDelivery.getCustomerId(),
                currentDelivery.getOrderId());
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
        tvDeliveryTime.setText(currentDelivery.getDeliveryTime());
        tvTotalCost.setText(currentDelivery.getTotalCost());
        tvCostDelivery.setText(String.format(Locale.US,"%.02f", EAHCONST.DELIVERY_COST) + " €");
        tvRestaurantAdress.setText(currentDelivery.getRestaurantAddress());
        tvDeliveryAdress.setText(currentDelivery.getDeliveryAddress());
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
