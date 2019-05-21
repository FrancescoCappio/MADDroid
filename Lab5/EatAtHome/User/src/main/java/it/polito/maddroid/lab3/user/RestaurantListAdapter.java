package it.polito.maddroid.lab3.user;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Restaurant;


public class RestaurantListAdapter extends ListAdapter<Restaurant, RestaurantListAdapter.MyViewHolder> {
    
    private static StorageReference storageReference;
    private ItemClickListener clickListener;
    
    protected RestaurantListAdapter(@NonNull DiffUtil.ItemCallback<Restaurant> diffCallback, ItemClickListener clickListener) {
        super(diffCallback);
        storageReference = FirebaseStorage.getInstance().getReference();
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_list_item, parent, false);
    
        // create view holder and pass main view to it
        RestaurantListAdapter.MyViewHolder vh = new RestaurantListAdapter.MyViewHolder(v);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupRestaurant(getItem(position), clickListener);
    }
    
    public interface ItemClickListener {
        void onItemClickListener(Restaurant restaurant);
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
        
        public void setupRestaurant(Restaurant restaurant, ItemClickListener itemClickListener) {
            tvRestaurantName.setText(restaurant.getName());
            tvRestaurantDescription.setText(restaurant.getDescription());
    
            StorageReference riversRef = storageReference.child("avatar_" + restaurant.getRestaurantID() +".jpg");
    
            GlideApp.with(ivRestaurantPhoto.getContext())
                    .load(riversRef)
                    .into(ivRestaurantPhoto);
            
            itemView.setOnClickListener(v -> itemClickListener.onItemClickListener(restaurant));
        }
        
    }
}
