package it.polito.maddroid.lab3.restaurateur;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import it.polito.maddroid.lab3.common.Order;


public class OrderListAdapter extends ListAdapter<Order, OrderListAdapter.MyViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private OrderListAdapter.ItemClickListener itemClickListener;

    protected OrderListAdapter(@NonNull DiffUtil.ItemCallback<Order> diffCallback, OrderListAdapter.ItemClickListener itemClickListener) {
        super(diffCallback);
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
        void clickListener(Order order);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView tvOrderCustomer;
        private TextView tvOrderTotPrice;
        private TextView tvOrderDate;
        private TextView tvOrderTime;
        private TextView tvOrderStatus;
        private CardView cardView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_element_container);
            tvOrderCustomer = itemView.findViewById(R.id.tv_customer_order_name);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderTotPrice = itemView.findViewById(R.id.tv_total_cost);
            tvOrderTime =  itemView.findViewById(R.id.tv_delivery_time);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            this.itemView = itemView;

        }

        public void setupOrder(Order order, OrderListAdapter.ItemClickListener itemClickListener, String userUID) {
            tvOrderCustomer.setText(order.getCustomerName());
            tvOrderDate.setText(order.getDate());
            String [] suddivido = order.getTotalCost().split(" ");
            float costo = Float.parseFloat(suddivido[0]);
            tvOrderTotPrice.setText(String.format(Locale.US,"%.02f", costo) + " €");
            
            tvOrderTime.setText(order.getOrderReadyTime());
            
            tvOrderStatus.setText(order.getOrderStatus().toString());
            itemView.setOnClickListener(v -> itemClickListener.clickListener(order));
            switch ( order.getOrderStatus()) {
                case PENDING:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_orange_alert));
                    break;
                case DECLINED:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_red_alert));
                    break;
                case CONFIRMED:
                case WAITING_RIDER:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_green_alert));
                    break;
                default:
                    tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),R.color.eah_black));
                    break;
            }
        }
    }

}
