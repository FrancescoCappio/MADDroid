package it.polito.maddroid.lab2;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


public class OrdersFragment extends Fragment {
    
    private ArrayList<Order> orders;
    
    private ListView lvOrders;
    
    OrdersArrayAdapter ordersAdapter;
    
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
        
        orders = new ArrayList<>();
        
        ordersAdapter = new OrdersArrayAdapter(getContext(), orders);
        
        lvOrders.setAdapter(ordersAdapter);
        
        return view;
    }
    
    public void addOrder(Order o) {
        orders.add(o);
        ordersAdapter.addOrder(o);
        ordersAdapter.notifyDataSetChanged();
    }
    
}
