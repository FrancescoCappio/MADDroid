package it.polito.maddroid.lab3.restaurateur;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.Utility;

public class OrderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailsActivity";
    private Order currentOrder;
    private int waitingCount;
    public final static String ORDER_KEY = "ORDER_KEY";

    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;

    private String nextRiderId;

    private MenuListAdapter adapter;

    private TextView tvRider;
    private TextView tvCustomer;
    private TextView tvTotPrice;
    private TextView tvDeliveryAddress;
    private TextView tvDeliveryTime;
    private TextView tvOrderStatus;
    private TextView tvDeliveryData;
    private RecyclerView rvDishes;
    private FloatingActionButton confirmOrder;
    private FloatingActionButton declineOrder;


    private List<Dish> dishList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        getReferencesToViews();
        setupClickListeners();
        Utility.generateRandomRiderId(dbRef, new Utility.RandomRiderCaller() {
            @Override
            public void generatedRiderId(String riderId) {
                nextRiderId = riderId;
            }
        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.order_detail);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Resources res = getResources();



        Intent i = getIntent();

        Serializable orderExtra = i.getSerializableExtra(ORDER_KEY);

        if (orderExtra != null) {
            currentOrder = (Order) orderExtra;
        } else {
            Log.e(TAG, "Cannot open detail for a null order");
            finish();
        }

        if(currentOrder.getOrderStatus().equals(EAHCONST.OrderStatus.PENDING)){
            declineOrder.show();
            confirmOrder.show();

        }
        else {
            declineOrder.hide();
            confirmOrder.hide();
        }



        tvOrderStatus.setText(currentOrder.getOrderStatus().toString());
        tvDeliveryData.setText(currentOrder.getDate());
        tvDeliveryTime.setText(currentOrder.getDeliveryTime());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        String [] suddivido = currentOrder.getTotalCost().split(" ");
        float costo = Float.parseFloat(suddivido[0]) - EAHCONST.DELIVERY_COST;
        tvTotPrice.setText(String.format("%.02f", costo) + " €");
        getRiderName(currentOrder.getRiderId());
        tvCustomer.setText(currentOrder.getCustomerName());

        //recycler view
        rvDishes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        adapter = new MenuListAdapter(new DishDiffUtilCallBack(), dish -> Log.d(TAG, "Nothing to do"), currentUser.getUid(), MenuListAdapter.MODE_ORDER_DISH_LIST);

        rvDishes.setAdapter(adapter);

        getDishesInfo();


    }




    private void setupClickListeners() {
        declineOrder.setOnClickListener( v -> {
            Map<String,Object> updateMap = new HashMap<>();
            updateMap.put(EAHCONST.generatePath(EAHCONST.ORDERS_CUST_SUBTREE, currentOrder.getCustomerId(), currentOrder.getOrderId(), EAHCONST.CUST_ORDER_STATUS), EAHCONST.OrderStatus.DECLINED);
            updateMap.put(EAHCONST.generatePath(EAHCONST.ORDERS_REST_SUBTREE, currentUser.getUid(), currentOrder.getOrderId(), EAHCONST.REST_ORDER_STATUS), EAHCONST.OrderStatus.DECLINED);

            dbRef.updateChildren(updateMap).addOnSuccessListener(s -> {
                Log.d(TAG, "Success declined order");
                Utility.showAlertToUser(this,R.string.notify_save_ok);
                setActivityLoading(false);

            }).addOnFailureListener(e -> {

                Log.e(TAG, "Database error while declined order: " + e.getMessage());
                Utility.showAlertToUser(this, R.string.notify_save_ko);
                setActivityLoading(false);
            });

        });

        confirmOrder.setOnClickListener(c-> {
            Map<String,Object> updateMap = new HashMap<>();

            updateMap.put(EAHCONST.generatePath(EAHCONST.ORDERS_CUST_SUBTREE, currentOrder.getCustomerId(), currentOrder.getOrderId(), EAHCONST.CUST_ORDER_STATUS), EAHCONST.OrderStatus.CONFIRMED);
            updateMap.put(EAHCONST.generatePath(EAHCONST.ORDERS_REST_SUBTREE, currentUser.getUid(), currentOrder.getOrderId(), EAHCONST.REST_ORDER_STATUS), EAHCONST.OrderStatus.CONFIRMED);
            // now from point of view of rider
            String riderOrderPath = EAHCONST.generatePath(EAHCONST.ORDERS_RIDER_SUBTREE, nextRiderId, currentOrder.getOrderId());
            updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), currentOrder.getOrderStatus());
            updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_RESTAURATEUR_ID), currentUser.getUid());
            updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_CUSTOMER_ID), currentOrder.getCustomerId());

            dbRef.updateChildren(updateMap).addOnSuccessListener(s -> {
                Log.d(TAG, "Success confirmed order");
                Utility.showAlertToUser(this,R.string.notify_save_ok);
                setActivityLoading(false);

            }).addOnFailureListener(e -> {

                Log.e(TAG, "Database error while confirmed order: " + e.getMessage());
                Utility.showAlertToUser(this, R.string.notify_save_ko);
                setActivityLoading(false);
            });


        });

    }


    private void getReferencesToViews() {
            //get references to views
        tvRider = findViewById(R.id.tv_rider);
        tvCustomer = findViewById(R.id.tv_customer);
        tvTotPrice = findViewById(R.id.tv_payment_total);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvDeliveryTime = findViewById(R.id.tv_time);
        tvDeliveryData = findViewById(R.id.tv_order_date);
        tvDeliveryData = findViewById(R.id.tv_order_date);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        declineOrder = findViewById(R.id.fab_decline);
        confirmOrder = findViewById(R.id.fab_accept);
        rvDishes = findViewById(R.id.rv_order_dishes);

    }

    private void getRiderName(String riderId) {

        if (riderId == null || riderId.isEmpty()) {
            tvRider.setText(R.string.rider_not_assigned);
            return;
        }

        setActivityLoading(true);

        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(riderId).child(EAHCONST.RIDER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    tvRider.setText((CharSequence) dataSnapshot.getValue());
                } else {
                    Utility.showAlertToUser(OrderDetailsActivity.this, R.string.alert_error_downloading_info);
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

    private void getDishesInfo() {

        setActivityLoading(true);

        Query queryRef = dbRef.child(EAHCONST.DISHES_SUB_TREE).child(currentUser.getUid()).orderByChild(EAHCONST.DISH_NAME);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()) {
                    Utility.showAlertToUser(OrderDetailsActivity.this, R.string.alert_error_downloading_info);
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

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String price = etPrice.getText().toString();

        if (!name.isEmpty()) {
            outState.putString(NAME_KEY, name);
        }

        if (!description.isEmpty()) {
            outState.putString(DESCRIPTION_KEY, description);
        }

        if (!price.isEmpty()) {
            outState.putString(PRICE_KEY, price);
        }

        //save edit mode status
        outState.putBoolean(EDIT_MODE_KEY, editMode);
        //save CURRENT dish
        outState.putSerializable(DISH_KEY, currentDish);
        // if to save image or not
        outState.putBoolean(SAVE_IMAGE_KEY,photoPresent);
        // if image change or not
        outState.putBoolean(SAVE_CHANGE_IMAGE_KEY,photoChanged);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String name = savedInstanceState.getString(NAME_KEY, "");
        String description = savedInstanceState.getString(DESCRIPTION_KEY, "");
        String price = savedInstanceState.getString(PRICE_KEY, "");

        if (!name.isEmpty()) {
            etName.setText(name);
        }

        if (!description.isEmpty()) {
            etDescription.setText(description);
        }

        if (!price.isEmpty()) {
            etPrice.setText(price);
        }

    }*/

}
