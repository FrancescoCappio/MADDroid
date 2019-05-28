package it.polito.maddroid.lab3.restaurateur;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.polito.maddroid.lab3.common.ChooseRiderActivity;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.Rider;
import it.polito.maddroid.lab3.common.Utility;

public class OrderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailsActivity";
    private Order currentOrder;
    private int waitingCount;
    public final static String ORDER_KEY = "ORDER_KEY";
    
    public final static int CHOOSE_RIDER_REQUEST_CODE = 2452;

    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;

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
    
    private MenuItem callRiderItem;


    private List<Dish> dishList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        getReferencesToViews();
        setupClickListeners();
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.order_detail);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent i = getIntent();

        Serializable orderExtra = i.getSerializableExtra(ORDER_KEY);

        if (orderExtra != null) {
            currentOrder = (Order) orderExtra;
        } else {
            Log.e(TAG, "Cannot open detail for a null order");
            finish();
        }

        tvDeliveryData.setText(currentOrder.getDate());
        tvDeliveryTime.setText(currentOrder.getOrderReadyTime());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        
        String [] suddivido = currentOrder.getTotalCost().split(" ");
        float costo = Float.parseFloat(suddivido[0]);
        tvTotPrice.setText(String.format(Locale.US,"%.02f", costo) + " €");
        
        getRiderName(currentOrder.getRiderId());
        tvCustomer.setText(currentOrder.getCustomerName());

        //recycler view
        rvDishes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        adapter = new MenuListAdapter(new DishDiffUtilCallBack(), dish -> Log.d(TAG, "Nothing to do"), currentUser.getUid(), MenuListAdapter.MODE_ORDER_DISH_LIST);

        rvDishes.setAdapter(adapter);

        getDishesInfo();

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater menuInflater = getMenuInflater();
        
        menuInflater.inflate(R.menu.order_detail_menu, menu);
        
        callRiderItem = menu.findItem(R.id.action_call_rider);
        
        updateUIforOrderStatus();
        
        return super.onCreateOptionsMenu(menu);
    }
    

    private void updateUIforOrderStatus() {
        if(currentOrder.getOrderStatus().equals(EAHCONST.OrderStatus.PENDING)){
            declineOrder.show();
            confirmOrder.show();
            
            Utility.showAlertToUser(this, R.string.alert_confirm_decline_order);
        
        } else {
            declineOrder.hide();
            confirmOrder.hide();
        }
    
        tvOrderStatus.setText(currentOrder.getOrderStatus().toString());
        
        if (callRiderItem != null) {
            callRiderItem.setVisible(currentOrder.getOrderStatus().equals(EAHCONST.OrderStatus.CONFIRMED));
        }
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
                
                currentOrder.setOrderStatus(EAHCONST.OrderStatus.DECLINED);
                updateUIforOrderStatus();

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
    
            dbRef.updateChildren(updateMap).addOnSuccessListener(s -> {
                Log.d(TAG, "Success confirmed order");
                Utility.showAlertToUser(this,R.string.notify_save_ok);
                setActivityLoading(false);
        
                currentOrder.setOrderStatus(EAHCONST.OrderStatus.CONFIRMED);
                updateUIforOrderStatus();
        
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Database error while confirmed order: " + e.getMessage());
                Utility.showAlertToUser(this, R.string.notify_save_ko);
                setActivityLoading(false);
            });
        });
    }
    
    private void assignOrderToRider(String riderId) {
        // now from point of view of rider
    
        Map<String,Object> updateMap = new HashMap<>();
    
        updateMap.put(EAHCONST.generatePath(EAHCONST.ORDERS_REST_SUBTREE, currentUser.getUid(), currentOrder.getOrderId(), EAHCONST.REST_ORDER_STATUS), EAHCONST.OrderStatus.WAITING_RIDER);
        String riderOrderPath = EAHCONST.generatePath(EAHCONST.ORDERS_RIDER_SUBTREE, riderId, currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), EAHCONST.OrderStatus.PENDING);
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_RESTAURATEUR_ID), currentUser.getUid());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_CUSTOMER_ID), currentOrder.getCustomerId());
    
        dbRef.updateChildren(updateMap).addOnSuccessListener(s -> {
            Log.d(TAG, "Success confirmed order");
            Utility.showAlertToUser(this,R.string.notify_save_ok);
            setActivityLoading(false);
        
            currentOrder.setOrderStatus(EAHCONST.OrderStatus.WAITING_RIDER);
            getRiderName(riderId);
            updateUIforOrderStatus();
        
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Database error while confirmed order: " + e.getMessage());
            Utility.showAlertToUser(this, R.string.notify_save_ko);
            setActivityLoading(false);
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
        
        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.WAITING_RIDER) {
            tvRider.setText(R.string.waiting_rider_confirm);
            return;
        }

        setActivityLoading(true);

        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(riderId).child(EAHCONST.RIDER_NAME).addValueEventListener(new ValueEventListener() {
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
                
            case R.id.action_call_rider:
                if (currentOrder.getOrderStatus() != EAHCONST.OrderStatus.CONFIRMED) {
                    Log.e(TAG, "Illegal status");
                    return true;
                }
                startChooseRiderActivity();
                
        }
        return false;
    }
    
    private void startChooseRiderActivity() {
        
        Intent intent = new Intent(getApplicationContext(), ChooseRiderActivity.class);
        intent.putExtra(ChooseRiderActivity.RESTAURANT_ID_KEY, currentUser.getUid());
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
                assignOrderToRider(rider.getId());
            }
        }
    }
}
