package it.polito.maddroid.lab3.restaurateur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.polito.maddroid.lab3.common.Order;


public class OrderListAdapter extends ListAdapter<OrderRestaurant, OrderListAdapter.MyViewHolder> {

    private static StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private OrderListAdapter.ItemClickListener itemClickListener;

    protected OrderListAdapter(@NonNull DiffUtil.ItemCallback<OrderRestaurant> diffCallback, OrderListAdapter.ItemClickListener itemClickListener) {
        super(diffCallback);
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_item, parent, false);

        // create view holder and pass main view to it
        OrderListAdapter.MyViewHolder oh = new OrderListAdapter.MyViewHolder(v);
        return oh;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderListAdapter.MyViewHolder holder, int position) {
        holder.setupOrder(getItem(position), itemClickListener, currentUser.getUid());
    }

    public interface ItemClickListener {
        void clickListener(OrderRestaurant order);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView tvOrderId;
        private TextView tvOrderTotPrice;
        private TextView tvOrderRiderId;
        private TextView tvOrderTimeTable;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderRiderId = itemView.findViewById(R.id.tv_rider_id);
            tvOrderTotPrice = itemView.findViewById(R.id.tv_total_cost);
            tvOrderTimeTable =  itemView.findViewById(R.id.tv_schedule);
            this.itemView = itemView;

        }

        public void setupOrder(OrderRestaurant order, OrderListAdapter.ItemClickListener itemClickListener, String userUID) {
            tvOrderId.setText(order.getOrderId());
            tvOrderRiderId.setText(order.getRiderId());
            tvOrderTotPrice.setText(String.valueOf(order.getTotalCost()));
            tvOrderTimeTable.setText(order.getDeliveryTime());
            itemView.setOnClickListener(v -> itemClickListener.clickListener(order));

        }
    }

}
