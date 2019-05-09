package it.polito.maddroid.lab3.rider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.OrderDiffUtilCallBack;
import it.polito.maddroid.lab3.common.Utility;


public class OrdersFragment extends Fragment {

    private static final String TAG = "OrdersFragment";

    private RecyclerView rvOrders;
    private ProgressBar pbLoading;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String riderUID;

    private RiderOrdersListAdapter adapter;

    private List<RiderOrder> riderOrders;
    private List<Order> orders;
    private int waitingCount = 0;

    public OrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  =  inflater.inflate(R.layout.fragment_orders,container,false);

        rvOrders = view.findViewById(R.id.rv_orders);
        pbLoading = view.findViewById(R.id.pb_loading);

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        riderUID = currentUser.getUid();

        adapter = new RiderOrdersListAdapter(new OrderDiffUtilCallBack(), order -> {

            if (order.getOrderStatus() != EAHCONST.OrderStatus.COMPLETED ||order.getOrderStatus() != EAHCONST.OrderStatus.DECLINED ) {


                //TODO : modify the code
                Bundle bundle = new Bundle();
                bundle.putString(CurrentOrderFragment.ORDER_RESTAURANT_UID, order.getRestaurantId()); // Put anything what you want
                bundle.putString(CurrentOrderFragment.ORDER_CUSTOMER_UID, order.getCustomerId());
                bundle.putString(CurrentOrderFragment.ORDER_ID_KEY, order.getOrderId());
                bundle.putString(CurrentOrderFragment.ORDER_STATUS, order.getOrderStatus().toString());
//
//                CurrentOrderFragment currentOrderFragment = new CurrentOrderFragment();
//                currentOrderFragment.setArguments(bundle);

//                getFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_container, currentOrderFragment)
//                        .commit();

                MainActivity main = new MainActivity();
                main.selectItem(0, bundle);
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        downloadOrdersInfo();


        return view;
    }

    private void downloadOrdersInfo() {
        setFragmentLoading(true);

        Query queryRef = dbRef
                .child(EAHCONST.ORDERS_RIDER_SUBTREE)
                .child(riderUID)
                .orderByKey();

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()) {
                    Log.d(TAG, "There are no orders for this user");
                    Utility.showAlertToUser(getActivity(), R.string.alert_no_orders_yet);
                    return;
                }

                riderOrders = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String restaurantId = (String) ds.child(EAHCONST.RIDER_ORDER_RESTAURATEUR_ID).getValue();
                    String customerID = (String) ds.child(EAHCONST.RIDER_ORDER_CUSTOMER_ID).getValue();
                    String orderId = ds.getKey();
                    EAHCONST.OrderStatus orderStatus = ds.child(EAHCONST.RIDER_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);

                    RiderOrder co = new RiderOrder(orderId, restaurantId, customerID, orderStatus);
                    riderOrders.add(co);
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

        for (RiderOrder co : riderOrders) {

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
                    //EAHCONST.OrderStatus orderStatus = dataSnapshot.child(EAHCONST.REST_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);
                    EAHCONST.OrderStatus orderStatus = co.getOrderStatus();


                    Order order = new Order(co.getOrderId(), totalCost, riderUID,co.getCustomerId(), co.getRestaurantId(), deliveryTime, date, deliveryAddress, orderStatus);


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

    private synchronized void checkAllOrdersDownloaded() {

        if (orders.size() != riderOrders.size()) {
            return;
        }

        for (Order o : orders) {
            if (o.getRestaurantName() == null)
                return;
        }

        adapter.submitList(orders);
        setFragmentLoading(false);
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
