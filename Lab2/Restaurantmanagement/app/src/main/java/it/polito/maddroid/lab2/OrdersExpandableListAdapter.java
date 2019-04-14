package it.polito.maddroid.lab2;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class OrdersExpandableListAdapter extends BaseExpandableListAdapter {

    private final static int MINIUMUM_CHILD_HEIGHT_DP = 75;
    
    private Context context;
    private List<Order> expandableListTitle;
    private HashMap<Order, List<DailyOffer>> expandableListDetail;
    
    private int childMinimumHeight;

    public OrdersExpandableListAdapter(Context context, List<Order> expandableListTitle,
                                       HashMap<Order, List<DailyOffer>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        final float scale = context.getResources().getDisplayMetrics().density;
        childMinimumHeight = (int) (MINIUMUM_CHILD_HEIGHT_DP * scale + 0.5f);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        if (this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition) != null)
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
        else
            return null;
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        if (this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition) != null)
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition).getId();
        else
            return -1;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        
        final DailyOffer expandedListText;
        expandedListText = (DailyOffer) getChild(listPosition, expandedListPosition);
        
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.daily_offer_expanded_list_item, null);
    
            convertView.setMinimumHeight(childMinimumHeight);
        }
        
        TextView dishName = convertView.findViewById(R.id.tv_dish_name);
        TextView description = convertView.findViewById(R.id.tv_dish_description);
        TextView price = convertView.findViewById(R.id.tv_price);
        TextView quantity = convertView.findViewById(R.id.tv_quantity);
        
        
        float sum = expandedListText.getPrice() * (int)expandableListTitle.get(listPosition).getDishes().get(expandedListText.getId());

        if(dishName != null)
            dishName.setText(expandedListText.getName());
        
        if(description != null)
            description.setText(expandedListText.getDescription());
        
        if(price != null)
            price.setText(""+sum);
        
        if(quantity != null)
            quantity.setText(""+expandableListTitle.get(listPosition).getDishes().get(expandedListText.getId()));
        
        dishName.setTypeface(null,Typeface.BOLD);
        price.setTypeface(null, Typeface.BOLD);
        quantity.setTypeface(null, Typeface.BOLD);

        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        if(this.expandableListDetail.get(this.expandableListTitle.get(listPosition))!= null) {
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size();
        }
        else
            return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.expandableListTitle.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return expandableListTitle.get(listPosition).getId();
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        
        Order orderTitle = expandableListTitle.get(listPosition);
        
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.order_list_item, null);
        }
        
        TextView OrderTitleTextView = convertView.findViewById(R.id.tv_order_number);
        TextView TimeTitleTextView = convertView.findViewById(R.id.tv_schedule);
        TextView riderId = convertView.findViewById(R.id.tv_rider_id);
        TextView customerId = convertView.findViewById(R.id.tv_customer_id);
        
        OrderTitleTextView.setTypeface(null, Typeface.BOLD);
        OrderTitleTextView.setText(""+orderTitle.getId());
        
        TimeTitleTextView.setTypeface(null, Typeface.BOLD);
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(orderTitle.getTimeHour());
        String sminutes = formatter.format(orderTitle.getTimeMinutes());
        TimeTitleTextView.setText(""+shour+":"+sminutes);
        
        riderId.setText(""+orderTitle.getRiderId());
        riderId.setTypeface(null,Typeface.BOLD);
        
        customerId.setText(""+orderTitle.getCustomerId());
        customerId.setTypeface(null,Typeface.BOLD);
        
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    public void notifyUpadateOrder(List<Order> expandableListTitle,
                                   HashMap<Order, List<DailyOffer>> expandableListDetail){
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        notifyDataSetChanged();
        return;
    }
}



