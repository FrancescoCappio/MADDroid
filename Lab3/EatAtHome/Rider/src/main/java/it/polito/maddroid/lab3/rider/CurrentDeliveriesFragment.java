package it.polito.maddroid.lab3.rider;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;


public class CurrentDeliveriesFragment extends Fragment implements MainActivity.OrdersUpdateListener {
    
    private static final String TAG = "CurrentDeliveriesFragment";

    private int waitingCount = 0;
    
    private TextView tvNoOrdersPlaceHolder;
    private ProgressBar pbLoading;
    private RecyclerView rvDeliveries;
    
    private RiderOrdersListAdapter adapter;
    
    private List<RiderOrderDelivery> acceptedDeliveries;

    public CurrentDeliveriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_deliveries, container, false);

        getReferencesToViews(view);
        
        rvDeliveries.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new RiderOrdersListAdapter(new RiderOrderDeliveryDiffUtilCallback(), order -> {
            Intent intent = new Intent(getContext(), OrderDeliveryActivity.class);
            intent.putExtra(OrderDeliveryActivity.ORDER_DELIVERY_KEY, order);
            startActivity(intent);
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
        if (acceptedDeliveries == null || acceptedDeliveries.isEmpty()) {
            tvNoOrdersPlaceHolder.setVisibility(View.VISIBLE);
            rvDeliveries.setVisibility(View.GONE);
        } else {
            tvNoOrdersPlaceHolder.setVisibility(View.GONE);
            rvDeliveries.setVisibility(View.VISIBLE);
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
            List<RiderOrderDelivery> allDeliveries = ((MainActivity) getActivity()).getAllDeliveries();
            
            acceptedDeliveries = new ArrayList<>();
            
            for (RiderOrderDelivery rod : allDeliveries) {
                if (rod.getOrderStatus() == EAHCONST.OrderStatus.ONGOING || rod.getOrderStatus() == EAHCONST.OrderStatus.CONFIRMED)
                    acceptedDeliveries.add(rod);
            }
            
            adapter.submitList(acceptedDeliveries);
            
            manageVisibility();
        }
    }
}
