package it.polito.maddroid.lab3.user;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.OrderDiffUtilCallBack;
import it.polito.maddroid.lab3.common.Utility;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrdersFragment extends Fragment {
    
    private static final String TAG = "OrdersFragment";
    
    private RecyclerView rvOrders;
    private ProgressBar pbLoading;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    
    private CustomerOrdersListAdapter adapter;
    
    private List<CustomerOrder> customerOrders;
    private List<Order> orders;
    private int waitingCount = 0;
    
    public OrdersFragment() {
        // Required empty public constructor
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        
        rvOrders = view.findViewById(R.id.rv_orders);
        pbLoading = view.findViewById(R.id.pb_loading);
    
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    
        adapter = new CustomerOrdersListAdapter(new OrderDiffUtilCallBack(), order -> {
            Intent i = new Intent(getContext(), OrderDetailActivity.class);
            i.putExtra(OrderDetailActivity.ORDER_KEY, order);
            startActivity(i);
        });
        
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
        
        downloadOrdersInfo();
        
        return view;
    }
    
    
    private void downloadOrdersInfo() {
        setFragmentLoading(true);
    
        Query queryRef = dbRef
                .child(EAHCONST.ORDERS_CUST_SUBTREE)
                .child(currentUser.getUid())
                .orderByKey();
        
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                
                if (!dataSnapshot.hasChildren()) {
                    Log.d(TAG, "There are no orders for this user");
                    Utility.showAlertToUser(getActivity(), R.string.alert_no_orders_yet);
                    setFragmentLoading(false);
                    return;
                }
                
                customerOrders = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String restaurantId = (String) ds.child(EAHCONST.CUST_ORDER_RESTAURATEUR_ID).getValue();
                    String riderId = (String) ds.child(EAHCONST.CUST_ORDER_RIDER_ID).getValue();
                    String orderId = ds.getKey();
                    EAHCONST.OrderStatus orderStatus = ds.child(EAHCONST.CUST_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);
                    
                    CustomerOrder co = new CustomerOrder(orderId, restaurantId, riderId, orderStatus);
                    customerOrders.add(co);
                }
                
                getOrdersDetails();
        
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error");
            }
        });
    
    }
    
    private void getOrdersDetails() {
        orders = new ArrayList<>();
        
        for (CustomerOrder co : customerOrders) {
         
            dbRef.child(EAHCONST.ORDERS_REST_SUBTREE)
                    .child(co.getRestaurantId())
                    .child(co.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(EAHCONST.REST_ORDER_STATUS).getValue() == null) {
                        Log.e(TAG, "This order does not exists");
                        return;
                    }
                    
                    String totalCost = (String) dataSnapshot.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue();
                    String deliveryTime = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_TIME).getValue();
                    String date = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DATE).getValue();
                    String deliveryAddress = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_ADDRESS).getValue();
                    
                    Map<String, Integer> dishes = new HashMap<>();
                    for (DataSnapshot dishesSnap : dataSnapshot.child(EAHCONST.REST_ORDER_DISHES_SUBTREE).getChildren()) {
                        String id = dishesSnap.getKey();
                        int quantity = ((Long) dishesSnap.getValue()).intValue();
                        dishes.put(id, quantity);
                    }
                    
                    Order order = new Order(co.getOrderId(), totalCost, co.getRiderId(), currentUser.getUid(), co.getRestaurantId(), deliveryTime, date, deliveryAddress, co.getOrderStatus());
                    order.setDishesMap(dishes);
                    
                    orders.add(order);
                    downloadRestaurantName(order.getRestaurantId());
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error");
                }
            });
            
        }
    }
    
    private synchronized void checkAllOrdersDownloaded() {
        
        if (orders.size() != customerOrders.size()) {
            return;
        }
        
        for (Order o : orders) {
            if (o.getRestaurantName() == null)
                return;
        }
    
        Collections.sort(orders);
        adapter.submitList(orders);
        setFragmentLoading(false);
    }
    
    private void downloadRestaurantName(String restaurantId) {
        dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(restaurantId).child(EAHCONST.RESTAURANT_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Log.e(TAG, "Cannot find name for restaurant: " + restaurantId);
                    return;
                }
                
                for (Order o : orders) {
                    if (o.getRestaurantId() == restaurantId) {
                        o.setRestaurantName((String) dataSnapshot.getValue());
                        break;
                    }
                }
                checkAllOrdersDownloaded();
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error");
            }
        });
    }
    
    private synchronized void setFragmentLoading(boolean loading) {
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
}
