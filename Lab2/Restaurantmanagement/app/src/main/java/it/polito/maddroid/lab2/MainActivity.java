package it.polito.maddroid.lab2;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    private NavigationView navigationView;
    
    public static final int ORDER_DETAIL_CODE = 123;
    public static final int DAILY_OFFER_DETAIL_CODE = 124;

    
    private final static String TAG = "MainActivity";
    private static String STATE_SELECTED_POSITION = "state_selected_position";
    
    private OrdersFragment ordersFragment;
    private DailyOffersFragment dailyOffersFragment;
    private RestaurantFragment restaurantFragment;
    private int currentSelectedPosition; //0 = orders; 1 = daily_offers; 2 = restaurant
    
    private MenuItem addItem;
    private MenuItem confirmItem;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        if (savedInstanceState == null)
            selectItem(0);
        
    }

    public void selectItem(int position) {
        
        Fragment fragment = null;
        
        // before creating a new fragment we should check if the already displayed one is the same we want to open
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        List<Fragment> fragments = fragmentManager.getFragments();
        
        Log.d(TAG, "Fragments count: " + fragments.size());
        
        for (Fragment fr : fragments) {
            if ((fr instanceof OrdersFragment) || (fr instanceof  DailyOffersFragment) || (fr instanceof RestaurantFragment)) {
                fragment = fr;
                break;
            }
        }
    
        currentSelectedPosition = position;
        
        boolean changed = false;
        switch (position) {
            case 0:
                if (!(fragment instanceof OrdersFragment)) {
                    ordersFragment = new OrdersFragment();
                    fragment = ordersFragment;
                    changed = true;
                }
                
                getSupportActionBar().setTitle(R.string.orders);
                if(addItem != null)
                    addItem.setVisible(true);
                if (confirmItem != null)
                    confirmItem.setVisible(false);
                
                navigationView.setCheckedItem(R.id.nav_orders);
                break;
                
            case 1:
    
                if (!(fragment instanceof DailyOffersFragment)) {
                    dailyOffersFragment = new DailyOffersFragment();
                    fragment = dailyOffersFragment;
                    changed = true;
                }
                
                getSupportActionBar().setTitle(R.string.daily_offers);
                if(addItem != null)
                    addItem.setVisible(true);
                if (confirmItem != null)
                    confirmItem.setVisible(false);
                navigationView.setCheckedItem(R.id.nav_daily_offers);
                break;
            case 2:
    
                if (!(fragment instanceof RestaurantFragment)) {
                    restaurantFragment = new RestaurantFragment();
                    fragment = restaurantFragment;
                    changed = true;
                }
                
                getSupportActionBar().setTitle("Restaurant Details");
                if(addItem != null)
                    addItem.setVisible(false);
                if (confirmItem != null)
                    confirmItem.setVisible(true);
                navigationView.setCheckedItem(R.id.nav_restaurant_details);

            default:
                break;
        }
        
        if (fragment != null && changed) {
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
        } else {
            Log.d("MainActivity", "No need to change the fragment");
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        addItem = menu.findItem(R.id.action_add);
        confirmItem = menu.findItem(R.id.action_confirm);
        
        if (currentSelectedPosition == 0 || currentSelectedPosition == 1) {
            confirmItem.setVisible(false);
            addItem.setVisible(true);
        } else {
            confirmItem.setVisible(true);
            addItem.setVisible(false);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            if (currentSelectedPosition == 1) {
                Intent i = new Intent(getApplicationContext(), DailyOfferDetailActivity.class);
                i.putExtra(DailyOfferDetailActivity.PAGE_TYPE_KEY,DailyOfferDetailActivity.MODE_NEW);
                startActivityForResult(i, DAILY_OFFER_DETAIL_CODE);
            }
            if(currentSelectedPosition == 0){
                Intent i = new Intent(getApplicationContext(), OrderDetailActivity.class);
                i.putExtra(OrderDetailActivity.PAGE_TYPE_KEY,OrderDetailActivity.MODE_NEW);
                startActivityForResult(i,ORDER_DETAIL_CODE);
            }
            return true;
        } else if (id == R.id.action_confirm) {
            
            if (currentSelectedPosition != 2) {
                Log.e(TAG, "This shoud not be possible");
                return true;
            }
            
            if (restaurantFragment != null)
                restaurantFragment.saveData();
        
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        
        if (id == R.id.nav_daily_offers) {
            selectItem(1);
            
        } else if (id == R.id.nav_orders) {

            selectItem(0);

        } else if (id == R.id.nav_restaurant_details) {

            selectItem(2);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            case ORDER_DETAIL_CODE:
                if (ordersFragment != null)
                    ordersFragment.notifyUpdate();
                break;
                
            case DAILY_OFFER_DETAIL_CODE:
                if (dailyOffersFragment != null) {
                    dailyOffersFragment.notifyUpdate();
                }
                break;
                
            default:
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
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
    
}
