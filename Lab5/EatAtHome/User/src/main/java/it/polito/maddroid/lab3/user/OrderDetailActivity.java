package it.polito.maddroid.lab3.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Order;
import it.polito.maddroid.lab3.common.RatingActivity;
import it.polito.maddroid.lab3.common.RoutingUtility;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shuhart.stepview.StepView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class OrderDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "OrderDetailActivity";
    
    public static final String ORDER_KEY = "ORDER_KEY";
    
    private static final int RATING_RIDER_CODE = 478;
    private static final int RATING_RESTAURANT_CODE = 243;
    
    
    private Order currentOrder;
    private int waitingCount;
    private DatabaseReference dbRef;
    
    private MenuListAdapter adapter;

    private LatLng restaurantLocation;
    private LatLng customerLocation;

    private List<List<HashMap<String, String>>> restaurantToCustomerRoutes;
    
    //views
    private TextView tvOrderDate;
    private TextView tvDeliveryTime;
    private TextView tvTotalCost;
    private TextView tvDeliveryCost;
    private TextView tvRestaurantName;
    private TextView tvRiderName;
    private TextView tvDeliveryAddress;
    private TextView tvStringControl;
    private RecyclerView rvDishes;
    private Button btRateRider;
    private Button btRateRestaurant;
    private Button btTrackRider;
    
    private List<Dish> dishList;

    private StepView stepView;
    private boolean viewLoaded = false;

    //currentStep = 0 Confirmed
    //currentStep = 1 Pickup Products
    //currentStep = 2 Delivery
    //currentStep = 3 Completed
    
    private List<String> seekBarStatus = Arrays.asList("Confirmed","Waiting for rider","On the way","Completed");
    private List<String> seekBarStatusWaiting = Arrays.asList("Waiting confirm","Waiting rider","On the way","Completed");
    private List<String> seekBarStatusDeclined = Arrays.asList("Declined","Waiting rider","On the way","Completed");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        
        Intent i = getIntent();
        
        Serializable orderExtra = i.getSerializableExtra(ORDER_KEY);
        
        if (orderExtra != null) {
            currentOrder = (Order) orderExtra;
        } else {
            Log.e(TAG, "Cannot open detail for a null order");
            finish();
        }
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        getReferencesToViews();
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.order_detail);
        }
        
        tvOrderDate.setText(currentOrder.getDate());
        tvDeliveryTime.setText(currentOrder.getDeliveryTime());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        tvStringControl.setText(currentOrder.getDeliveryStringControl());
        tvDeliveryCost.setText(String.format(Locale.US,"%.02f", EAHCONST.DELIVERY_COST) + " €");
        tvTotalCost.setText(currentOrder.getTotalCost());
        tvRestaurantName.setText(currentOrder.getRestaurantName());
        
        //recycler view
        rvDishes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        
        adapter = new MenuListAdapter(new DishDiffUtilCallBack(), dish -> Log.d(TAG, "Nothing to do"), currentOrder.getRestaurantId(), MenuListAdapter.MODE_ORDER_DISH_LIST);
        
        rvDishes.setAdapter(adapter);
        
        getDishesInfo();
        
        updateUIForOrderStatus();
        
        setupClickListeners();

        stepView.getState().steps(seekBarStatusWaiting).commit();
        
        View rootView = getWindow().getDecorView().getRootView();
        ViewTreeObserver observer = rootView .getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                viewLoaded = true;
                updateUIForOrderStatus();
            }
        });
    
        getUpdatedOrderStatus();
        
        getRiderName(currentOrder.getRiderId());
    }

    private int getCurrentStep(EAHCONST.OrderStatus orderStatus){
        switch (orderStatus) {
            case PENDING:
                return 0;
            case CONFIRMED:
            case WAITING_RIDER:
                return 1;
            case ONGOING:
                return 2;
            case COMPLETED:
                return 3;
            case DECLINED:
                return -1;
        }
        return 0;
    }
    
    private void setSteps(int currentStep) {
        if (currentStep == -1) {
            stepView.done(true);
        }
        if (currentStep != 3)
            stepView.go(currentStep, true);
        else {
            stepView.go(currentStep, true);
            stepView.done(true);
        }
    }
    
    private void getReferencesToViews() {
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvDeliveryTime = findViewById(R.id.tv_time);
        tvRiderName = findViewById(R.id.tv_rider);
        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvTotalCost = findViewById(R.id.tv_payment_total);
        tvDeliveryCost = findViewById(R.id.tv_delivery_cost);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvStringControl = findViewById(R.id.tv_control_string);
        rvDishes = findViewById(R.id.rv_order_dishes);
        btRateRestaurant = findViewById(R.id.bt_rate_restaurant);
        btRateRider = findViewById(R.id.bt_rate_rider);
        btTrackRider = findViewById(R.id.bt_track_rider);

        stepView = findViewById(R.id.step_view);
    }
    
    private void setupClickListeners() {
        btRateRider.setOnClickListener(v -> {
            Intent rateRiderIntent = new Intent(OrderDetailActivity.this, RatingActivity.class);
            rateRiderIntent.putExtra(RatingActivity.RATING_MODE_KEY, RatingActivity.RATING_MODE_RIDER);
            rateRiderIntent.putExtra(RatingActivity.RATED_UID_KEY, currentOrder.getRiderId());
            rateRiderIntent.putExtra(RatingActivity.RATER_TYPE_KEY, RatingActivity.RATER_TYPE_USER);
            rateRiderIntent.putExtra(RatingActivity.RATER_UID_KEY, currentOrder.getCustomerId());
            rateRiderIntent.putExtra(RatingActivity.RATING_ORDER_KEY, currentOrder.getOrderId());
            startActivityForResult(rateRiderIntent, RATING_RIDER_CODE);
        });
        
        btRateRestaurant.setOnClickListener(v -> {
            Intent rateRiderIntent = new Intent(OrderDetailActivity.this, RatingActivity.class);
            rateRiderIntent.putExtra(RatingActivity.RATING_MODE_KEY, RatingActivity.RATING_MODE_RESTAURANT);
            rateRiderIntent.putExtra(RatingActivity.RATED_UID_KEY, currentOrder.getRestaurantId());
            rateRiderIntent.putExtra(RatingActivity.RATER_TYPE_KEY, RatingActivity.RATER_TYPE_USER);
            rateRiderIntent.putExtra(RatingActivity.RATER_UID_KEY, currentOrder.getCustomerId());
            rateRiderIntent.putExtra(RatingActivity.RATING_ORDER_KEY, currentOrder.getOrderId());
            startActivityForResult(rateRiderIntent, RATING_RESTAURANT_CODE);
        });

        btTrackRider.setOnClickListener(v -> getDirectionToCustomer());
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
            if (waitingCount > 0) {
                waitingCount--;
                if (waitingCount == 0)
                    pbLoading.setVisibility(View.INVISIBLE);
            }
        }
    }
    
    private void getRiderName(String riderId) {
        
        if (riderId == null || riderId.isEmpty()) {
            tvRiderName.setText(R.string.rider_not_assigned);
            return;
        }
        
        setActivityLoading(true);
    
        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(riderId).child(EAHCONST.RIDER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    tvRiderName.setText((CharSequence) dataSnapshot.getValue());
                } else {
                    Utility.showAlertToUser(OrderDetailActivity.this, R.string.alert_error_downloading_info);
                    Log.e(TAG, "Cannot obtain rider's name");
                }
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
    }
    
    private void getDishesInfo() {
    
        setActivityLoading(true);
        
        Query queryRef = dbRef.child(EAHCONST.DISHES_SUB_TREE).child(currentOrder.getRestaurantId()).orderByChild(EAHCONST.DISH_NAME);
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        
                if (!dataSnapshot.hasChildren()) {
                    Utility.showAlertToUser(OrderDetailActivity.this, R.string.alert_error_downloading_info);
                    return;
                }
                
                dishList = new ArrayList<>();
                
                Map<String, Integer> currentOrderDishes = currentOrder.getDishesMap();
                
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String dishId = ds.getKey();
                    
                    if (dishId == null)
                        continue;
                    
                    if (currentOrderDishes.containsKey(dishId)) {
                        String dishName = (String) ds.child(EAHCONST.DISH_NAME).getValue();
                        String description = (String) ds.child(EAHCONST.DISH_DESCRIPTION).getValue();
    
                        float dishPrice = Float.parseFloat(ds.child(EAHCONST.DISH_PRICE).getValue().toString());
                        
                        Dish dish = new Dish(Integer.valueOf(dishId), dishName, dishPrice, description);
                        dish.setQuantity(currentOrderDishes.get(dishId));
                        
                        dishList.add(dish);
                    }
                }
                
                adapter.submitList(dishList);
                setActivityLoading(false);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
    
        switch (item.getItemId()) {
        
            case android.R.id.home:
                //emulate back pressed
                onBackPressed();
                return true;
        }
        return false;
    }

    private void getUpdatedOrderStatus() {
        setActivityLoading(true);
        dbRef.child(EAHCONST.ORDERS_CUST_SUBTREE).child(currentOrder.getCustomerId()).child(currentOrder.getOrderId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()) {
                    Utility.showAlertToUser(OrderDetailActivity.this, R.string.alert_error_downloading_info);
                    return;
                }
                EAHCONST.OrderStatus orderStatus = dataSnapshot.child(EAHCONST.CUST_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);
                
                if (dataSnapshot.child(EAHCONST.CUST_ORDER_RIDER_ID).getValue() != null)
                    currentOrder.setRiderId(dataSnapshot.child(EAHCONST.CUST_ORDER_RIDER_ID).getValue(String.class));

                currentOrder.setOrderStatus(orderStatus);
                
                if (currentOrder.getRiderId() != null && !currentOrder.getRiderId().isEmpty())
                    getRiderName(currentOrder.getRiderId());
                
                updateUIForOrderStatus();
                
                setActivityLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                setActivityLoading(false);
            }
        });

    }
    
    private void updateUIForOrderStatus() {
        
        if (currentOrder.getOrderStatus() != EAHCONST.OrderStatus.PENDING) {
            stepView.getState().steps(seekBarStatus).commit();
        }
    
        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.COMPLETED) {
            btRateRider.setVisibility(View.VISIBLE);
            btRateRestaurant.setVisibility(View.VISIBLE);
            
            btRateRider.setEnabled(!currentOrder.getRiderRated());
            btRateRestaurant.setEnabled(!currentOrder.getRestaurantRated());
        } else {
            btRateRider.setVisibility(View.GONE);
            btRateRestaurant.setVisibility(View.GONE);
        }
        
        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.DECLINED) {
            stepView.getState().steps(seekBarStatusDeclined).commit();
            stepView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.eah_red_alert));
        }

        if (currentOrder.getOrderStatus() == EAHCONST.OrderStatus.ONGOING) {
            btTrackRider.setVisibility(View.VISIBLE);
            getRestaurantLocations();
        }
        else
            btTrackRider.setVisibility(View.GONE);
    
        if (viewLoaded)
            setSteps(getCurrentStep(currentOrder.getOrderStatus()));
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
                EAHCONST.ORDERS_REST_SUBTREE,currentOrder.getRestaurantId(),currentOrder.getOrderId());

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
        if (restaurantLocation != null && customerLocation != null) {
            // get Restaurant to customer Routes

            new RoutingUtility(this, restaurantLocation, customerLocation, new RoutingUtility.GetRouteCaller() {
                @Override
                public void routeCallback(List<List<HashMap<String, String>>> route, String[] distances, int minutes) {
                    restaurantToCustomerRoutes = route;
                }
    
                @Override
                public void routeErrorCallback(Exception e) {
                    Log.e(TAG, "Exception routing: " + e.getMessage());
                }
            });
        }
    }

    private void getDirectionToCustomer() {
        if (restaurantToCustomerRoutes == null) {
            Utility.showAlertToUser(this, R.string.route_not_ready);
            return;
        }

        Intent intent = new Intent(getApplicationContext(), TrackingRiderActivity.class);
        intent.putExtra(TrackingRiderActivity.ORIGIN_LOCATION_KEY, restaurantLocation);
        intent.putExtra(TrackingRiderActivity.DESTINATION_LOCATION_KEY, customerLocation);
        intent.putExtra(TrackingRiderActivity.ROUTE_KEY, (Serializable) restaurantToCustomerRoutes);
        intent.putExtra(OrderDetailActivity.ORDER_KEY, currentOrder);
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == RATING_RIDER_CODE) {
                currentOrder.setRiderRated(true);
            } else if (requestCode == RATING_RESTAURANT_CODE) {
                currentOrder.setRestaurantRated(true);
            }
            updateUIForOrderStatus();
        }
    }
}
