package it.polito.maddroid.lab3.common;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class RidersListAdapter extends ListAdapter<Rider, RidersListAdapter.MyViewHolder> {

    public static final String TAG = "RidersListAdapter";
    
    private ItemClickListener clickListener;

    
    protected RidersListAdapter(@NonNull DiffUtil.ItemCallback<Rider> diffCallback, ItemClickListener clickListener) {
        super(diffCallback);

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
        private ImageView ivRiderAvatar;
        private RatingBar ratingBar;
        private Context context;
        private String riderId;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            tvRiderName = itemView.findViewById(R.id.tv_rider_name);
            tvRiderDistance = itemView.findViewById(R.id.tv_rider_distance);
            ivRiderAvatar = itemView.findViewById(R.id.iv_rider_avatar);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            this.itemView = itemView;
            
        }
        
        public void setupRider(Rider rider, ItemClickListener itemClickListener) {
            
            tvRiderName.setText(rider.getName());
            tvRiderDistance.setText(String.format(Locale.US, "%.02f",rider.getDistance()) + " Km");
            ratingBar.setRating(rider.getAverageReview());

            riderId = rider.getId();
            downloadAvatar(riderId);
            
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(rider));
        }
    
        private void downloadAvatar(String UID) {
            File localFile = getAvatarFile();
            
            if (localFile.exists()) {
                updateAvatarImage();
                return;
            }
            
            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("avatar_" + UID +".jpg");
        
            riversRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Avatar downloaded successfully");
                    updateAvatarImage();
                }).addOnFailureListener(exception -> {
                    Log.e(TAG, "Error while downloading avatar image: " + exception.getMessage());
                });
        }

        private void updateAvatarImage() {
            File img = getAvatarFile();

            if (!img.exists() || !img.isFile()) {
                Log.d(TAG, "Cannot load unexisting file as avatar");
                return;
            }
            
            try {
    
                Glide.with(context).load(img).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivRiderAvatar);
    
            } catch (Exception ex) {
                Log.e(TAG, "Exception: " + ex.getMessage());
            }
        }
        
        private File getAvatarFile() {
            // Determine Uri of camera image to save.
            final File root = new File(context.getFilesDir() + File.separator + "images" + File.separator);
            root.mkdirs();
            final String fname = "Rider_avatar_tmp_" + riderId + ".jpg";
            return new File(root, fname);
        }
    }
}