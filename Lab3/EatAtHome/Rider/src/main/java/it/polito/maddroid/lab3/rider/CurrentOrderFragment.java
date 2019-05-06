package it.polito.maddroid.lab3.rider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import it.polito.maddroid.lab3.common.EAHCONST;


public class CurrentOrderFragment extends Fragment {


    public static final String TAG = "CurrentOrderFragment";

    public static final String ORDER_ID_KEY = "ORDER_ID_KEY";
    public static final String ORDER_TIME = "ORDER_TIME";
    public static final String ORDER_COST_DELIVERY = "ORDER_COST_DELIVERY";
    public static final String ORDER_TOTAL_COST = "ORDER_TOTAL_COST";
    public static final String ORDER_RESTAURANT_ADDRESS = "ORDER_RESTAURANT_ADDRESS";
    public static final String ORDER_DELIVERY_ADDRESS = "ORDER_DELIVERY_ADDRESS";


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;



    private String riderUID;
    private String customerUID;
    private String restaurantUID;

    private String orderID;
    private String orderTime;
    private String orderTotalCost;
    private float orderCost;
    private String orderRestaurantAddress;
    private String orderDeliveryAddress;

    private TextView tvDeliveryTime;
    private TextView tvCostDelivery;
    private TextView tvTotalCost;
    private TextView tvRestaurantAdress;
    private TextView tvDeliveryAdress;

    private Boolean toConfirm = false;





    public CurrentOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        riderUID = currentUser.getUid();

        checkPendingOrder();

        if (toConfirm){
            Intent i = new Intent(getContext(),confirmOrderActivity.class);
            i.putExtra(ORDER_ID_KEY,orderID);
            i.putExtra(ORDER_TIME,orderTime);
            i.putExtra(ORDER_COST_DELIVERY, orderCost);
            i.putExtra(ORDER_TOTAL_COST,orderTotalCost);
            i.putExtra(ORDER_DELIVERY_ADDRESS,orderDeliveryAddress);
            i.putExtra(ORDER_RESTAURANT_ADDRESS,orderRestaurantAddress);
            startActivity(i);
        }
    }

    private void checkPendingOrder() {
        Query queryRef = dbRef.child(EAHCONST.ORDERS_RIDER_SUBTREE)
                .child(riderUID).orderByChild(EAHCONST.RIDER_ORDER_STATUS).equalTo("PENDING");

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    toConfirm = true;

                    orderID = ds.getKey();

                    customerUID = (String) ds.child(EAHCONST.RIDER_ORDER_CUSTOMER_ID).getValue();
                    restaurantUID = (String) ds.child(EAHCONST.RIDER_ORDER_RESTAURATEUR_ID).getValue();

                    if (customerUID != null && restaurantUID != null){
                        getRestaurantOrderDetail();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }

    private void getRestaurantOrderDetail() {

        Query queryRef = dbRef.child(EAHCONST.ORDERS_REST_SUBTREE).child(restaurantUID).child(orderID);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderTime = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_TIME).getValue();
                orderTotalCost = (String) dataSnapshot.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue();
                orderCost = EAHCONST.DELIVERY_COST;

                if (orderTime != null && orderTotalCost != null){
                    getRestaurantAdress();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRestaurantAdress() {
        Query queryRef = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(restaurantUID);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderRestaurantAddress = (String) dataSnapshot.child(EAHCONST.RESTAURANT_ADDRESS).getValue();

                if (orderRestaurantAddress != null){
                    getDeliveryAdress();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getDeliveryAdress() {
        Query queryRef = dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(customerUID);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderDeliveryAddress = (String) dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS).getValue();

                if (orderDeliveryAddress != null){
                    setDataToView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_order, container, false);

        tvDeliveryTime = view.findViewById(R.id.et_time);
        tvCostDelivery = view.findViewById(R.id.et_cost_delivery);
        tvTotalCost = view.findViewById(R.id.et_total_cost);
        tvRestaurantAdress = view.findViewById(R.id.et_restaurant_address);
        tvDeliveryAdress = view.findViewById(R.id.et_delivery_address);


        return view;
    }

    private void setDataToView() {
        tvDeliveryTime.setText(orderTime);
        tvTotalCost.setText(orderTotalCost);
        tvCostDelivery.setText("4.20$");
        tvRestaurantAdress.setText(orderRestaurantAddress);
        tvDeliveryAdress.setText(orderDeliveryAddress);
    }


}
