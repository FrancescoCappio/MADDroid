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

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;


public class CurrentOrderFragment extends Fragment {


    public static final String TAG = "CurrentOrderFragment";

    private int waitingCount = 0;


    public static final String ORDER_KEY = "ORDER_KEY";
    public static final String ORDER_ID_KEY = "ORDER_ID_KEY";
    public static final String ORDER_TIME = "ORDER_TIME";
    public static final String ORDER_COST_DELIVERY = "ORDER_COST_DELIVERY";
    public static final String ORDER_TOTAL_COST = "ORDER_TOTAL_COST";
    public static final String ORDER_RESTAURANT_ADDRESS = "ORDER_RESTAURANT_ADDRESS";
    public static final String ORDER_DELIVERY_ADDRESS = "ORDER_DELIVERY_ADDRESS";
    public static final String ORDER_RESTAURANT_UID = "ORDER_DELIVERY_ADDRESS";
    public static final String ORDER_CUSTOMER_UID = "ORDER_DELIVERY_ADDRESS";


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;



    private String riderUID;
    private String customerUID;
    private String restaurantUID;

    private Order order ;
    private String orderID;
    private String orderRestaurantAddress;


    private TextView tvNoOrder;
    private TextView tvDeliveryTime;
    private TextView tvDeliveryTimeTitle;
    private View tvDeliveryTimeSeprator;
    private TextView tvCostDelivery;
    private TextView tvCostDeliveryTitle;
    private View tvCostDeliverySeprator;
    private TextView tvTotalCost;
    private TextView tvTotalCostTitle;
    private View tvTotalCostSeprator;
    private TextView tvRestaurantAdress;
    private TextView tvRestaurantAdressTitle;
    private View tvRestaurantAdressSeprator;
    private TextView tvDeliveryAdress;
    private TextView tvDeliveryAdressTitle;
    private View tvDeliveryAdressSeprator;
    private ProgressBar pbLoading;


    private Boolean toConfirm = false;

