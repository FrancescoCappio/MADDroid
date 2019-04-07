package it.polito.maddroid.lab2;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class OrdersArrayAdapter extends BaseAdapter {
    private ArrayList<Order> orders;
    
    int rowResourceId;
    
    Context context;
    
    
    public OrdersArrayAdapter(Context context, List<Order> objects) {
        this.context = context;
        orders = new ArrayList<>(objects);
    }
    
    
    @Override
    public int getCount() {
        return orders.size();
    }
    
    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.order_list_item, parent, false);
    
        TextView tvTime = rowView.findViewById(R.id.tv_schedule);
        TextView tvId = rowView.findViewById(R.id.tv_order_number);
        
        Order o = orders.get(position);
    
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(o.getTimeHour());
        String sminutes = formatter.format(o.getTimeMinutes());
        
        tvTime.setText(shour +  ":" + sminutes);
        tvId.setText("" + o.getId());
        return rowView;
    }
    
    public void addOrder(Order o) {
        orders.add(o);
    }
    
}
