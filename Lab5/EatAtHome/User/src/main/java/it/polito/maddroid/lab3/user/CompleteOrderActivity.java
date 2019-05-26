package it.polito.maddroid.lab3.user;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.GeocodingLocation;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.RoutingUtility;
import it.polito.maddroid.lab3.common.TimePickerFragment;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CompleteOrderActivity extends AppCompatActivity {
    
    public static final String TAG = "CompleteOrderActivity";
    public static final String DISHES_KEY = "DISHES_KEY";
    public static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    public static final String ADDRESS_KEY = "ADDRESS_KEY";
    public static final String ADDRESSES_KEY = "ADDRESSES_KEY";
    public static final String ADDRESS_NOTES_KEY = "ADDRESS_NOTES_KEY";
    public static final String TIME_KEY = "TIME_KEY";
    public static final String POSITION_KEY = "POSITION_KEY";
    public static final String CHOICE_KEY = "CHOICE_KEY";
    public static final String POSITION_DIALOG_KEY = "POSITION_DIALOG_KEY";
    public static final String DELIVERY_TIME_DIALOG_KEY = "DELIVERY_TIME_DIALOG_KEY";
    public static final String TOTAL_MIN_TIME_KEY = "TOTAL_MIN_TIME_KEY";
    public static final String COMPUTED_DELIVERY_TIME_KEY = "COMPUTED_DELIVERY_TIME_KEY";
    public static final String CHOSEN_ADDRESS_KEY = "CHOSEN_ADDRESS_KEY";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    // general purpose attributes
    private int waitingCount = 0;
    private List<Dish> selectedDishes;
    private Restaurant currentRestaurant;
    private AlertDialog possiblePosition;
    private List<Address> addressList;
    private Address address;
    private String[] multiChoiceItems;
    private int choice;
    private int deliveryTimeMinutes = -1;
    private boolean positionDialogOpen = false;
    private String chosenDeliveryTime;
    private int totalMinimumTime = -1;
    private boolean timeDialogOpen = false;
    private AlertDialog deliveryTimeDialog;
    
    private String currentUserDefaultAddress;
    private String currentUserDefaultAddressNotes;
    
    private TextView tvTotalCost;
    private EditText etDeliveryAddress;
    private Button btConfirmOrder;
    private TextView tvDeliveryCost;
    private EditText etAddressNotes;
    private CheckBox cbAccountAddress;
    private EditText etTimeDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);
        
        ActionBar actionBar = getSupportActionBar();
        
        if (actionBar != null) {
            actionBar.setTitle(R.string.complete_order);
            
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        getReferencesToViews();
        
        if (savedInstanceState == null) {
            Intent i = getIntent();
            Serializable dishesExtra = i.getSerializableExtra(DISHES_KEY);
            Serializable restaurantExtra = i.getSerializableExtra(RESTAURANT_KEY);
            if (dishesExtra == null || restaurantExtra == null) {
                Utility.showAlertToUser(this, R.string.alert_order_problem);
                finish();
                return;
            }
    
            currentRestaurant = (Restaurant) restaurantExtra;
            selectedDishes = (List<Dish>) dishesExtra;
    
            downloadCurrentUserInfo();
    
        } else {
            
            Serializable dishesExtra = savedInstanceState.getSerializable(DISHES_KEY);
            Serializable restaurantExtra = savedInstanceState.getSerializable(RESTAURANT_KEY);
            if (dishesExtra == null || restaurantExtra == null) {
                Utility.showAlertToUser(this, R.string.alert_order_problem);
                finish();
                return;
            }
    
            currentRestaurant = (Restaurant) restaurantExtra;
            selectedDishes = (List<Dish>) dishesExtra;
            
            etDeliveryAddress.setText(savedInstanceState.getString(ADDRESS_KEY, ""));
            etAddressNotes.setText(savedInstanceState.getString(ADDRESS_NOTES_KEY, ""));
            
            timeDialogOpen = savedInstanceState.getBoolean(DELIVERY_TIME_DIALOG_KEY, false);
            positionDialogOpen = savedInstanceState.getBoolean(POSITION_DIALOG_KEY, false);
            choice = savedInstanceState.getInt(CHOICE_KEY);
            multiChoiceItems = (String[]) savedInstanceState.getSerializable(POSITION_KEY);
            addressList = (List<Address>) savedInstanceState.getSerializable(ADDRESSES_KEY);

            if (etDeliveryAddress.getText().toString().isEmpty())
                downloadCurrentUserInfo();
            if  (positionDialogOpen) {
                showPositionDialog(multiChoiceItems, choice);
            }
    
            totalMinimumTime = savedInstanceState.getInt(TOTAL_MIN_TIME_KEY, -1);
            deliveryTimeMinutes = savedInstanceState.getInt(COMPUTED_DELIVERY_TIME_KEY, -1);
            
            address = savedInstanceState.getParcelable(CHOSEN_ADDRESS_KEY);
            
            if (timeDialogOpen)
                openDeliveryTimeDialog();
            if (etTimeDialog != null)
                etTimeDialog.setText(savedInstanceState.getString(TIME_KEY, ""));
            
        }
        String totalCost = computeTotalCost(true);
        tvTotalCost.setText(totalCost);
        tvDeliveryCost.setText(String.format(Locale.US,"%.02f", EAHCONST.DELIVERY_COST) + " €");
        
        btConfirmOrder.setOnClickListener(v ->{
            String userDeliveryAddress = etDeliveryAddress.getText().toString();
            if(userDeliveryAddress.isEmpty())
            {
                Utility.showAlertToUser(this, R.string.fields_empty_alert);
                return;
            }
            else
            {
                GeocodingLocation locationAddress = new GeocodingLocation();
                setActivityLoading(true);
                locationAddress.getAddressFromLocation(userDeliveryAddress, getApplicationContext(), new CompleteOrderActivity.GeocoderHandler());
            }
        } );
        
        cbAccountAddress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAddressNotes.setVisibility(View.GONE);
                etDeliveryAddress.setEnabled(false);
                
                if (currentUserDefaultAddress != null)
                    etDeliveryAddress.setText(currentUserDefaultAddress);
            } else {
                etAddressNotes.setVisibility(View.VISIBLE);
                etDeliveryAddress.setEnabled(true);
            }
    
        });
    
        // do not open keyboard on activity open
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    
    private void getReferencesToViews() {
        
        tvTotalCost = findViewById(R.id.tv_payment_total);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        btConfirmOrder = findViewById(R.id.bt_confirm);
        tvDeliveryCost = findViewById(R.id.tv_delivery_cost);
        etAddressNotes = findViewById(R.id.et_delivery_address_notes);
        cbAccountAddress = findViewById(R.id.cb_address);
        
    }
    
    private synchronized void setActivityLoading(boolean loading) {
        // this method is necessary to show the user when the activity is doing a network operation
        // as downloading data or uploading data
        // how to use: call with loading = true to notify that a new transmission has been started
        // call with loading = false to notify end of transmission
        
        ProgressBar pbLoading = findViewById(R.id.pb_loading);
        if (loading) {
            if (waitingCount == 0)
                pbLoading.setVisibility(View.VISIBLE);
            waitingCount++;
        } else {
            waitingCount--;
            if (waitingCount == 0)
                pbLoading.setVisibility(View.INVISIBLE);
        }
    }
    
    private String computeTotalCost(boolean addDelivery) {
        float totalCost = 0;
        
        for (Dish d : selectedDishes) {
            totalCost += d.getQuantity() * d.getPrice();
        }

        if(addDelivery == true)
            totalCost += EAHCONST.DELIVERY_COST;
        
        return String.format(Locale.US, "%.02f", totalCost) + " €";
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        
        return false;
    }
    
    private void downloadCurrentUserInfo() {
        setActivityLoading(true);
        
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                
                if (dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS).getValue() == null) {
                    Log.e(TAG, "Cannot download current user info");
                    setActivityLoading(false);
                    return;
                }
                
                currentUserDefaultAddress = (String) dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS).getValue();
                currentUserDefaultAddressNotes = (String) dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS_NOTES).getValue();
                etDeliveryAddress.setText(currentUserDefaultAddress);
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error while downloading customer info: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
    }
    
    private void checkDistanceAndConfirmOrder() {
        setActivityLoading(true);
        new RoutingUtility(this,
                new LatLng(currentRestaurant.getGeoLocation().getLatitude(), currentRestaurant.getGeoLocation().getLongitude()),
                new LatLng(address.getLatitude(), address.getLongitude()),
                new RoutingUtility.GetRouteCaller() {
            @Override
            public void routeCallback(List<List<HashMap<String, String>>> route, String[] distances, int minutes) {
                deliveryTimeMinutes = minutes;
                setActivityLoading(false);
                openDeliveryTimeDialog();
            }
    
            @Override
            public void routeErrorCallback(Exception e) {
                setActivityLoading(false);
                Log.e(TAG, "Exception on routing: " + e.getMessage());
                Utility.showAlertToUser(CompleteOrderActivity.this, R.string.alert_computing_distance);
            }
        });
    }
    
    private void actionConfirmOrder() {
        //checks
        
        if (selectedDishes == null || selectedDishes.isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_order_no_dishes);
            return;
        }
        
        if (etDeliveryAddress.getText().toString().isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_order_no_address);
            return;
        }
        
        if (!cbAccountAddress.isChecked()) {
            if (etAddressNotes.getText().toString().isEmpty()) {
                Utility.showAlertToUser(this, R.string.alert_order_no_address_notes);
                return;
            }
        }
        
        setActivityLoading(true);
    
        Map<String,Object> updateMap = new HashMap<>();
        
        String orderId = Utility.generateUUID();
    
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        
        String deliveryAddress = etDeliveryAddress.getText().toString();
        
        String addressNotes;
        if (cbAccountAddress.isChecked())
            addressNotes = currentUserDefaultAddressNotes;
        else
            addressNotes = etAddressNotes.getText().toString();
        
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.PENDING;
        
        // put everything related to order from point of view of restaurateur
        String restOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE,
                currentRestaurant.getRestaurantID(),
                orderId);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_STATUS),orderStatus);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DATE),date);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DELIVERY_TIME),chosenDeliveryTime);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_CUSTOMER_ID),currentUser.getUid());
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_TOTAL_COST),computeTotalCost(false));
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DELIVERY_ADDRESS), deliveryAddress);
        updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DELIVERY_ADDRESS_NOTES), addressNotes);
        
        for (Dish d : selectedDishes) {
            updateMap.put(EAHCONST.generatePath(restOrderPath, EAHCONST.REST_ORDER_DISHES_SUBTREE, String.valueOf(d.getDishID())), d.getQuantity());
        }
        
        // now from point of view of user
        String custOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                currentUser.getUid(),
                orderId);
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_STATUS), orderStatus);
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_RESTAURATEUR_ID), currentRestaurant.getRestaurantID());
        
        
        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            DatabaseReference dbRef1 = dbRef.child(EAHCONST.ORDERS_REST_SUBTREE).child(currentRestaurant.getRestaurantID()).child(orderId);
            GeoFire geoFire = new GeoFire(dbRef1);
            geoFire.setLocation(EAHCONST.CUST_ORDER_DELIVERY_POS, new GeoLocation(address.getLatitude(), address.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        Log.d("Location GeoFire", "There was an error saving the location to GeoFire: " + error);
                    } else {
                        Log.d("Location GeoFire", "Location saved on server successfully!");
                    }
                }
            });
            Toast.makeText(CompleteOrderActivity.this, R.string.order_completed, Toast.LENGTH_LONG).show();
            
            Intent mainActIntent = new Intent(this, MainActivity.class);
            mainActIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainActIntent);
            finish();
        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(CompleteOrderActivity.this, R.string.alert_error_ordering);
        });
    
    }
    
    public boolean checkValidDeliveryTime() {
        String time = etTimeDialog.getText().toString();
        
        String[] splits = time.split(":");
        
        if (splits.length != 2)
            return false;
        
        int timeHour = Integer.parseInt(splits[0]);
        int timeMinutes = Integer.parseInt(splits[1]);
    
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int currentMinutes = calendar.get(Calendar.MINUTE);
        
        //TODO and another date??
        int chosenTimeMinutes = timeHour*60 + timeMinutes;
        int currentTimeMinutes = currentHour*60 + currentMinutes;
        
        if (chosenTimeMinutes - currentTimeMinutes < totalMinimumTime)
            return false;
        return true;
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(possiblePosition != null)
            possiblePosition.dismiss();
        
        outState.putSerializable(DISHES_KEY, (Serializable) selectedDishes);
        outState.putSerializable(RESTAURANT_KEY, currentRestaurant);
        outState.putSerializable(POSITION_KEY, multiChoiceItems);
        outState.putSerializable(ADDRESSES_KEY, (Serializable) addressList);
        outState.putInt(CHOICE_KEY,choice);
        outState.putBoolean(POSITION_DIALOG_KEY, positionDialogOpen);
        outState.putBoolean(DELIVERY_TIME_DIALOG_KEY, timeDialogOpen);
        
        if (address != null)
            outState.putParcelable(CHOSEN_ADDRESS_KEY, address);
        
        if (totalMinimumTime != -1)
            outState.putInt(TOTAL_MIN_TIME_KEY, totalMinimumTime);
        
        if (deliveryTimeMinutes != -1)
            outState.putInt(COMPUTED_DELIVERY_TIME_KEY, deliveryTimeMinutes);
        
        if (!etDeliveryAddress.getText().toString().isEmpty()) {
            outState.putString(ADDRESS_KEY, etDeliveryAddress.getText().toString());
        }
        
        if (!etAddressNotes.getText().toString().isEmpty()) {
            outState.putString(ADDRESS_NOTES_KEY, etAddressNotes.getText().toString());
        }
    
        if (etTimeDialog != null && !etTimeDialog.getText().toString().isEmpty()) {
            outState.putString(TIME_KEY, etTimeDialog.getText().toString());
        }
    }


    public class GeocoderHandler extends Handler {
        String locationAddress = new String();

        @Override
        public void handleMessage(Message message) {
            setActivityLoading(false);

            switch (message.what) {
                case 0:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    etDeliveryAddress.setText("");
                    Utility.showAlertToUser(CompleteOrderActivity.this, R.string.address_not_found );
                    etDeliveryAddress.setHint(R.string.address_not_found);
                    checkDistanceAndConfirmOrder();
                    break;
                case 1:
                    bundle = message.getData();
                    addressList = (List<Address>) bundle.getSerializable("address");
                    Log.d("accountInfo latlong", addressList.get(0).getLatitude() + " " + addressList.get(0).getLongitude());
                    address = addressList.get(0);
                    etDeliveryAddress.setText("" + address.getThoroughfare()+" "+ address.getSubThoroughfare()+ ", " + address.getLocality() );
                    checkDistanceAndConfirmOrder();
                    break;
                case 2:
                    bundle = message.getData();
                    addressList = (List<Address>) bundle.getSerializable("address");
                    multiChoiceItems = new String[addressList.size()];
                    for (int i = 0; i < addressList.size(); ++i) {
                        multiChoiceItems[i] = "" + addressList.get(i).getThoroughfare();
                        multiChoiceItems[i] = multiChoiceItems[i] + " " + addressList.get(i).getSubThoroughfare();
                        multiChoiceItems[i] = multiChoiceItems[i] + ", " + addressList.get(i).getLocality();

                    }
                    showPositionDialog(multiChoiceItems, 0);
                    break;
                default:
                    locationAddress = null;
                    break;
            }


        }
    }

    private void showPositionDialog(String[] multiChoiceItems, int checkedItems) {

        positionDialogOpen = true;

        possiblePosition = new AlertDialog.Builder(this)
                .setTitle("Select Your Address")
                .setSingleChoiceItems(multiChoiceItems, checkedItems, (dialog, which) -> choice = which)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    dialog.dismiss();
                    etDeliveryAddress.setText(multiChoiceItems[choice]);
                    Log.d("accountInfo latlong", addressList.get(choice).getLatitude() + " " + addressList.get(choice).getLongitude());
                    address = addressList.get(choice);
                    checkDistanceAndConfirmOrder();
                    positionDialogOpen = false;
                })
                .setNegativeButton("Back", (dialog, which) -> {
                    dialog.dismiss();
                    etDeliveryAddress.setText("");
                    checkDistanceAndConfirmOrder();
                    positionDialogOpen = false;
                }).create();


        possiblePosition.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(possiblePosition != null) {
            possiblePosition.dismiss();
        }
        
        if (deliveryTimeDialog != null) {
            deliveryTimeDialog.dismiss();
        }
    }
    
    private void openDeliveryTimeDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.order_delivery_time_dialog, null);
        
        etTimeDialog = alertLayout.findViewById(R.id.et_time);
        
        //TODO: convert messages in hours + minutes
    
        etTimeDialog.setFocusable(false);
        etTimeDialog.setClickable(true);
        etTimeDialog.setOnClickListener(v -> showTimePickerDialog());
        
        TextView tvDialogMessage = alertLayout.findViewById(R.id.tv_dialog_message);
        
        String message = getString(R.string.delivery_time_message);
        
        int orderTime = currentRestaurant.getAvgOrderTime();
        
        totalMinimumTime = orderTime + deliveryTimeMinutes;
        
        message = message + " " + totalMinimumTime + " minutes";
        
        tvDialogMessage.setText(message);
    
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.delivery_time_title)
                .setView(alertLayout)
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> {
            deliveryTimeDialog = null;
            timeDialogOpen = false;
            dialog.dismiss();
        }).setPositiveButton(R.string.ok, (dialog, which) -> {
            timeDialogOpen = false;
            if (etTimeDialog.getText().toString().isEmpty()) {
                Utility.showAlertToUser(CompleteOrderActivity.this, R.string.alert_order_no_time);
                return;
            }
            
            chosenDeliveryTime = etTimeDialog.getText().toString();
            
            if (!checkValidDeliveryTime()) {
                String alert = getString(R.string.alert_order_time_not_valid);
                alert = alert + " " + totalMinimumTime + " minutes";
                Utility.showAlertToUser(CompleteOrderActivity.this, alert);
                return;
            }
            actionConfirmOrder();
            deliveryTimeDialog = null;
        });
        
        deliveryTimeDialog = alertBuilder.create();
        
        timeDialogOpen = true;
        deliveryTimeDialog.show();
    }
    
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment(etTimeDialog);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
}
