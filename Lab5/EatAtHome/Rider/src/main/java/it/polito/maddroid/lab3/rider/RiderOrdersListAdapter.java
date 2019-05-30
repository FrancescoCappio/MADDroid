package it.polito.maddroid.lab3.rider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.polito.maddroid.lab3.common.EAHCONST;

public class RiderOrdersListAdapter extends ListAdapter<RiderOrderDelivery, RiderOrdersListAdapter.MyViewHolder>{
    private RiderOrdersListAdapter.ItemClickListener clickListener;

    protected RiderOrdersListAdapter(@NonNull DiffUtil.ItemCallback<RiderOrderDelivery> diffCallback, ItemClickListener itemClickListener) {
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
        void onItemClick(RiderOrderDelivery order);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tvRestaurantName;
        private TextView tvOrderDate;
        private TextView tvDeliveryTime;
        private TextView tvDeliveryCost;
        private TextView tvOrderStatus;
        private TextView tvOrderAddress;
        private CardView cvContainer;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            cvContainer = itemView.findViewById(R.id.cv_element_container);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
            tvDeliveryCost = itemView.findViewById(R.id.tv_delivery_cost);
            tvOrderAddress = itemView.findViewById(R.id.tv_delivery_address);
        }

        public void setupOrder(RiderOrderDelivery order, RiderOrdersListAdapter.ItemClickListener itemClickListener) {

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order));

            tvOrderDate.setText(order.getDeliveryDate());
            tvDeliveryTime.setText(order.getDeliveryTime());
            if(order.getOrderStatus() != EAHCONST.OrderStatus.PENDING)
             tvDeliveryCost.setText(String.format(Locale.US,"%.02f", order.getDeliveryCost()) + "€");
            tvOrderStatus.setText(order.getOrderStatus().toString());
            tvRestaurantName.setText(order.getRestaurantName());
            tvOrderAddress.setText(order.getDeliveryAddress());

            switch (order.getOrderStatus()) {
                case PENDING:
                    tvDeliveryCost.setText(R.string.see_order_details);
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_orange_alert));
                    break;
                case CONFIRMED:
                case ONGOING:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_green_alert));
                    break;
                case DECLINED:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_red_alert));
                    break;
                case COMPLETED:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_black));
                    break;

            }

        }


    }

}
