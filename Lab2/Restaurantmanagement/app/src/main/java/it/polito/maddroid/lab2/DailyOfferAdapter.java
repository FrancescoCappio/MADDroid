package it.polito.maddroid.lab2;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class DailyOfferAdapter extends BaseAdapter {
    
    private static final String TAG = "DailyOfferAdapter";
    
    private ArrayList<DailyOffer> dailyOffers;
    
    private Context context;
    
    public DailyOfferAdapter(List<DailyOffer> dailyOffers, Context context) {
        this.dailyOffers = new ArrayList<>(dailyOffers);
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
        View rowView = inflater.inflate(R.layout.daily_offer_list_item, parent, false);
    
        ImageView ivDishPhoto = rowView.findViewById(R.id.iv_dish_photo);
    
        TextView tvDishName = rowView.findViewById(R.id.tv_dish_name);
        
        TextView tvDishDescription = rowView.findViewById(R.id.tv_dish_description);
        
        TextView tvPrice = rowView.findViewById(R.id.tv_price);
        
        TextView tvQuantity = rowView.findViewById(R.id.tv_quantity);
        
        DailyOffer dailyOffer = dailyOffers.get(position);
        
        
        Bitmap img = DataManager.getInstance(context).getDishBitmap(context, dailyOffer.getId());
        
        if (img != null) {
            ivDishPhoto.setImageBitmap(img);
        }
        
        tvDishName.setText(dailyOffer.getName());
        
        tvDishDescription.setText(dailyOffer.getDescription());
        
        tvPrice.setText("" + dailyOffer.getPrice());
        
        tvQuantity.setText("" + dailyOffer.getQuantity());
        
        return rowView;
    }
    
    
    public void updateList(List<DailyOffer> dOffers) {
        this.dailyOffers = new ArrayList<>(dOffers);
        notifyDataSetChanged();
    }
    
    
    
}
