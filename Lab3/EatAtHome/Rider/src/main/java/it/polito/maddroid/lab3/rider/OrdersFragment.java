package it.polito.maddroid.lab3.rider;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import it.polito.maddroid.lab3.common.EAHCONST;

public class OrdersFragment extends Fragment implements MainActivity.OrdersUpdateListener{

    private static final String TAG = "OrdersFragment";

    private RecyclerView rvOrders;
    private ProgressBar pbLoading;

    private RiderOrdersListAdapter adapter;

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
        
        setFragmentLoading(true);

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

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).registerOrdersUpdateListener(this);
        }

        return view;
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
            if (waitingCount > 0)
                waitingCount--;
            if (waitingCount == 0)
                pbLoading.setVisibility(View.INVISIBLE);
        }
    }
    
    
    @Override
    public void manageOrdersUpdate() {
    
        setFragmentLoading(false);
        
        if (getActivity() instanceof MainActivity) {
            if (adapter != null)
                adapter.submitList(((MainActivity) getActivity()).getAllDeliveries());
        }
    }
}
