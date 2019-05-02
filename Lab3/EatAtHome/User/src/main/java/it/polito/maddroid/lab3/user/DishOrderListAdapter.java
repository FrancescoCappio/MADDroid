package it.polito.maddroid.lab3.user;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.Restaurant;


public class DishOrderListAdapter extends ListAdapter<Dish, DishOrderListAdapter.MyViewHolder> {
    private static StorageReference storageReference;

    private Restaurant currentRestaurant;
    private DataUpdatedListener listener;
    
    protected DishOrderListAdapter(@NonNull DiffUtil.ItemCallback<Dish> diffCallback, Restaurant currentRestaurant, DataUpdatedListener listener) {
        super(diffCallback);
        storageReference = FirebaseStorage.getInstance().getReference();
        this.currentRestaurant = currentRestaurant;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_dishes_list_item, parent, false);
    
        // create view holder and pass main view to it
        DishOrderListAdapter.MyViewHolder vh = new DishOrderListAdapter.MyViewHolder(v);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupDish(getItem(position), currentRestaurant,  listener);
    }
    
    public List<Dish> getChosenDishes() {
        List<Dish> dishes = new ArrayList<>();
        
        for (int i = 0; i < getItemCount(); ++i) {
            Dish d = getItem(i);
            if (d.getQuantity() > 0)
                dishes.add(d);
        }
        
        return dishes;
    }
    
    public interface DataUpdatedListener {
        public void notifyDataUpdated();
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvDishName;
        private TextView tvDishDescription;
        private TextView tvDishQuantity;
        private TextView tvDishTotalCost;
        private ImageView ivDishPhoto;
        private ImageButton ibRemoveDish;
        private ImageButton ibAddDish;
        
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvDishDescription = itemView.findViewById(R.id.tv_dish_description);
            tvDishQuantity = itemView.findViewById(R.id.tv_quantity_dishes);
            tvDishTotalCost = itemView.findViewById(R.id.tv_current_dish_cost);
            
            ivDishPhoto = itemView.findViewById(R.id.iv_dish_photo);
            ibAddDish = itemView.findViewById(R.id.ib_add_Dish);
            ibRemoveDish = itemView.findViewById(R.id.ib_remove_Dish);
        }
        
        public void setupDish(Dish dish, Restaurant currentRestaurant, DataUpdatedListener listener) {
            
            tvDishName.setText(dish.getName());
            tvDishDescription.setText(dish.getDescription());
            
            tvDishQuantity.setText(dish.getQuantity() +"");
            
            setTotalCost(dish);
            
            StorageReference riversRef = storageReference.child("dish_" + currentRestaurant.getRestaurantID() + "_" + dish.getDishID() +".jpg");
            
            GlideApp.with(ivDishPhoto.getContext())
                    .load(riversRef)
                    .into(ivDishPhoto);
            
            ibAddDish.setOnClickListener(v -> {
                dish.setQuantity(dish.getQuantity() + 1);
                setTotalCost(dish);
                tvDishQuantity.setText(dish.getQuantity() + "");
                listener.notifyDataUpdated();
            });
            
            ibRemoveDish.setOnClickListener(v -> {
                int count = dish.getQuantity();
                if (count > 0) {
                    dish.setQuantity(count - 1);
                    setTotalCost(dish);
                    tvDishQuantity.setText(String.valueOf(dish.getQuantity()));
                    listener.notifyDataUpdated();
                }
            });
            
        }
        
        private void setTotalCost(Dish dish) {
            float tot = dish.getQuantity()*dish.getPrice();
            tvDishTotalCost.setText(String.format("%.02f", tot) + " €");
        }
        
    }
}
