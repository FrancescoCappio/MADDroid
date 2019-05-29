package it.polito.maddroid.lab3.restaurateur;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;


public class MenuListAdapter extends ListAdapter<Dish, MenuListAdapter.MyViewHolder> {
    
    public static final String MODE_RESTAURANT_MENU = "MODE_RESTAURANT_MENU";
    public static final String MODE_ORDER_DISH_LIST = "MODE_ORDER_DISH_LIST";
    public static final String MODE_MOST_POPULAR_DISHES_LIST = "MODE_MOST_POPULAR_DISHES_LIST";

    private static StorageReference storageReference;
    private ItemClickListener itemClickListener;
    private String adapterMode;
    private String restaurantId;

    protected MenuListAdapter(@NonNull DiffUtil.ItemCallback<Dish> diffCallback, ItemClickListener itemClickListener, String restaurantId, String adapterMode) {
        super(diffCallback);
        storageReference = FirebaseStorage.getInstance().getReference();
        this.itemClickListener = itemClickListener;
        this.adapterMode = adapterMode;
        this.restaurantId = restaurantId;
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
        holder.setupDish(getItem(position), itemClickListener, restaurantId, adapterMode);
    }

    public interface ItemClickListener {
        void clickListener(Dish dish);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tvDishName;
        private TextView tvDishPrice;
        private TextView tvDishDescription;
        private TextView tvQuantity;
        private ImageView ivDishPhoto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDishName = itemView.findViewById(R.id.tv_menu_name);
            tvDishPrice = itemView.findViewById(R.id.tv_menu_price);
            tvDishDescription = itemView.findViewById(R.id.tv_menu_description);
            ivDishPhoto = itemView.findViewById(R.id.iv_menu_photo);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            this.itemView = itemView;
        }

        public void setupDish(Dish dish, ItemClickListener itemClickListener, String restaurantId, String adapterMode) {
            tvDishName.setText(dish.getName());
            tvDishPrice.setText(String.format(Locale.US,"%.02f", dish.getPrice()) + " €");
            tvDishDescription.setText(dish.getDescription());

            StorageReference riversRef = storageReference.child("dish_"+ restaurantId +"_" + dish.getDishID() +".jpg");

            GlideApp.with(ivDishPhoto.getContext())
                    .load(riversRef).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .into(ivDishPhoto);

            itemView.setOnClickListener(v -> itemClickListener.clickListener(dish));
            
            if (adapterMode.equals(MODE_RESTAURANT_MENU)) {
                tvQuantity.setVisibility(View.GONE);
            } else if (adapterMode.equals(MODE_ORDER_DISH_LIST)){
                tvQuantity.setVisibility(View.VISIBLE);
                String quantity = tvQuantity.getContext().getString(R.string.quantity);
                
                tvQuantity.setText(quantity + ": " + dish.getQuantity());
            }
            else if (adapterMode.equals(MODE_MOST_POPULAR_DISHES_LIST)){
                tvDishPrice.setVisibility(View.GONE);
                tvQuantity.setText("Quntity : " + dish.getQuantity());

            }

        }

    }
}
