package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import it.polito.maddroid.lab3.common.Restaurant;


public class RestaurantDiffUtilCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<Restaurant> {
    @Override
    public boolean areItemsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
        return oldItem.getRestaurantID().equals(newItem.getRestaurantID());
    }
    
    @Override
    public boolean areContentsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
        return oldItem.getRestaurantID().equals(newItem.getRestaurantID());
    }
}
