package it.polito.maddroid.lab3.user;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class OrdersFragment extends Fragment {
    
    private static final String TAG = "OrdersFragment";
    
    private RecyclerView rvOrders;
    
    public OrdersFragment() {
        // Required empty public constructor
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        
        rvOrders = view.findViewById(R.id.rv_orders);
        
        
        return view;
    }
    
    
    private void downloadOrdersInfo() {
    
    }
    
}
