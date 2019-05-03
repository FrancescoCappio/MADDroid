package it.polito.maddroid.lab3.user;


import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


public class CustomerOrdersListAdapter extends ListAdapter<Order, CustomerOrdersListAdapter.MyViewHolder> {
    
    
    protected CustomerOrdersListAdapter(@NonNull DiffUtil.ItemCallback<Order> diffCallback) {
        super(diffCallback);
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    
    }
    
    public interface ItemClickListener {
        void onItemClickListener(Order order);
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private View itemView;
        private TextView tvRestaurantName;
        private TextView tvRestaurantDescription;
        private ImageView ivRestaurantPhoto;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            this.itemView = itemView;
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            tvRestaurantDescription = itemView.findViewById(R.id.tv_restaurant_description);
            ivRestaurantPhoto = itemView.findViewById(R.id.iv_restaurant_photo);
        }
        
        public void setupOrder(Order order, CustomerOrdersListAdapter.ItemClickListener itemClickListener) {
        
        }
        
    }
}
