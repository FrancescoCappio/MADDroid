package it.polito.maddroid.lab2;

import android.app.Activity;
import android.content.Intent;
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
    private HashMap<Integer, Integer> mapDishes;

    DailyOfferAdapterForChooseDishes adapter;

    TextView tvTotalcost ;

    List<DailyOffer> dailyOffers;


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

        adapter = new DailyOfferAdapterForChooseDishes(new HashMap<>(), new ArrayList<>(dailyOffers), () -> setTotalCost(), getApplicationContext());

        lvChooseDishes.setAdapter(adapter);

    }
    

    public void setTotalCost(){
        double dishCostTotal = 0;
        for(Map.Entry<Integer, Integer> entry : adapter.getMapDishes().entrySet()){
            dishCostTotal += entry.getValue() * DataManager.getInstance(getApplicationContext()).getDailyOfferWithId(entry.getKey()).getPrice();
        }

        tvTotalcost.setText(""+ dishCostTotal  + " \u20AC");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);

        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_confirm);

        menuEdit.setVisible(false);
        menuSave.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent data = new Intent();
        mapDishes = new HashMap<>();
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, data);
                finish();
                break;

            case R.id.menu_confirm:
                Log.d(TAG, "Confirm pressed");
                mapDishes = adapter.getMapDishes();
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
        }

        return true;
    }

}
