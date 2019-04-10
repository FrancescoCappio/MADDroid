package it.polito.maddroid.lab2;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;


public class OrdersFragment extends Fragment {
    
    private List<Order> orders;
    
    private ListView lvOrders;
    
    OrdersAdapter ordersAdapter;
    
    public OrdersFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        
        View view =  inflater.inflate(R.layout.fragment_orders, container, false);
        
        lvOrders = view.findViewById(R.id.lv_orders);
        
        orders = DataManager.getInstance(getContext()).getOrders();
        
        ordersAdapter = new OrdersAdapter(getContext(), orders);
        
        lvOrders.setAdapter(ordersAdapter);
        
        return view;
    }
    
    public void notifyUpdate() {
        orders = DataManager.getInstance(getContext()).getOrders();
        ordersAdapter.updateOrders(orders);
    }
    
    
}
