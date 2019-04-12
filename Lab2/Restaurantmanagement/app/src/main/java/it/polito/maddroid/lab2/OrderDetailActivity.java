package it.polito.maddroid.lab2;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.DecimalFormat;
import java.util.List;


public class OrderDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "OrderDetailActivity";

    private static final int ORDER_CHOOSE_DISHES = 123;
    
    public static final String TIME_MIN_KEY = "TIME_MIN_KEY";
    public static final String TIME_H_KEY = "TIME_H_KEY";
    public static final String DISH_KEY = "DISH_KEY";
    public static final String CUSTOMER_KEY = "CUSTOMER_KEY";
    public static final String RIDER_KEY = "RIDER_KEY";
    
    
    private EditText etTime;
    private EditText etDish;
    private EditText etCustomer;
    private EditText etRider;
    
    private int timeHour;
    private int timeMinutes;
    

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
        
        
        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        ImageButton imageButton = findViewById(R.id.ib_add_Dish1);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), OrderChooseDishesActivity.class);
                startActivityForResult(i,ORDER_CHOOSE_DISHES);
            }

        });
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
                List<DailyOffer> list = (List<DailyOffer>) data.getSerializableExtra("dishesChose");
                for(DailyOffer ii :list){

                }
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
                
                Order n = new Order(dataManager.getNextOrderId(), timeHour, timeMinutes, customerId, riderId);
                
                dataManager.addNewOrder(getApplicationContext(), n);
                
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
        }
        
        return true;
    }
    
    void setTime(int hour, int minutes) {
        timeHour = hour;
        timeMinutes = minutes;
    
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(hour);
        String sminutes = formatter.format(minutes);
        
        etTime.setText(shour + ":" + sminutes);
    }
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    
}
