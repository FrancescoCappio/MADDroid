package it.polito.maddroid.lab3.rider;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import it.polito.maddroid.lab3.common.Customer;
import it.polito.maddroid.lab3.common.CustomerDetailActivity;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.RoutingUtility;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shuhart.stepview.StepView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class OrderDeliveryActivity extends AppCompatActivity {

    private static final String TAG = "OrderDeliveryActivity";
    public static final String ORDER_DELIVERY_KEY = "ORDER_DELIVERY_KEY";

    private RiderOrderDelivery currentOrder;
    private LatLng lastLocation;
    private LatLng restaurantLocation;
    private LatLng customerLocation;

    private List<List<HashMap<String, String>>> riderToRestaurantRoutes;
    private String riderToRestaurantDistance;
    private String riderToRestaurantDuration;
    private List<List<HashMap<String, String>>> restaurantToCustomerRoutes;
    private String restaurantToCustomerDistance;
    private String restaurantToCustomerDuration;
    private float kmRestToCust;

    private TextView tvDeliveryTime;
    private TextView tvCostDelivery;
    private TextView tvTotalCost;
    private TextView tvRestaurantAddress;
    private TextView tvDeliveryAddress;
    private ProgressBar pbLoading;
    private TextView tvCustomerName;
    private TextView tvRestaurantName;
    private TextView tvRiderToRestaurantDistKM;
    private TextView tvRiderToRestaurantDistTime;
    private TextView tvRestaurantToCustomerDistKM;
    private TextView tvRestaurantToCustomerDistTime;
    private TextView tvDeliveryAddressNotes;
    private TextView tvDeliveryDate;
    private EditText etControlString;

    private Button btGetFood;
    private Button btDeliverFood;
    private Button btDirectionToRestaurant;
    private Button btDirectionToCustomer;
    private Button btCustomerInfo;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private int waitingCount;
    private Customer currentCustomer;

    private StepView stepView;
    private int currentStep = 0;
    private boolean viewLoaded = false;

    private AlertDialog controlConfirm;

    //currentStep = 0 Confirmed
    //currentStep = 1 Pickup Products
    //currentStep = 2 Delivery
    //currentStep = 3 Completed
    private List<String> seekBarStatus = Arrays.asList("Confirmed", "Pickup products", "In delivery", "Completed");
    private boolean controlDialogOpen;
    private String open_control= "open_control";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_delivery);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        
        if (intent.getSerializableExtra(ORDER_DELIVERY_KEY) == null) {
            Log.e(TAG, "Cannot show null order delivery");
            finish();
            return;
        }
        
        currentOrder = (RiderOrderDelivery) intent.getSerializableExtra(ORDER_DELIVERY_KEY);


        RiderLocationService service = RiderLocationService.getInstance();
        //to get last location from MainActivity
        if (service != null ){
            Location loc = service.getLastLocation();
            
            if (loc != null)
                lastLocation = new LatLng(loc.getLatitude(),loc.getLongitude());
            if (lastLocation == null)
                Utility.showAlertToUser(this,R.string.not_find_location);
        }

        getReferencesToViews();
        
        setDataToView();
        
        setOnClickListeners();
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.order_delivery);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        getCustomerInfo();
        
        setupButtonsEnable();

        getRestaurantLocations();

        stepView.getState().steps(seekBarStatus).commit();
        stepView.go(0,false);

        View rootView = getWindow().getDecorView().getRootView();
        ViewTreeObserver observer = rootView .getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Do what you need with yourView here...
                setSteps();
                viewLoaded = true;
            }
        });

    }

    private void setSteps() {
        if (currentStep != 3)
            stepView.go(currentStep, true);
        else {
            stepView.go(currentStep, true);
            stepView.done(true);
        }
    }

    private void setupButtonsEnable() {
        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.CONFIRMED) {
            btDeliverFood.setEnabled(false);
            btGetFood.setEnabled(true);
            btDeliverFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            btGetFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_green_accept));
            currentStep = 1;
        } else if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.ONGOING) {
            btGetFood.setEnabled(false);
            btDeliverFood.setEnabled(true);
            btGetFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            btDeliverFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_green_accept));
            currentStep = 2;
        } else {
            btGetFood.setEnabled(false);
            btDeliverFood.setEnabled(false);
            btGetFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            btDeliverFood.setBackgroundColor(ContextCompat.getColor(this, R.color.eah_grey));
            currentStep = 3;
        }

        if (viewLoaded)
            setSteps();
    }
    
    private void getReferencesToViews(){
        pbLoading = findViewById(R.id.pb_loading);
        
        tvDeliveryTime = findViewById(R.id.tv_time);
        tvCostDelivery = findViewById(R.id.tv_cost_delivery);
        tvTotalCost = findViewById(R.id.tv_total_cost);
        tvRestaurantAddress = findViewById(R.id.tv_restaurant_address);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvDeliveryAddressNotes = findViewById(R.id.tv_delivery_address_notes);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvRiderToRestaurantDistKM = findViewById(R.id.tv_restaurant_distance);
        tvRiderToRestaurantDistTime = findViewById(R.id.tv_restaurant_duration);
        tvRestaurantToCustomerDistKM = findViewById(R.id.tv_delivery_address_distance);
        tvRestaurantToCustomerDistTime = findViewById(R.id.tv_delivery_address_duration);

        btDirectionToCustomer = findViewById(R.id.bt_direction_to_customer);
        btDirectionToRestaurant = findViewById(R.id.bt_direction_to_restaurant);
        btCustomerInfo = findViewById(R.id.bt_customer_info);
        btGetFood = findViewById(R.id.bt_get_food);
        btDeliverFood = findViewById(R.id.bt_deliver_food);
        tvDeliveryDate = findViewById(R.id.tv_date);

        stepView = findViewById(R.id.step_view);
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
    
    private void setDataToView() {
        
        tvDeliveryTime.setText(currentOrder.getDeliveryTime());
        tvTotalCost.setText(currentOrder.getTotalCost());
        tvCostDelivery.setText(String.format(Locale.US,"%.02f", currentOrder.getDeliveryCost()) + " €");
        tvRestaurantAddress.setText(currentOrder.getRestaurantAddress());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        tvRestaurantName.setText(currentOrder.getRestaurantName());
        tvDeliveryAddressNotes.setText(currentOrder.getDeliveryAddressNotes());
        tvDeliveryDate.setText(currentOrder.getDeliveryDate());
    }
    
    private void setOnClickListeners() {
        
        btDeliverFood.setOnClickListener(v -> deliverFoodAction());
        
        btGetFood.setOnClickListener(v -> getFoodAction());

        btDirectionToRestaurant.setOnClickListener(v -> getDirectionToRestaurant());

        btDirectionToCustomer.setOnClickListener(v -> getDirectionToCustomer());
        
        btCustomerInfo.setOnClickListener(v -> {
            if (currentCustomer == null) {
                Utility.showAlertToUser(this, R.string.not_ready_alert);
                return;
            }
            Intent customerInfoIntent = new Intent(this, CustomerDetailActivity.class);
            customerInfoIntent.putExtra(CustomerDetailActivity.CUSTOMER_KEY, currentCustomer);
            startActivity(customerInfoIntent);
        });
    }

    private void getDirectionToRestaurant() {
        if (riderToRestaurantRoutes == null) {
            Utility.showAlertToUser(this, R.string.route_not_ready);
            return;
        }

        Intent intent = new Intent(getApplicationContext(), RoutingActivity.class);
        intent.putExtra(RoutingActivity.ORIGIN_LOCATION_KEY, lastLocation);
        intent.putExtra(RoutingActivity.DESTINATION_LOCATION_KEY, restaurantLocation);
        intent.putExtra(RoutingActivity.ROUTE_KEY, (Serializable) riderToRestaurantRoutes);

        startActivity(intent);
    }

    private void getDirectionToCustomer() {
        if (restaurantToCustomerRoutes == null) {
            Utility.showAlertToUser(this, R.string.route_not_ready);
            return;
        }
        
        Intent intent = new Intent(getApplicationContext(), RoutingActivity.class);
        intent.putExtra(RoutingActivity.ORIGIN_LOCATION_KEY, restaurantLocation);
        intent.putExtra(RoutingActivity.DESTINATION_LOCATION_KEY, customerLocation);
        intent.putExtra(RoutingActivity.ROUTE_KEY, (Serializable) restaurantToCustomerRoutes);
        startActivity(intent);
    }

    private void getFoodAction() {
        setActivityLoading(true);

        Map<String,Object> updateMap = new HashMap<>();

        //update Rider SubTree
        EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.ONGOING;
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_RIDER_SUBTREE,
                currentUser.getUid(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);


        //update customer subtree
        String custOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_CUST_SUBTREE,
                currentOrder.getCustomerId(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_STATUS), orderStatus);

        //update Restaurant SubTree
        orderStatus = EAHCONST.OrderStatus.COMPLETED;
        String restaurantOrderPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE,
                currentOrder.getRestaurantId(),
                currentOrder.getOrderId());
        updateMap.put(EAHCONST.generatePath(restaurantOrderPath, EAHCONST.REST_ORDER_STATUS), orderStatus);


        // perform the update
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            setActivityLoading(false);
            currentOrder.setOrderStatus(EAHCONST.OrderStatus.ONGOING);
            setupButtonsEnable();
            Utility.showAlertToUser(this, R.string.get_food_note);

        }).addOnFailureListener(e -> {
            setActivityLoading(false);
            Utility.showAlertToUser(this, R.string.alert_error_get_food);
        });
        
    }
    
    private void deliverFoodAction() {

        //dialog

        controlDialogOpen = true;
        LayoutInflater inflater = getLayoutInflater();
        View controlLayout = inflater.inflate(R.layout.confirm_delivery, null);
        etControlString = controlLayout.findViewById(R.id.et_confirm);

        AlertDialog.Builder alertControl = new AlertDialog.Builder(this);
        alertControl.setTitle(R.string.confirm_delivery);
        // this is set the view from XML inside AlertDialog
        alertControl.setView(controlLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alertControl.setCancelable(false);
        alertControl.setNegativeButton(R.string.action_cancel, (dialog, which) -> {
            controlDialogOpen = false;
            controlConfirm = null;
            dialog.dismiss();
        });

        alertControl.setPositiveButton(R.string.ok, (dialog, which) -> {
            controlDialogOpen = true;
            String s = etControlString.getText().toString();
            if (s.equals(currentOrder.getStringControl()))
            {
                setActivityLoading(true);

                Map<String,Object> updateMap = new HashMap<>();

                EAHCONST.OrderStatus orderStatus = EAHCONST.OrderStatus.COMPLETED;
                String riderOrderPath = EAHCONST.generatePath(
                        EAHCONST.ORDERS_RIDER_SUBTREE,
                        currentUser.getUid(),
                        currentOrder.getOrderId());
                updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_ORDER_STATUS), orderStatus);
                updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_KM_REST_CUST), kmRestToCust);
                updateMap.put(EAHCONST.generatePath(riderOrderPath, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());


                //update customer subtree
                String custOrderPath = EAHCONST.generatePath(
                        EAHCONST.ORDERS_CUST_SUBTREE,
                        currentOrder.getCustomerId(),
                        currentOrder.getOrderId());
                updateMap.put(EAHCONST.generatePath(custOrderPath, EAHCONST.CUST_ORDER_STATUS), orderStatus);

                addRiderIncome();
                // perform the update
                dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
                    setActivityLoading(false);
                    currentOrder.setOrderStatus(EAHCONST.OrderStatus.COMPLETED);
                    setupButtonsEnable();
                    Utility.showAlertToUser(this, R.string.deliver_food_note);
                }).addOnFailureListener(e -> {
                    setActivityLoading(false);
                    Utility.showAlertToUser(this, R.string.alert_error_deliver_food);
                });

            } else{
                Utility.showAlertToUser(this, R.string.wrong_code);
                controlDialogOpen = false;
                controlConfirm = null;
                dialog.dismiss();
                return;
            }

            controlDialogOpen = false;
            controlConfirm = null;
            dialog.dismiss();

        });

        controlConfirm = alertControl.create();
        controlConfirm.show();

        //controlDialogOpen = false;


    }

    private void addRiderIncome() {
        String year = currentOrder.getDeliveryDate().split("-")[2];
        String month = currentOrder.getDeliveryDate().split("-")[1];
        String day = currentOrder.getDeliveryDate().split("-")[0];

        // TODO if is null aggiungi, se no sommi sul giorno tot, sommi sul mese tot, sommi sull'anno tot

        String riderIncomePathDay = EAHCONST.generatePath(
                EAHCONST.RIDERS_INCOME_SUB_TREE,
                currentUser.getUid(),
                year, month, day);

        String riderIncomePathMonth = EAHCONST.generatePath(
                EAHCONST.RIDERS_INCOME_SUB_TREE,
                currentUser.getUid(),
                year, month);

        String riderIncomePath = EAHCONST.generatePath(
                EAHCONST.RIDERS_INCOME_SUB_TREE,
                currentUser.getUid(),
                year);


        dbRef.child(EAHCONST.RIDERS_INCOME_SUB_TREE).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setActivityLoading(true);
                Map<String, Object> updateMap = new HashMap<>();

                if (dataSnapshot.getValue() == null) {
                    updateMap.put(EAHCONST.generatePath(riderIncomePathDay, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                    updateMap.put(EAHCONST.generatePath(riderIncomePathMonth, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                    updateMap.put(EAHCONST.generatePath(riderIncomePath, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                } else {
                    if (dataSnapshot.hasChild(year)) {
                        float prevIncomeYear = dataSnapshot.child(year).child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue();
                        if (dataSnapshot.child(year).hasChild(month)) {
                            float prevIncomeMonth = dataSnapshot.child(year).child(month).child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue();
                            if (dataSnapshot.child(year).child(month).hasChild(day)) {
                                float prevIncomeDay = dataSnapshot.child(year).child(month).child(day).child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue();
                                updateMap.put(EAHCONST.generatePath(riderIncomePathDay, EAHCONST.RIDER_INCOME), prevIncomeDay + currentOrder.getDeliveryCost());
                                updateMap.put(EAHCONST.generatePath(riderIncomePathMonth, EAHCONST.RIDER_INCOME), prevIncomeDay + prevIncomeMonth + currentOrder.getDeliveryCost());
                                updateMap.put(EAHCONST.generatePath(riderIncomePath, EAHCONST.RIDER_INCOME), prevIncomeDay + prevIncomeYear + prevIncomeMonth + currentOrder.getDeliveryCost());
                            } else {
                                updateMap.put(EAHCONST.generatePath(riderIncomePathDay, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                                updateMap.put(EAHCONST.generatePath(riderIncomePathMonth, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost() + prevIncomeMonth);
                                updateMap.put(EAHCONST.generatePath(riderIncomePath, EAHCONST.RIDER_INCOME), prevIncomeYear + prevIncomeMonth);

                            }
                        } else {
                            updateMap.put(EAHCONST.generatePath(riderIncomePathDay, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                            updateMap.put(EAHCONST.generatePath(riderIncomePathMonth, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                            updateMap.put(EAHCONST.generatePath(riderIncomePath, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost() + prevIncomeYear);
                        }
                    } else {

                        updateMap.put(EAHCONST.generatePath(riderIncomePathDay, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                        updateMap.put(EAHCONST.generatePath(riderIncomePathMonth, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                        updateMap.put(EAHCONST.generatePath(riderIncomePath, EAHCONST.RIDER_INCOME), currentOrder.getDeliveryCost());
                    }

                }


                dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
                    setActivityLoading(false);
                }).addOnFailureListener(e -> {
                    setActivityLoading(false);
                    Log.e(TAG, "ERROR: "+e.getMessage());
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "ERROR: "+databaseError.getMessage());
            }
        });


    }

    private synchronized void setActivityLoading(boolean loading) {
        // this method is necessary to show the user when the activity is doing a network operation
        // as downloading data or uploading data
        // how to use: call with loading = true to notify that a new transmission has been started
        // call with loading = false to notify end of transmission
        
        
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
    
    private void getCustomerInfo() {
        
        setActivityLoading(true);
        
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(currentOrder.getCustomerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Utility.showAlertToUser(OrderDeliveryActivity.this, R.string.alert_error_downloading_info);
                    tvCustomerName.setText(R.string.alert_error_downloading_info);
                    setActivityLoading(false);
                    return;
                }
                
                String name = dataSnapshot.child(EAHCONST.CUSTOMER_NAME).getValue(String.class);
                String address = dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS).getValue(String.class);
                String addressNotes = dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS_NOTES).getValue(String.class);
                String phoneNumber = dataSnapshot.child(EAHCONST.CUSTOMER_PHONE).getValue(String.class);
                String email = dataSnapshot.child(EAHCONST.CUSTOMER_EMAIL).getValue(String.class);
                String description = dataSnapshot.child(EAHCONST.CUSTOMER_DESCRIPTION).getValue(String.class);

                
                tvCustomerName.setText(name);
                
                currentCustomer = new Customer(currentOrder.getCustomerId(), name, address, addressNotes, description, phoneNumber, email);
                
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
    }

    private void getRestaurantLocations() {

        String restaurantUID = currentOrder.getRestaurantId();

        String restaurantPath = EAHCONST.generatePath(
                EAHCONST.RESTAURANTS_SUB_TREE,restaurantUID);

        DatabaseReference dbRef1 = dbRef.child(restaurantPath);
        GeoFire geoFireRestaurant = new GeoFire(dbRef1);

        geoFireRestaurant.getLocation(EAHCONST.RESTAURANT_POSITION, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    restaurantLocation = new LatLng(location.latitude, location.longitude);
                    getCustomerLocation();
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }

    private void getCustomerLocation() {

        String customerPath = EAHCONST.generatePath(
                EAHCONST.ORDERS_REST_SUBTREE, currentOrder.getRestaurantId(), currentOrder.getOrderId());

        dbRef.child(customerPath).child(EAHCONST.REST_ORDER_CONTROL_STRING).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.getValue() != null)
               {
                   currentOrder.setControlString(dataSnapshot.getValue(String.class));
               }
           }
           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });

        DatabaseReference dbRef2 = dbRef.child(customerPath);
        GeoFire geoFireCustomer = new GeoFire(dbRef2);

        geoFireCustomer.getLocation(EAHCONST.CUSTOMER_POSITION, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    customerLocation = new LatLng(location.latitude, location.longitude);
                    getRoutes();
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }

    private void getRoutes() {
        if (lastLocation != null && restaurantLocation != null && customerLocation != null) {
            // get Rider to Restaurant Routes
            
            new RoutingUtility(this, lastLocation, restaurantLocation, new RoutingUtility.GetRouteCaller() {
                @Override
                public void routeCallback(List<List<HashMap<String, String>>> route, String[] distances, int minutes) {
                    riderToRestaurantRoutes = route;
                    riderToRestaurantDistance = distances[0];
                    tvRiderToRestaurantDistKM.setText(riderToRestaurantDistance);
                    riderToRestaurantDuration = distances[1];
                    tvRiderToRestaurantDistTime.setText(riderToRestaurantDuration);
                }
    
                @Override
                public void routeErrorCallback(Exception e) {
                    Log.e(TAG, "Exception in routing: " + e.getMessage());
                }
            });
            
            new RoutingUtility(this, restaurantLocation, customerLocation, new RoutingUtility.GetRouteCaller() {
                @Override
                public void routeCallback(List<List<HashMap<String, String>>> route, String[] distances, int minutes) {
                    restaurantToCustomerRoutes = route;
                    restaurantToCustomerDistance = distances[0];
                    String[] splits = distances[0].split(" ");
                    kmRestToCust = Float.parseFloat(splits[0]);
                    tvRestaurantToCustomerDistKM.setText(restaurantToCustomerDistance);
                    restaurantToCustomerDuration = distances[1];
                    tvRestaurantToCustomerDistTime.setText(restaurantToCustomerDuration);
                }
    
                @Override
                public void routeErrorCallback(Exception e) {
                    Log.e(TAG, "Exception in routing: " + e.getMessage());
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(open_control, controlDialogOpen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        controlDialogOpen = savedInstanceState.getBoolean(open_control);
        if(controlDialogOpen)
            deliverFoodAction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(controlConfirm != null)
        {
            controlConfirm.dismiss();
        }
    }
}
