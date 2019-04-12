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


    public DailyOfferAdapterForChooseDishes(ArrayList<DailyOffer> dailyOffers, Context context) {
        this.dailyOffers = dailyOffers;
        this.context = context;
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

        tvDishQuantity.setText("" + dailyOffer.getQuantityChose());

        tvCurrentCost.setText("" + (dailyOffer.getQuantityChose() * dailyOffer.getPrice())+ " \u20AC");

//        imageMinus.setImageResource(R.drawable.ic_remove_circle_outline_24dp);
//
//        imagePlus.setImageResource(R.drawable.ic_add_circle_outline_24dp);

        imageMinus.setOnClickListener(v -> {
            dailyOffer.removeFromQuantity();
            tvDishQuantity.setText("" + dailyOffer.getQuantityChose());
            tvCurrentCost.setText("" + (dailyOffer.getQuantityChose() * dailyOffer.getPrice())+ " \u20AC");
            notifyDataSetChanged();
        });
        
        imagePlus.setOnClickListener(v -> {
            dailyOffer.addToQuantity();
            tvDishQuantity.setText("" + dailyOffer.getQuantityChose());
            tvCurrentCost.setText("" + (dailyOffer.getQuantityChose() * dailyOffer.getPrice())+ " \u20AC");
            notifyDataSetChanged();
        });

        return rowView;
    }


}
