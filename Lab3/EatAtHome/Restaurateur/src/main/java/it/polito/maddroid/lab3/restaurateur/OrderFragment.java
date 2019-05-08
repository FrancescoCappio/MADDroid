package it.polito.maddroid.lab3.restaurateur;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.OrderDiffUtilCallBack;

import static android.app.Activity.RESULT_OK;

public class OrderFragment extends Fragment {
    private RecyclerView rvOrder;

    private static final String TAG = "OrderFragment";

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private OrderListAdapter adapter;
    private int waitingCount = 0;

   // private Order lastOrderClicked;

    public List<Order> orders;

    private ProgressBar pbLoading;
    private int countOrders = 0;


    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        pbLoading = view.findViewById(R.id.pb_loading_order);

        dbRef = FirebaseDatabase.getInstance().getReference();

        rvOrder = view.findViewById(R.id.rv_orders);

        rvOrder.setLayoutManager(new LinearLayoutManager(getContext()));

        downloadOrdersInfo();

        adapter = new OrderListAdapter(new OrderDiffUtilCallBack(), order -> {
            Intent i = new Intent(getContext(), OrderDetailsActivity.class);
            i.putExtra(OrderDetailsActivity.ORDER_KEY, order);
            //lastOrderClicked = order;
            startActivity(i);
        });

        rvOrder.setAdapter(adapter);
        return view;
    }

    private void downloadOrdersInfo() {
        setActivityLoading(true);
        orders = new ArrayList<>();
        Query queryRef = dbRef
                .child(EAHCONST.ORDERS_REST_SUBTREE).child(currentUser.getUid());

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //todo costo totale in float o in string?
                Log.d(TAG, "onDataChange Called");
                orders = new ArrayList<>();
                countOrders = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String orderId = ds.getKey();
                    String riderId = (String) ds.child(EAHCONST.REST_ORDER_RIDER_ID).getValue();
                    String customerId = (String) ds.child(EAHCONST.REST_ORDER_CUSTOMER_ID).getValue();
                    //float totalPrice = Float.parseFloat( ds.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue().toString());
                    String totalPrice = (String) ds.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue();
                    String deliveryAddress = (String) ds.child(EAHCONST.REST_ORDER_DELIVERY_ADDRESS).getValue();
                    String timeDelivery = (String) ds.child(EAHCONST.REST_ORDER_DELIVERY_TIME).getValue();
                    String dateDelivery = (String) ds.child(EAHCONST.REST_ORDER_DATE).getValue();
                    EAHCONST.OrderStatus orderStatus = ds.child(EAHCONST.REST_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);

                    Map<String, Integer> dishes = new HashMap<>();
                    for (DataSnapshot dishesSnap : ds.child(EAHCONST.REST_ORDER_DISHES_SUBTREE).getChildren()) {
                        String id = dishesSnap.getKey();
                        int quantity = ((Long) dishesSnap.getValue()).intValue();
                        dishes.put(id, quantity);
                    }

                    Order order = new Order(orderId,totalPrice,riderId,customerId,currentUser.getUid(),timeDelivery,dateDelivery,deliveryAddress,orderStatus);
                    order.setDishesMap(dishes);

                    orders.add(order);
                    downloadCustomerName(order);
                }
                setActivityLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });


    }

    private void downloadCustomerName(Order order) {
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(order.getCustomerId()).child(EAHCONST.CUSTOMER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Log.e(TAG, "Cannot find name for customer: ");
                    return;
                }
                order.setCustomerName((String) dataSnapshot.getValue());
                checkAllOrdersDownloaded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error");
            }
        });


    }

    private void checkAllOrdersDownloaded() {
        if (orders.size() != countOrders)
            return;

        for(Order o : orders)
            if(o.getCustomerName() == null || o.getCustomerName().isEmpty())
                return;

        adapter.submitList(orders);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }

        if (data == null) {
            Log.e(TAG, "Result data null");
            return;
        }

        switch (requestCode) {
            case MainActivity.ORDER_DETAIL_CODE:
                downloadOrdersInfo();

        }

    }

    private void setActivityLoading(boolean b) {
        if (b) {
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