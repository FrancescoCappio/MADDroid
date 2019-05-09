package it.polito.maddroid.lab3.rider;


import androidx.annotation.NonNull;
import it.polito.maddroid.lab3.common.Order;


public class RiderOrderDeliveryDiffUtilCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<RiderOrderDelivery> {
    @Override
    public boolean areItemsTheSame(@NonNull RiderOrderDelivery oldItem, @NonNull RiderOrderDelivery newItem) {
        return oldItem.getOrderId().equals(newItem.getOrderId());
    }
    
    @Override
    public boolean areContentsTheSame(@NonNull RiderOrderDelivery oldItem, @NonNull RiderOrderDelivery newItem) {
        return oldItem.getOrderId().equals(newItem.getOrderId());
    }
}
