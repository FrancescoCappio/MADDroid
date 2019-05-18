package it.polito.maddroid.lab3.rider;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.EAHFirebaseMessagingService;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.Utility;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "MainActivity";
    private static MainActivity instance;

    private final static int LOCATION_PERMISSION_CODE = 123;
    
    private static String STATE_SELECTED_POSITION = "state_selected_position";
    
    private SharedPreferences sharedPreferences;
    public static final String SHARED_PREFS = "EATAATHOME_RIDER_SHARED_PREFS";
    public static final String RIDER_ON_DUTY_KEY = "RIDER_ON_DUTY_KEY";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference storageReference;

    private String riderUID ;

    private LinearLayout llNavHeaderMain;
    private NavigationView navigationView;
    private TextView tvAccountEmail;
    private ImageView ivAvatar;
    private Switch onDutySwitch;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;


    private boolean riderOnDuty = false;

    private Bundle orderHistoryBundle;
    
    private int currentSelectedPosition;
    
    private Intent myServiceIntent;

    private Fragment selectedFragment;
    private CurrentDeliveriesFragment currentDeliveriesFragment;
    private OrdersFragment ordersFragment;
    
    private List<RiderOrderDelivery> allDeliveries;
    
    public static String FILE_PROVIDER_AUTHORITY = "it.polito.maddroid.eatathome.fileprovider.rider";
    
    public interface OrdersUpdateListener {
        public void manageOrdersUpdate();
    }
    
    private List<OrdersUpdateListener> ordersUpdateListeners;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        setupNavigation();
    
        getReferencesToViews();
        
        setupClickListeners();
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        myServiceIntent = new Intent(this, RiderLocationService.class);

        ordersUpdateListeners = new ArrayList<>();
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        
        if (currentUser == null) {
            // this is probably an error, the user should be logged in to see this activity
            Utility.showAlertToUser(this, R.string.login_alert);
            
            // start Login Actvity
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            loginIntent.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
            startActivity(loginIntent);
            
            // exit
            finish();
        }

        riderUID = currentUser.getUid();

        tvAccountEmail.setText(currentUser.getEmail());
    
        EAHFirebaseMessagingService.setActivityToLaunch(MainActivity.class);
    
        StorageReference riversRef = storageReference.child("avatar_" + currentUser.getUid() +".jpg");
        GlideApp.with(getApplicationContext())
                .load(riversRef)
                .into(ivAvatar);
    
        cancelAllTheNotifications();
    
        selectItem(0);
        
        downloadOrdersInfo();
        
        checkPermissions();
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void startStopService(boolean start) {
        ActionBar actionBar = getSupportActionBar();

        if (!start) {

            if (allDeliveries != null) {
                if (!getCurrentDeliveries(allDeliveries).isEmpty()) {

                    Utility.showAlertToUser(this, R.string.alert_complete_current_deliveries);

                    if (onDutySwitch != null)
                        onDutySwitch.setChecked(true);
                    return;
                }
            }
        }

        if (actionBar != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            riderOnDuty = start;
            if (riderOnDuty) {
                actionBar.setTitle(R.string.rider_on_duty);
                startLocationService();
            }
            else {
                actionBar.setTitle(R.string.rider_not_on_duty);
                stopLocationService();
            }
            editor.putBoolean(RIDER_ON_DUTY_KEY, riderOnDuty);
            editor.apply();
        }

        checkAllOrdersDownloaded();
    }

    private void startLocationService() {

        if (!isMyServiceRunning(RiderLocationService.class))
            startService(myServiceIntent);

        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(currentUser.getUid()).child(EAHCONST.RIDER_ON_DUTY)
                .setValue(EAHCONST.RiderStatus.ON_DUTY)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Correctly set rider's status on server"))
                .addOnFailureListener(e -> Log.e(TAG, "Could not set rider's status on server: " + e.getMessage()));
    }

    private void stopLocationService() {
        stopService(myServiceIntent);

        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(currentUser.getUid()).child(EAHCONST.RIDER_ON_DUTY)
                .setValue(EAHCONST.RiderStatus.NOT_ON_DUTY)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Correctly set rider's status on server"))
                .addOnFailureListener(e -> Log.e(TAG, "Could not set rider's status on server: " + e.getMessage()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.activity_main_menu, menu);

        // Get the action view used in your toggleservice item
        final MenuItem toggleservice = menu.findItem(R.id.action_toggle_service);
        final Switch actionView = (Switch) toggleservice.getActionView();

        onDutySwitch = actionView;
        riderOnDuty = sharedPreferences.getBoolean(RIDER_ON_DUTY_KEY,false);

        if (riderOnDuty)
            actionView.setChecked(true);
        onDutySwitch.setEnabled(false);

        actionView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            startStopService(isChecked);
        });
        restoreRiderStatusFromServer();
        
        return super.onCreateOptionsMenu(menu);
    }
    
    private void restoreRiderStatusFromServer() {
        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(currentUser.getUid()).child(EAHCONST.RIDER_ON_DUTY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Log.e(TAG, "Error restoring rider status from server");
                    startStopService(riderOnDuty);
                    return;
                }
                riderOnDuty = dataSnapshot.getValue(EAHCONST.RiderStatus.class) == EAHCONST.RiderStatus.ON_DUTY;
                onDutySwitch.setChecked(riderOnDuty);
                startStopService(riderOnDuty);
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error while restoring rider status");
                startStopService(riderOnDuty);
            }
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_permissions_title)
                    .setMessage(R.string.alert_permissions_needed)
                    .setOnDismissListener(dialog -> checkPermissions()).create().show();
        }
        
    }
    
    private void cancelAllTheNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserHasCompletedAccountInfo();
    }
    
    private void setupNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    
    private void getReferencesToViews() {
        
        llNavHeaderMain = navigationView.getHeaderView(0).findViewById(R.id.ll_nav_header_main);
        tvAccountEmail = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_rider_email);
        ivAvatar = navigationView.getHeaderView(0).findViewById(R.id.iv_nav_rider_avatar);
        
    }
    
    private void setupClickListeners() {
        
        llNavHeaderMain.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), AccountInfoActivity.class);
            i.putExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, false);
            startActivity(i);
        });
        
    }
    
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        
        if (id == R.id.nav_current_deliveries) {
            selectItem(0);
        }
        else if (id == R.id.nav_all_deliveries) {
            selectItem(1);
        }
        else if (id == R.id.nav_app_info) {
            AlertDialog.Builder dialogInfo = new AlertDialog.Builder(this);
            dialogInfo.setMessage("Developers: \n - Francesco Cappio Borlino\n - David Liffredo\n - Iman Ebrahimi Mehr");
            dialogInfo.setTitle("MAD lab3");
    
            dialogInfo.setCancelable(true);
            dialogInfo.create().show();
        }
        
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectItem(int position) {

        Fragment fragment = null;

        // before creating a new fragment we should check if the already displayed one is the same we want to open
        FragmentManager fragmentManager = getSupportFragmentManager();

        List<Fragment> fragments = fragmentManager.getFragments();

        Log.d(TAG, "Fragments count: " + fragments.size());

        for (Fragment fr : fragments) {
            if ((fr instanceof CurrentDeliveriesFragment) || (fr instanceof  OrdersFragment)) {
                fragment = fr;
                break;
            }
        }

        ActionBar actionBar = getSupportActionBar();
        currentSelectedPosition = position;

        boolean changed = false;
        switch (position) {
            case 0:
                if (!(fragment instanceof CurrentDeliveriesFragment)) {
                    currentDeliveriesFragment = new CurrentDeliveriesFragment();
                    fragment = currentDeliveriesFragment;
                    changed = true;
                }

                if (orderHistoryBundle != null )
                    fragment.setArguments(orderHistoryBundle);
                
                orderHistoryBundle = null;

                if (actionBar != null) {
                    if (riderOnDuty)
                        actionBar.setTitle(R.string.rider_on_duty);
                    else
                        actionBar.setTitle(R.string.rider_not_on_duty);
                }
                navigationView.setCheckedItem(R.id.nav_current_deliveries);

                break;

            case 1:
                if (!(fragment instanceof OrdersFragment)) {
                    ordersFragment = new OrdersFragment();
                    fragment = ordersFragment;
                    changed = true;
                }

                getSupportActionBar().setTitle(R.string.all_deliveries);
                navigationView.setCheckedItem(R.id.nav_all_deliveries);

                break;
            case 2:

                break;

            default:
                break;
        }


        if (fragment != null && changed) {
            selectedFragment = fragment;
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
        } else {
            Log.d("MainActivity", "No need to change the fragment");
        }
    }
    
    private void checkIfUserHasCompletedAccountInfo() {
        String userId = currentUser.getUid();
        dbRef.child(EAHCONST.USERS_SUB_TREE).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userEmail = (String) dataSnapshot.child(EAHCONST.USERS_MAIL).getValue();

                if (userEmail == null) {
                    //the user has not filled its account info yet
                    
                    Intent i = new Intent(getApplicationContext(), AccountInfoActivity.class);
                    i.putExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, true);
                    i.putExtra(EAHCONST.ACCOUNT_INFO_EMPTY, true);
                    startActivity(i);
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }


     @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        selectItem(currentSelectedPosition);
    }
    
    
    private void downloadOrdersInfo() {
        
        Query queryRef = dbRef
                .child(EAHCONST.ORDERS_RIDER_SUBTREE)
                .child(currentUser.getUid())
                .orderByKey();
        
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    
                allDeliveries = new ArrayList<>();
                if (!dataSnapshot.hasChildren()) {
                    Log.d(TAG, "There are no orders for this user");
                    Utility.showAlertToUser(MainActivity.this, R.string.alert_no_orders_yet);
                    checkAllOrdersDownloaded();
                    return;
                }
                
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    String restaurantId = (String) ds.child(EAHCONST.RIDER_ORDER_RESTAURATEUR_ID).getValue();
                    String customerID = (String) ds.child(EAHCONST.RIDER_ORDER_CUSTOMER_ID).getValue();
                    String orderId = ds.getKey();
                    EAHCONST.OrderStatus orderStatus = ds.child(EAHCONST.RIDER_ORDER_STATUS).getValue(EAHCONST.OrderStatus.class);
                    
                    if (orderStatus == EAHCONST.OrderStatus.PENDING) {
                        Utility.showAlertToUser(MainActivity.this, R.string.new_pending_order);
                    }
                    
                    RiderOrderDelivery co = new RiderOrderDelivery(orderId, restaurantId, customerID, orderStatus);
                    allDeliveries.add(co);
                }
                
                getOrdersDetails();
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error");
            }
        });
        
    }
    
    private void getOrdersDetails() {
        
        for (RiderOrderDelivery co : allDeliveries) {
            
            dbRef.child(EAHCONST.ORDERS_REST_SUBTREE)
                    .child(co.getRestaurantId())
                    .child(co.getOrderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(EAHCONST.REST_ORDER_STATUS).getValue() == null) {
                                Log.e(TAG, "This order does not exists");
                                return;
                            }
                            
                            String totalCost = (String) dataSnapshot.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue();
                            String deliveryTime = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_TIME).getValue();
                            String date = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DATE).getValue();
                            String deliveryAddress = (String) dataSnapshot.child(EAHCONST.REST_ORDER_DELIVERY_ADDRESS).getValue();
                            
                            co.setDeliveryAddress(deliveryAddress);
                            co.setTotalCost(totalCost);
                            co.setDeliveryTime(deliveryTime);
                            co.setDeliveryDate(date);
                            
                            downloadRestaurantsInfo(co, co.getRestaurantId());
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Database error");
                        }
                    });
            
        }
    }
    
    private void downloadRestaurantsInfo(RiderOrderDelivery co, String restaurantId) {
        dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE)
                .child(restaurantId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Log.e(TAG, "Cannot find name for restaurant: " + restaurantId);
                            return;
                        }
                        
                        co.setRestaurantName((String) dataSnapshot.child(EAHCONST.RESTAURANT_NAME).getValue());
                        co.setRestaurantAddress((String) dataSnapshot.child(EAHCONST.RESTAURANT_ADDRESS).getValue());
                        checkAllOrdersDownloaded();
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Database error");
                    }
                });
    }
    
    private synchronized void checkAllOrdersDownloaded() {
        
        if (allDeliveries == null)
            return;

        for (RiderOrderDelivery o : allDeliveries) {
            if (o.getRestaurantName() == null)
                return;
        }
    
        Collections.sort(allDeliveries);
        
        if (onDutySwitch != null)
            onDutySwitch.setEnabled(true);

        for (OrdersUpdateListener listener : ordersUpdateListeners) {
            try {
                listener.manageOrdersUpdate();
            } catch (Exception ex) {
                Log.e(TAG, "Exception while notifying one listener");
            }
        }
    }
    
    public void registerOrdersUpdateListener(OrdersUpdateListener listener) {
        ordersUpdateListeners.add(listener);
        
        if (allDeliveries != null) {
            listener.manageOrdersUpdate();
        }
    }
    
    public List<RiderOrderDelivery> getAllDeliveries() {
        return allDeliveries;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            Log.e(TAG, "Cannot check if service is running, activity manager is null");
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public static List<RiderOrderDelivery> getCurrentDeliveries(List<RiderOrderDelivery> allDeliveries) {
        List<RiderOrderDelivery> currentDeliveries = new ArrayList<>();

        for (RiderOrderDelivery rod : allDeliveries) {
            if (rod.getOrderStatus() == EAHCONST.OrderStatus.ONGOING || rod.getOrderStatus() == EAHCONST.OrderStatus.CONFIRMED || rod.getOrderStatus() == EAHCONST.OrderStatus.PENDING)
                currentDeliveries.add(rod);
        }
        return currentDeliveries;
    }
}
