package it.polito.maddroid.lab3.rider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;


public class CurrentDeliveriesFragment extends Fragment implements MainActivity.OrdersUpdateListener {
    
    private static final String TAG = "CurrentDeliveriesFragment";

    private int waitingCount = 0;
    
    private TextView tvNoOrdersPlaceHolder;
    private ProgressBar pbLoading;
    private RecyclerView rvDeliveries;
    
    private RiderOrdersListAdapter adapter;
    
    private List<RiderOrderDelivery> currentDeliveries;
    
    private SharedPreferences sharedPreferences;

    public CurrentDeliveriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_deliveries, container, false);

        getReferencesToViews(view);
        
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        }
        
        rvDeliveries.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new RiderOrdersListAdapter(new RiderOrderDeliveryDiffUtilCallback(), order -> {
            if (order.getOrderStatus() == EAHCONST.OrderStatus.ONGOING ||
                    order.getOrderStatus() == EAHCONST.OrderStatus.CONFIRMED ) {
        
                Intent intent = new Intent(getContext(), OrderDeliveryActivity.class);
                intent.putExtra(OrderDeliveryActivity.ORDER_DELIVERY_KEY, order);
                startActivity(intent);
            } else if (order.getOrderStatus() == EAHCONST.OrderStatus.PENDING) {
                Intent intent = new Intent(getContext(), ConfirmOrderActivity.class);
                intent.putExtra(ConfirmOrderActivity.RIDER_ORDER_DELIVERY_KEY, order);
                startActivity(intent);
            }
        });
        
        rvDeliveries.setAdapter(adapter);
        
        setActivityLoading(true);
        
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).registerOrdersUpdateListener(this);
        }

        return view;
    }
    
    
    private void getReferencesToViews(View view){
        pbLoading = view.findViewById(R.id.pb_loading);
        rvDeliveries = view.findViewById(R.id.rv_deliveries);
        tvNoOrdersPlaceHolder = view.findViewById(R.id.tv_no_order);
    }

    private void manageVisibility() {
        
        boolean onDuty = sharedPreferences.getBoolean(MainActivity.RIDER_ON_DUTY_KEY, false);
        
        if (onDuty) {
            if (currentDeliveries == null || currentDeliveries.isEmpty()) {
                tvNoOrdersPlaceHolder.setVisibility(View.VISIBLE);
                tvNoOrdersPlaceHolder.setText(R.string.alert_no_orders_yet);
                rvDeliveries.setVisibility(View.GONE);
            }
            else {
                tvNoOrdersPlaceHolder.setVisibility(View.GONE);
                rvDeliveries.setVisibility(View.VISIBLE);
            }
        } else {
            tvNoOrdersPlaceHolder.setVisibility(View.VISIBLE);
            tvNoOrdersPlaceHolder.setText(R.string.alert_on_duty_switch);
            rvDeliveries.setVisibility(View.GONE);
        }
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
    
    
    @Override
    public void manageOrdersUpdate() {
        
        setActivityLoading(false);
    
        if (getActivity() instanceof MainActivity) {
    
            currentDeliveries = MainActivity.getCurrentDeliveries(((MainActivity) getActivity()).getAllDeliveries());
            
            adapter.submitList(currentDeliveries);
            
            manageVisibility();
        }
    }
}
