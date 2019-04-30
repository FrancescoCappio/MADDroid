package it.polito.maddroid.lab3.user;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;


public class DishOrderListAdapter extends ListAdapter<Dish, DishOrderListAdapter.MyViewHolder> {
    private static StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    
    protected DishOrderListAdapter(@NonNull DiffUtil.ItemCallback<Dish> diffCallback) {
        super(diffCallback);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
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
        holder.setupDish(getItem(position), currentUser);
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
        
        public void setupDish(Dish dish, FirebaseUser currentUser) {
            
            tvDishName.setText(dish.getName());
            tvDishDescription.setText(dish.getDescription());
            
            tvDishQuantity.setText(dish.getQuantity() +"");
            
            setTotalCost(dish);
            
            StorageReference riversRef = storageReference.child("dish_" + currentUser.getUid() + "_" + dish.getDishID() +".jpg");
            
            GlideApp.with(ivDishPhoto.getContext())
                    .load(riversRef)
                    .into(ivDishPhoto);
            
            ibAddDish.setOnClickListener(v -> {
                dish.setQuantity(dish.getQuantity() + 1);
                setTotalCost(dish);
                tvDishQuantity.setText(dish.getQuantity() + "");
            });
            
            ibRemoveDish.setOnClickListener(v -> {
                int count = dish.getQuantity();
                if (count > 0) {
                    dish.setQuantity(count - 1);
                    setTotalCost(dish);
                    tvDishQuantity.setText(dish.getQuantity());
                }
            });
            
        }
        
        private void setTotalCost(Dish dish) {
            float tot = dish.getQuantity()*dish.getPrice();
            tvDishTotalCost.setText(tot + " €");
        }
        
    }
}
