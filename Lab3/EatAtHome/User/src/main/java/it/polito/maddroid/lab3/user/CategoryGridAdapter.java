package it.polito.maddroid.lab3.user;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.RestaurantCategory;


public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.MyViewHolder>{
    
    private List<RestaurantCategory> categories;
    private static StorageReference storageReference;
    
    public CategoryGridAdapter(List<RestaurantCategory> categories) {
        this.categories = categories;
        
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvCatName;
        private ImageView ivCatImage;
        
        public MyViewHolder(View v) {
            super(v);
            
            tvCatName = v.findViewById(R.id.tv_cat_name);
            ivCatImage = v.findViewById(R.id.iv_cat_image);
            
        }
        
        public void setupCategory(RestaurantCategory category) {
            tvCatName.setText(category.getName());
    
            StorageReference riversRef = storageReference.child("rest_cat_" + category.getId() +".jpg");
            
            GlideApp.with(ivCatImage.getContext())
                    .load(riversRef)
                    .into(ivCatImage);
        }
    }
    
    @Override
    public CategoryGridAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
                
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item, parent, false);
       
        // create view holder and pass main view to it
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        
        // pass the category to the viewholder
        holder.setupCategory(categories.get(position));
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
}
