package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CompleteOrderActivity extends AppCompatActivity {
    
    public static final String TAG = "CompleteOrderActivity";
    public static final String DISHES_KEY = "DISHES_KEY";
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    // general purpose attributes
    private int waitingCount = 0;
    private List<Dish> selectedDishes;
    
    private String currentUserDefaultAddress;
    
    private TextView tvTotalCost;
    private EditText etDeliveryTime;
    private EditText etDeliveryAddress;
    
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
        if (dishesExtra == null) {
            Utility.showAlertToUser(this, R.string.alert_order_problem);
            finish();
            return;
        }
        
        selectedDishes = (List<Dish>) dishesExtra;
        String totalCost = computeTotalCost();
        tvTotalCost.setText(totalCost);
        
    }
    
    private void getReferencesToViews() {
        
        tvTotalCost = findViewById(R.id.tv_payment_total);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etDeliveryTime = findViewById(R.id.et_time);
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
}
