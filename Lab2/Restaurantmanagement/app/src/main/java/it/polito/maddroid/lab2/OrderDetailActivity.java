package it.polito.maddroid.lab2;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;


public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    private static final int ORDER_CHOOSE_DISHES = 123;
    
    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";
    public final static String ORDER_ID_KEY = "ORDER_ID_KEY";
    
    private  int currentOrderId;
    
    private EditText etTime;
    private EditText etCustomer;
    private EditText etRider;
    private TextView tvTotCost;
    private Button btChooseDishes;
    private HashMap<Integer, Integer> mapDishes;
    
    private int timeHour = -1;
    private int timeMinutes = -1;

    private MenuItem menuEdit;
    private MenuItem menuSave;
    private MenuItem menuDelete;
    private boolean editMode = false;
    private String pageType;
    
    public static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    public static final String TIME_HOUR_KEY = "TIME_HOUR_KEY";
    public static final String TIME_MIN_KEY = "TIME_MIN_KEY";
    public static final String CURRENT_ORDER_ID_KEY = "CURRENT_ORDER_ID_KEY";
    public static final String RIDER_ID_KEY = "RIDER_ID_KEY";
    public static final String CUSTOMER_ID_KEY = "CUSTOMER_ID_KEY";

    DataManager dataManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // add back button
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // get references to views
        etTime = findViewById(R.id.et_time);
        etCustomer = findViewById(R.id.et_customer);
        etRider = findViewById(R.id.et_idRider);
        tvTotCost = findViewById(R.id.tv_total_cost);
        
        
        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(v -> showTimePickerDialog());

        btChooseDishes = findViewById(R.id.ib_choose_dishes);
        
        dataManager = DataManager.getInstance(getApplicationContext());
    
        Intent i  = getIntent();
        pageType = i.getStringExtra(PAGE_TYPE_KEY);
        
        if (pageType.equals(MODE_NEW)) {
            currentOrderId = dataManager.getNextOrderId();
            editMode = true;
        } else {
            // we need to load the order from the file system
            
            currentOrderId = i.getIntExtra(this.ORDER_ID_KEY, -1);
            
            editMode = false;
        
            Order order = dataManager.getOrderWithId(currentOrderId);
        
            etRider.setText(""+order.getRiderId());
            etCustomer.setText(""+order.getCustomerId());
            
            setTotalCost(order.getTotPrice(getApplicationContext()));
            
            mapDishes = order.getDishes();
            
            // notice format for minutes and hours
            timeHour = order.getTimeHour();
            timeMinutes = order.getTimeMinutes();
            
            writeTime();
        }
    
        btChooseDishes.setOnClickListener(arg0 -> {
            Intent intent = new Intent(getApplicationContext(), OrderChooseDishesActivity.class);
            if (pageType.equals(MODE_NEW)) {
                intent.putExtra(OrderChooseDishesActivity.PAGE_TYPE_KEY, OrderChooseDishesActivity.MODE_NEW);
            } else {
                intent.putExtra(OrderChooseDishesActivity.PAGE_TYPE_KEY, OrderChooseDishesActivity.MODE_SHOW);
                intent.putExtra(OrderChooseDishesActivity.ORDER_CHOOSE_DISHES_KEY, mapDishes);
            }
            startActivityForResult(intent,ORDER_CHOOSE_DISHES);
        });
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (pageType.equals(MODE_NEW))
                getSupportActionBar().setTitle(R.string.new_order);
            else
                getSupportActionBar().setTitle(R.string.detail);
        
            // add back button
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }

        if (data == null) {
            Log.e(TAG, "Result data null");
            return;
        }

        switch (requestCode) {
            case ORDER_CHOOSE_DISHES:
                mapDishes = (HashMap<Integer, Integer>) data.getSerializableExtra(OrderChooseDishesActivity.ORDER_CHOOSE_DISHES_KEY);
                float totalCost = data.getFloatExtra(OrderChooseDishesActivity.TOTAL_COST_KEY, 0);
                setTotalCost(totalCost);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);
    
        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_confirm);
        menuDelete = menu.findItem(R.id.menu_delete);
    
        //enable/disable edit depending on the state
        setEditEnabled(editMode);
        
        return true;
    }
    
    private void setEditEnabled(boolean enabled) {
        editMode = enabled;
        menuEdit.setVisible(!enabled);
        menuSave.setVisible(enabled);
        if (pageType.equals(MODE_NEW)){
            menuDelete.setVisible(!enabled);
        }else {
            menuDelete.setVisible(enabled);
        }
        
        etTime.setEnabled(enabled);
        etCustomer.setEnabled(enabled);
        etRider.setEnabled(enabled);
        btChooseDishes.setEnabled(enabled);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(EDIT_MODE_KEY, editMode);
        outState.putInt(TIME_HOUR_KEY, timeHour);
        outState.putInt(TIME_MIN_KEY, timeMinutes);
        
        outState.putInt(CURRENT_ORDER_ID_KEY, currentOrderId);
        
        String riderIdStr = etRider.getText().toString();
        if (!riderIdStr.isEmpty())
            outState.putInt(RIDER_ID_KEY, Integer.parseInt(riderIdStr));
    
        String customerIdStr = etCustomer.getText().toString();
        if (!customerIdStr.isEmpty())
            outState.putInt(CUSTOMER_ID_KEY, Integer.parseInt(customerIdStr));
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        int customerId = savedInstanceState.getInt(CUSTOMER_ID_KEY, -1);
        
        if (customerId != -1)
            etCustomer.setText("" + customerId);
        
        int riderId = savedInstanceState.getInt(RIDER_ID_KEY, -1);
        
        if (riderId != -1)
            etRider.setText("" + riderId);
        
        timeHour = savedInstanceState.getInt(TIME_HOUR_KEY, -1);
        timeMinutes = savedInstanceState.getInt(TIME_MIN_KEY, -1);
        writeTime();
        
        editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
        
        currentOrderId = savedInstanceState.getInt(CURRENT_ORDER_ID_KEY);
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
                
            case R.id.menu_edit:
                setEditEnabled(true);
                break;
    
            case R.id.menu_delete:
                DataManager.getInstance(this).deleteOrderWithId(this, currentOrderId);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
                
            case R.id.menu_confirm:
                Log.d(TAG, "Confirm pressed");
                
                //TODO: check that all the elements have been filled
                if((timeHour == -1) || (timeMinutes == -1)  || (mapDishes == null || mapDishes.isEmpty()) ||
                        (etCustomer.getText().toString().isEmpty()) ||
                        (etRider.getText().toString().isEmpty())) {

                    Snackbar.make(etCustomer, R.string.fill_fields, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                String customer = etCustomer.getText().toString();
                int customerId = Integer.parseInt(customer);
                String rider = etRider.getText().toString();
                int riderId = Integer.parseInt(rider);
               
                DataManager dataManager = DataManager.getInstance(getApplicationContext());
                
                if (pageType.equals(MODE_NEW)) {
                    Order o = new Order(dataManager.getNextOrderId(), timeHour, timeMinutes, customerId, riderId);
                    o.setDishesMap(mapDishes, getApplicationContext());
                    dataManager.addNewOrder(getApplicationContext(), o);
                } else if(pageType.equals(MODE_SHOW)) {
                    Order o = dataManager.getOrderWithId(currentOrderId);
                    
                    o.setCustomerId(customerId);
                    o.setRiderId(riderId);
                    o.setTimeHour(timeHour);
                    o.setTimeMinutes(timeMinutes);
                    o.setDishesMap(mapDishes, getApplicationContext());
                    
                    dataManager.setOrderWithID(getApplicationContext(), o);
                }
                
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
        }
        
        return true;
    }
    
    void setTime(int hour, int minutes) {
        timeHour = hour;
        timeMinutes = minutes;
        writeTime();
    }
    
    private void writeTime() {
        
        if (timeMinutes == -1 || timeHour == -1)
            return;
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(timeHour);
        String sminutes = formatter.format(timeMinutes);
        etTime.setText(shour + ":" + sminutes);
    }
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    
    private void setTotalCost(float totalCost) {
        String totalCostRes = getResources().getString(R.string.total);
        tvTotCost.setText(totalCostRes + ": " + String.format("%.2f",totalCost) + " €");
    }
    
}
