package it.polito.maddroid.lab2;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChooseDishesAdapter extends BaseAdapter {

    private static final String TAG = "DailyOfferAdapterChooseDishes";

    private ArrayList<DailyOffer> dailyOffers;
    private Context context;
    private HashMap<Integer, Integer> mapDishes;
    DishQuantityListener dishQuantityListener;

    public ChooseDishesAdapter(Map<Integer,Integer> dishes, ArrayList<DailyOffer> dailyOffers, DishQuantityListener listener, Context context) {
        this.dailyOffers = dailyOffers;
        this.context = context;
        this.dishQuantityListener = listener;
        this.mapDishes = new HashMap<>();
        
        for (DailyOffer d : dailyOffers) {
            if (dishes.containsKey(d.getId()))
                mapDishes.put(d.getId(), dishes.get(d.getId()));
            else
                mapDishes.put(d.getId(), 0);
        }
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
        
        //load image into imageview with glide
        Glide.with(context).load(DataManager.getDishImageFile(context,dailyOffer.getId())).centerCrop().into(ivDishPhoto);

        
        tvDishName.setText(dailyOffer.getName());
        tvDishDescription.setText(dailyOffer.getDescription());
        tvDishQuantity.setText("" + mapDishes.get(dailyOffer.getId()));
        tvCurrentCost.setText("" + (mapDishes.get(dailyOffer.getId()) * dailyOffer.getPrice())+ " \u20AC");
        
        //count usato per contare il numero di dailyoffer ordinati
        imageMinus.setOnClickListener(v -> {
    
            // get current chosen quantity
            int cnt = mapDishes.get(dailyOffer.getId());
            
            // increase quantityLeft
            if (cnt > 0) {
    
                dailyOffer.increaseQuantityLeftByOne();
                //update chosen quantity
                cnt--;
                mapDishes.put(dailyOffer.getId(),cnt);
    
                tvDishQuantity.setText("" + cnt);
                tvCurrentCost.setText("" + (cnt * dailyOffer.getPrice())+ " \u20AC");
    
                dishQuantityListener.quantityUpdated();
                
            }
            
        });
        
        imagePlus.setOnClickListener(v -> {
            
            int cnt = mapDishes.get(dailyOffer.getId());
            
            if (dailyOffer.decreaseQuantityLeftByOne()) {
                cnt++;
                mapDishes.put(dailyOffer.getId(), cnt);
    
                tvDishQuantity.setText("" + cnt);
                tvCurrentCost.setText("" + (cnt * dailyOffer.getPrice())+ " \u20AC");
                
                dishQuantityListener.quantityUpdated();
            }
        });

        return rowView;
    }
    
    
    public static interface DishQuantityListener {
        public void quantityUpdated();
    }

    public HashMap<Integer, Integer> getMapDishes() {
        // we do not want to return a map with ids with associated quantity 0
        HashMap<Integer, Integer> dishes = new HashMap<>();
        
        for (int id : mapDishes.keySet()) {
            if (mapDishes.get(id) > 0)
                dishes.put(id, mapDishes.get(id));
        }
        return dishes;
    }

    public void confirmDishesRequest() {
        // when this method is called it means that the user has confirmed is order and so we need to
        // save the dailyorders with the updated quantity left
        
        DataManager dataManager = DataManager.getInstance(context);
        
        for (Map.Entry<Integer,Integer> entry : mapDishes.entrySet()) {
            if (entry.getValue() > 0) {
                
                int id = entry.getKey();
                
                for (DailyOffer d : dailyOffers) {
                    if (d.getId() == id) {
                        dataManager.setDailyOfferWithId(context, d);
                        break;
                    }
                }
                
            }
        }
    }
}
