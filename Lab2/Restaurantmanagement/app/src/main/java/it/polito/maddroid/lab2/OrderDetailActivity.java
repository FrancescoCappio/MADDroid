package it.polito.maddroid.lab2;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.text.DecimalFormat;


public class OrderDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "OrderDetailActivity";
    
    public static final String TIME_MIN_KEY = "TIME_MIN_KEY";
    public static final String TIME_H_KEY = "TIME_H_KEY";
    public static final String DISH_KEY = "DISH_KEY";
    public static final String CUSTOMER_KEY = "CUSTOMER_KEY";
    public static final String RIDER_KEY = "RIDER_KEY";
    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";
    public final static String ORDER_ID_KEY = "ORDER_ID_KEY";

    private  int currentOfferId;

    private EditText etTime;
    private EditText etDish;
    private EditText etCustomer;
    private EditText etRider;
    private EditText etPriceTot;
    
    private int timeHour;
    private int timeMinutes;

    private MenuItem menuEdit;
    private MenuItem menuSave;
    private boolean editMode = false;
    private String pageType;

    DataManager dataManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_activity);

        // set activity title
        getSupportActionBar().setTitle("New Order");
    
        // add back button
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // get references to views
        etTime = findViewById(R.id.et_time);
        etDish = findViewById(R.id.et_dish);
        etCustomer = findViewById(R.id.et_customer);
        etRider = findViewById(R.id.et_idRider);
        etPriceTot = findViewById(R.id.et_priceTot);
        
        
        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        dataManager = DataManager.getInstance(getApplicationContext());

        Intent i  = getIntent();
        pageType = i.getStringExtra(PAGE_TYPE_KEY);
        Log.d("stampa Order Detail ", pageType);
        if (pageType.equals(MODE_NEW)) {
            currentOfferId = dataManager.getNextOrderId();
            editMode = true;
        } else {
            Log.d("stampa Order Detail", String.valueOf(currentOfferId));
            currentOfferId = i.getIntExtra(this.ORDER_ID_KEY, -1);
            Log.d("stampa Order Detail", String.valueOf(currentOfferId));
            editMode = false;

            StringBuilder s = new StringBuilder(5) ;
            Order order= dataManager.getOrderWithId(currentOfferId);

            Log.d("Order Detail Activity", "qui arriva");
            etRider.setText(""+order.getRiderId());
            etCustomer.setText(""+order.getCustomerId());
            etPriceTot.setText("da terminare");
            etDish.setText("da terminare");
            //todo attenzione a come si stampano i minuti tipo 02 03 etc
            DecimalFormat formatter = new DecimalFormat("00");
            String shour = formatter.format(order.getTimeHour());
            String sminutes = formatter.format(order.getTimeMinutes());
            etTime.setText(""+shour+":"+sminutes);

        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (pageType.equals(MODE_NEW))
                getSupportActionBar().setTitle(R.string.new_order);
            else
                getSupportActionBar().setTitle(R.string.order_detail);

            // add back button
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
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
                
                //TODO: check that all the elements have been filled
                
                String dish = etDish.getText().toString();
                String customer = etCustomer.getText().toString();
                int customerId = Integer.parseInt(customer);
                String rider = etRider.getText().toString();
                int riderId = Integer.parseInt(rider);
               
                DataManager dataManager = DataManager.getInstance(getApplicationContext());
                
                Order o = new Order(dataManager.getNextOrderId(), timeHour, timeMinutes, customerId, riderId);

                Intent i  = getIntent();
                pageType = i.getStringExtra(PAGE_TYPE_KEY);

                if (pageType.equals(MODE_NEW)) {
                    dataManager.addNewOrder(getApplicationContext(), o);
                }
                else if(pageType.equals(MODE_SHOW)){
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
        String s;
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(hour);
        String sminutes = formatter.format(minutes);
        s = ""+shour+":"+sminutes;
        etTime.setText(s.toString());
    }
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    
}
