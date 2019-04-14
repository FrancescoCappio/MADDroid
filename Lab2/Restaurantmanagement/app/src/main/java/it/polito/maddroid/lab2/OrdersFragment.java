package it.polito.maddroid.lab2;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class OrdersFragment extends Fragment {
    public static final String TAG = "OrdersFragment";
    
    ExpandableListView expandableListView;
    OrdersExpandableListAdapter expandableListAdapter;
    ArrayList<Order> expandableListTitle;
    HashMap<Order, List<DailyOffer>> expandableListDetail;
    
    private List<Order> orders;
    private ExpandableListView lvOrders;
    
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
        
        expandableListView = view.findViewById(R.id.expandableListView);
        expandableListDetail = getMapFromList(DataManager.getInstance(getContext()).getOrders());
        expandableListTitle = new ArrayList<>(DataManager.getInstance(getContext()).getOrders());
        expandableListAdapter = new OrdersExpandableListAdapter(getContext(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupExpandListener(groupPosition -> {
        });

        expandableListView.setOnGroupCollapseListener(groupPosition -> {
        });
        expandableListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            int groupPos = ExpandableListView.getPackedPositionGroup(id);
            int itemType = ExpandableListView.getPackedPositionType(id);

            if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {


            } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                Intent i = new Intent(getContext(), OrderDetailActivity.class);
                i.putExtra(OrderDetailActivity.PAGE_TYPE_KEY, OrderDetailActivity.MODE_SHOW);
                i.putExtra(OrderDetailActivity.ORDER_ID_KEY, groupPos);
                startActivityForResult(i, MainActivity.ORDER_DETAIL_CODE);

            } else {
                // null item; we don't consume the click
                return false;
            }
        return true;
        });

        return view;
    }

    public HashMap<Order, List<DailyOffer>> getMapFromList(List<Order> orderList){
        HashMap<Order, List<DailyOffer>> orderMap = new HashMap<>();
        DataManager dataManager = DataManager.getInstance(getContext());
        for (Order o : orderList){
            ArrayList<DailyOffer> dishesList = new ArrayList<>();
            Map<Integer, Integer> dishes = o.getDishes();

            for ( Map.Entry<Integer, Integer> entry : dishes.entrySet())
            {
                DailyOffer dailyOffer = dataManager.getDailyOfferWithId(entry.getKey());
                dishesList.add(dailyOffer);
            }
            orderMap.put(o, dishesList);
            
        }

        return  orderMap;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }
        
        if (requestCode == MainActivity.ORDER_DETAIL_CODE) {
            notifyUpdate();
        }
    }
    
    public void notifyUpdate() {
        expandableListDetail = this.getMapFromList(DataManager.getInstance(getContext()).getOrders());
        expandableListTitle = new ArrayList<>(DataManager.getInstance(getContext()).getOrders());
        expandableListAdapter.notifyUpadateOrder(expandableListTitle,expandableListDetail);
        return;
    }
    
}
