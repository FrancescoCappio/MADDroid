package it.polito.maddroid.lab3.user;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Order;


public class CustomerOrdersListAdapter extends ListAdapter<Order, CustomerOrdersListAdapter.MyViewHolder> {
    private CustomerOrdersListAdapter.ItemClickListener clickListener;
    
    protected CustomerOrdersListAdapter(@NonNull DiffUtil.ItemCallback<Order> diffCallback, ItemClickListener itemClickListener) {
        super(diffCallback);
        clickListener = itemClickListener;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_list_item, parent, false);
    
        return new MyViewHolder(v);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupOrder(getItem(position), clickListener);
    }
    
    public interface ItemClickListener {
        void onItemClick(Order order);
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private View itemView;
        private TextView tvRestaurantName;
        private TextView tvOrderDate;
        private TextView tvDeliveryTime;
        private TextView tvTotalCost;
        private TextView tvOrderStatus;
        private CardView cvContainer;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            this.itemView = itemView;
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            cvContainer = itemView.findViewById(R.id.cv_element_container);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
            tvTotalCost = itemView.findViewById(R.id.tv_total_cost);
        }
        
        public void setupOrder(Order order, CustomerOrdersListAdapter.ItemClickListener itemClickListener) {
        
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order));
            
            tvOrderDate.setText(order.getDate());
            tvDeliveryTime.setText(order.getDeliveryTime());
            tvTotalCost.setText(order.getTotalCost());
            tvOrderStatus.setText(order.getOrderStatus().toString());
            tvRestaurantName.setText(order.getRestaurantName());
            
            switch (order.getOrderStatus()) {
                case PENDING:
                    cvContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_orange_alert));
                    break;
                case ONGOING:
                    cvContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_green_alert));
                    break;
                case DECLINED:
                    cvContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_red_alert));
                    break;
                case CONFIRMED:
                    cvContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_green_alert));
                    break;
                case COMPLETED:
                    cvContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_white));
                    break;
            }
            
            
        }
        
    }
}
