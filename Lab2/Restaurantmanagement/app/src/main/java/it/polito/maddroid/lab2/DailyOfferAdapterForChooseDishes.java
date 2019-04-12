package it.polito.maddroid.lab2;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class DailyOfferAdapterForChooseDishes extends BaseAdapter {

    private static final String TAG = "DailyOfferAdapterChooseDishes";

    private ArrayList<DailyOffer> dailyOffers;

    private Context context;
    
    DishQuantityListener dishQuantityListener;

    public DailyOfferAdapterForChooseDishes(ArrayList<DailyOffer> dailyOffers, DishQuantityListener listener, Context context) {
        this.dailyOffers = dailyOffers;
        this.context = context;
        this.dishQuantityListener = listener;
    }

    @Override
    public int getCount() {
        return dailyOffers.size();
    }

    @Override
    public Object getItem(int position) {
        return dailyOffers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dailyOffers.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.choose_dishes_list_item, parent, false);

        ImageView ivDishPhoto = rowView.findViewById(R.id.iv_dish_photo);

        TextView tvDishName = rowView.findViewById(R.id.tv_dish_name);
        
        TextView tvDishDescription = rowView.findViewById(R.id.tv_dish_description);

        TextView tvDishQuantity = rowView.findViewById(R.id.tv_quantity_dishes);

        TextView tvCurrentCost = rowView.findViewById(R.id.tv_Current_dish_cost);

        ImageButton imageMinus = rowView.findViewById(R.id.ib_remove_Dish);

        ImageButton imagePlus = rowView.findViewById(R.id.ib_add_Dish);

        DailyOffer dailyOffer = dailyOffers.get(position);


        Bitmap img = DataManager.getInstance(context).getDishBitmap(context, dailyOffer.getId());

        if (img != null) {
            ivDishPhoto.setImageBitmap(img);
        }

        tvDishName.setText(dailyOffer.getName());
        
        tvDishDescription.setText(dailyOffer.getDescription());

        tvDishQuantity.setText("" + dailyOffer.getQuantityChosen());

        tvCurrentCost.setText("" + (dailyOffer.getQuantityChosen() * dailyOffer.getPrice())+ " \u20AC");
        
        imageMinus.setOnClickListener(v -> {
            dailyOffer.removeFromQuantity();
            tvDishQuantity.setText("" + dailyOffer.getQuantityChosen());
            tvCurrentCost.setText("" + (dailyOffer.getQuantityChosen() * dailyOffer.getPrice())+ " \u20AC");
            dishQuantityListener.quantityUpdated();
        });
        
        imagePlus.setOnClickListener(v -> {
            dailyOffer.addToQuantity();
            tvDishQuantity.setText("" + dailyOffer.getQuantityChosen());
            tvCurrentCost.setText("" + (dailyOffer.getQuantityChosen() * dailyOffer.getPrice())+ " \u20AC");
            dishQuantityListener.quantityUpdated();
        });
        
        return rowView;
    }
    
    
    public static interface DishQuantityListener {
        public void quantityUpdated();
    }


}
