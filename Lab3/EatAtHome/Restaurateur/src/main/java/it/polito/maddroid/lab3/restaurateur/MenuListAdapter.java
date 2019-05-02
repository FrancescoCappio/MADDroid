package it.polito.maddroid.lab3.restaurateur;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;


public class MenuListAdapter extends ListAdapter<Dish, MenuListAdapter.MyViewHolder> {

    private static StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ItemClickListener itemClickListener;

    protected MenuListAdapter(@NonNull DiffUtil.ItemCallback<Dish> diffCallback, ItemClickListener itemClickListener) {
        super(diffCallback);
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_list_item, parent, false);

        // create view holder and pass main view to it
        MenuListAdapter.MyViewHolder vh = new MenuListAdapter.MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupDish(getItem(position), itemClickListener, currentUser.getUid());
    }

    public interface ItemClickListener {
        void clickListener(Dish dish);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tvDishName;
        private TextView tvDishPrice;
        private TextView tvDishDescription;
        private ImageView ivDishPhoto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDishName = itemView.findViewById(R.id.tv_menu_name);
            tvDishPrice = itemView.findViewById(R.id.tv_menu_price);
            tvDishDescription = itemView.findViewById(R.id.tv_menu_description);
            ivDishPhoto = itemView.findViewById(R.id.iv_menu_photo);
            this.itemView = itemView;
        }

        public void setupDish(Dish dish, ItemClickListener itemClickListener,String userUID) {
            tvDishName.setText(dish.getName());
            tvDishPrice.setText(String.valueOf(dish.getPrice()));
            tvDishDescription.setText(dish.getDescription());

            StorageReference riversRef = storageReference.child("dish_"+ userUID+"_" + dish.getDishID() +".jpg");

            GlideApp.with(ivDishPhoto.getContext())
                    .load(riversRef).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .into(ivDishPhoto);

            itemView.setOnClickListener(v -> itemClickListener.clickListener(dish));

        }

    }
}
