package it.polito.maddroid.lab2;

import android.app.Activity;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderChooseDishesActivity extends AppCompatActivity {

    private static final String TAG = "OrderChooseDishActivity";


    private MenuItem menuEdit;
    private MenuItem menuSave;

    DataManager dataManager;
    private ListView lvChooseDishes;

    ChooseDishesAdapter adapter;

    TextView tvTotalcost ;

    List<DailyOffer> dailyOffers;
    
    public static final String ORDER_CHOOSE_DISHES_KEY = "ORDER_CHOOSE_DISHES_KEY";
    public static final String TOTAL_COST_KEY = "TOTAL_COST_KEY";
    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";

    private float dishCostTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_choose_dishes);

        getSupportActionBar().setTitle(R.string.choose_dishes);

        tvTotalcost = findViewById(R.id.tv_total_cost);

        //get reference to dataManager
        dataManager = DataManager.getInstance(getApplicationContext());

        lvChooseDishes = findViewById(R.id.lv_choose_dishes);

        dailyOffers = DataManager.getInstance(getApplicationContext()).getDailyOffers();
        
        Intent i = getIntent();
        
        Bundle b;
        
        if (savedInstanceState == null)
            b = i.getExtras();
        else
            b = savedInstanceState;
        
        String pageType = b.getString(PAGE_TYPE_KEY);
        
        if (pageType.equals(MODE_NEW))
            adapter = new ChooseDishesAdapter(new HashMap<>(), new ArrayList<>(dailyOffers), () -> setTotalCost(), getApplicationContext());
        else {
            Map<Integer,Integer> map = (Map<Integer, Integer>) b.getSerializable(ORDER_CHOOSE_DISHES_KEY);
            adapter = new ChooseDishesAdapter(map, new ArrayList<>(dailyOffers), () -> setTotalCost(), getApplicationContext());
        }
    
        setTotalCost();
        lvChooseDishes.setAdapter(adapter);
    
        // add back button
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    

    public void setTotalCost(){
        dishCostTotal = 0;
        for(Map.Entry<Integer, Integer> entry : adapter.getMapDishes().entrySet()){
            dishCostTotal += entry.getValue() * DataManager.getInstance(getApplicationContext()).getDailyOfferWithId(entry.getKey()).getPrice();
        }
        
        tvTotalcost.setText(String.format("%.02f", dishCostTotal) + " €");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);

        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_confirm);
        MenuItem menuDelete = menu.findItem(R.id.menu_delete);
        
        menuDelete.setVisible(false);
        menuEdit.setVisible(false);
        menuSave.setVisible(true);
        return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(PAGE_TYPE_KEY, MODE_SHOW);
        
        Map<Integer,Integer> map = adapter.getMapDishes();
        outState.putSerializable(ORDER_CHOOSE_DISHES_KEY, (Serializable) map);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent data = new Intent();
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, data);
                finish();
                break;

            case R.id.menu_confirm:
                Log.d(TAG, "Confirm pressed");
                Map<Integer,Integer> mapDishes = adapter.getMapDishes();
                data.putExtra(ORDER_CHOOSE_DISHES_KEY, (Serializable) mapDishes);
                data.putExtra(TOTAL_COST_KEY, dishCostTotal);
                setResult(Activity.RESULT_OK, data);
                //save dishes request
                adapter.confirmDishesRequest();
                finish();
                break;
        }

        return true;
    }

}
