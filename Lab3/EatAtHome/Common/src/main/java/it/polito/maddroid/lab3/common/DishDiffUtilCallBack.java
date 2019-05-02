package it.polito.maddroid.lab3.common;

import androidx.annotation.NonNull;
import it.polito.maddroid.lab3.common.Dish;


public class DishDiffUtilCallBack extends androidx.recyclerview.widget.DiffUtil.ItemCallback<Dish> {
    @Override
    public boolean areItemsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
        return oldItem.getDishID() == newItem.getDishID();
    }

    @Override
    public boolean areContentsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
        return oldItem.getName().equals(newItem.getName()) &&
                (Math.abs(oldItem.getPrice() - newItem.getPrice()) < 0.001) &&
                oldItem.getDescription().equals(newItem.getDescription()) &&
                oldItem.getUpdate() == newItem.getUpdate();

    }
}