    public CurrentOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_order, container, false);

        getReferencesToViews(view);



        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        riderUID = currentUser.getUid();

        setInvisible();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        checkPendingOrder();
    }

    private void checkPendingOrder() {

        setActivityLoading(true);

        Query queryRef = dbRef.child(EAHCONST.ORDERS_RIDER_SUBTREE)
                .child(riderUID).orderByChild(EAHCONST.RIDER_ORDER_STATUS).equalTo("PENDING");

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                if (!dataSnapshot.hasChildren()){
                    toConfirm = false;
                    setInvisible();
                    checkConfirmedOrder();
                    return;
                }
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

    private void checkConfirmedOrder() {

        Query queryRef = dbRef.child(EAHCONST.ORDERS_RIDER_SUBTREE)
                .child(riderUID).orderByChild(EAHCONST.RIDER_ORDER_STATUS).equalTo("CONFIRMED");

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()){
                    toConfirm = false;
                    setInvisible();
                    setActivityLoading(false);
                    return;
                }
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

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

                String orderTime = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_TIME).getValue();
                String orderTotalCost = (String) dataSnapshot.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue();
                String orderDate = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DATE).getValue();
                String orderDeliveryAddress = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_ADDRESS).getValue();
                order = new Order(orderID,orderTotalCost,riderUID,customerUID,restaurantUID,orderTime,orderDate,orderDeliveryAddress, EAHCONST.OrderStatus.PENDING);
                if (orderTime != null && orderTotalCost != null && orderDeliveryAddress != null){
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

                if (orderRestaurantAddress != null) {

                    if (toConfirm) {
                        String orderDeliveryAddress = order.getDeliveryAddress();
                        Intent i = new Intent(getContext(), confirmOrderActivity.class);

                        i.putExtra(ORDER_KEY,order);
                        i.putExtra(ORDER_COST_DELIVERY, String.valueOf(EAHCONST.DELIVERY_COST)+ " €");
                        i.putExtra(ORDER_RESTAURANT_ADDRESS, orderRestaurantAddress);
                        startActivity(i);
                        setActivityLoading(false);
                    }
                    else {
                        setVisible();
                        setDataToView();
                        setActivityLoading(false);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void getReferencesToViews(View view){

        tvNoOrder = view.findViewById(R.id.et_no_order);

        tvDeliveryTime = view.findViewById(R.id.et_time);
        tvDeliveryTimeTitle = view.findViewById(R.id.et_time_title);
        tvDeliveryTimeSeprator = view.findViewById(R.id.view_time_separator);

        tvCostDelivery = view.findViewById(R.id.et_cost_delivery);
        tvCostDeliveryTitle = view.findViewById(R.id.et_cost_delivery_title);
        tvCostDeliverySeprator = view.findViewById(R.id.view_cost_delivery);

        tvTotalCost = view.findViewById(R.id.et_total_cost);
        tvTotalCostTitle =  view.findViewById(R.id.et_total_cost_title);
        tvTotalCostSeprator = view.findViewById(R.id.view_total_cost_separator);

        tvRestaurantAdress = view.findViewById(R.id.et_restaurant_address);
        tvRestaurantAdressTitle = view.findViewById(R.id.tv_restaurante_address_title);
        tvRestaurantAdressSeprator =  view.findViewById(R.id.view_restaurant_address_separator);

        tvDeliveryAdress = view.findViewById(R.id.et_delivery_address);
        tvDeliveryAdressTitle =  view.findViewById(R.id.tv_delivery_address_title);
        tvDeliveryAdressSeprator =  view.findViewById(R.id.view_delivery_address_separator);

        pbLoading = view.findViewById(R.id.pb_loading);

    }

    private void setDataToView() {
        tvDeliveryTime.setText(order.getDeliveryTime());
        tvTotalCost.setText(order.getTotalCost());
        tvCostDelivery.setText(String.valueOf(EAHCONST.DELIVERY_COST) + " €");
        tvRestaurantAdress.setText(orderRestaurantAddress);
        tvDeliveryAdress.setText(order.getDeliveryAddress());
    }

    private void setInvisible() {
        tvDeliveryTime.setVisibility(View.INVISIBLE);
        tvDeliveryTimeTitle.setVisibility(View.INVISIBLE);
        tvDeliveryTimeSeprator.setVisibility(View.INVISIBLE);
        tvCostDelivery.setVisibility(View.INVISIBLE);
        tvCostDeliveryTitle.setVisibility(View.INVISIBLE);
        tvCostDeliverySeprator.setVisibility(View.INVISIBLE);
        tvTotalCost.setVisibility(View.INVISIBLE);
        tvTotalCostTitle.setVisibility(View.INVISIBLE);
        tvTotalCostSeprator.setVisibility(View.INVISIBLE);
        tvRestaurantAdress.setVisibility(View.INVISIBLE);
        tvRestaurantAdressTitle.setVisibility(View.INVISIBLE);
        tvRestaurantAdressSeprator.setVisibility(View.INVISIBLE);
        tvDeliveryAdress.setVisibility(View.INVISIBLE);
        tvDeliveryAdressTitle.setVisibility(View.INVISIBLE);
        tvDeliveryAdressSeprator.setVisibility(View.INVISIBLE);

        tvNoOrder.setVisibility(View.VISIBLE);

    }

    private void setVisible() {
        tvDeliveryTime.setVisibility(View.VISIBLE);
        tvDeliveryTimeTitle.setVisibility(View.VISIBLE);
        tvDeliveryTimeSeprator.setVisibility(View.VISIBLE);
        tvCostDelivery.setVisibility(View.VISIBLE);
        tvCostDeliveryTitle.setVisibility(View.VISIBLE);
        tvCostDeliverySeprator.setVisibility(View.VISIBLE);
        tvTotalCost.setVisibility(View.VISIBLE);
        tvTotalCostTitle.setVisibility(View.VISIBLE);
        tvTotalCostSeprator.setVisibility(View.VISIBLE);
        tvRestaurantAdress.setVisibility(View.VISIBLE);
        tvRestaurantAdressTitle.setVisibility(View.VISIBLE);
        tvRestaurantAdressSeprator.setVisibility(View.VISIBLE);
        tvDeliveryAdress.setVisibility(View.VISIBLE);
        tvDeliveryAdressTitle.setVisibility(View.VISIBLE);
        tvDeliveryAdressSeprator.setVisibility(View.VISIBLE);

        tvNoOrder.setVisibility(View.INVISIBLE);
    }


    private synchronized void setActivityLoading(boolean loading) {
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
