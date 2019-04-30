package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import it.polito.maddroid.lab3.common.Dish;


public class DishDiffUtilCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<Dish> {
    @Override
    public boolean areItemsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
        return oldItem.getDishID().equals(newItem.getDishID());
    }
    
    @Override
    public boolean areContentsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
        return oldItem.getDishID().equals(newItem.getDishID());
    }
}
