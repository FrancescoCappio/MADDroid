package it.polito.maddroid.lab3.common;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Locale;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class RidersListAdapter extends ListAdapter<Rider, RidersListAdapter.MyViewHolder> {
    
    private ItemClickListener clickListener;
    private static StorageReference storageReference;
    
    protected RidersListAdapter(@NonNull DiffUtil.ItemCallback<Rider> diffCallback, ItemClickListener clickListener) {
        super(diffCallback);

        storageReference = FirebaseStorage.getInstance().getReference();
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.riders_list_item, parent, false);
    
        // create view holder and pass main view to it
        return new MyViewHolder(v);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupRider(getItem(position), clickListener);
    }
    
    public interface ItemClickListener {
        void onItemClick(Rider rider);
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private View itemView;
        private TextView tvRiderName;
        private TextView tvRiderDistance;
        private ImageView ivRiverAvatar;
        private RatingBar ratingBar;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            tvRiderName = itemView.findViewById(R.id.tv_rider_name);
            tvRiderDistance = itemView.findViewById(R.id.tv_rider_distance);
            ivRiverAvatar = itemView.findViewById(R.id.iv_rider_avatar);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            this.itemView = itemView;
            
        }
        
        public void setupRider(Rider rider, ItemClickListener itemClickListener) {
            
            tvRiderName.setText(rider.getName());
            tvRiderDistance.setText(String.format(Locale.US, "%.02f",rider.getDistance()) + " Km");
            ratingBar.setRating(rider.getAverageReview());

            StorageReference riversRef = storageReference.child("avatar_" + rider.getId() +".jpg");
            GlideApp.with(ivRiverAvatar.getContext())
                    .load(riversRef)
                    .placeholder(R.drawable.placeholder_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivRiverAvatar);
            
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(rider));
        }
    }
}