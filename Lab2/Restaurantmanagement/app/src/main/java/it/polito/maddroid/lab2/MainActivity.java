package it.polito.maddroid.lab2;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    private List<DailyOffer> dailyOffers;
    
    private NavigationView navigationView;
    
    private int selectedId; //0 = orders; 1 = daily_offers
    
    private static final int ORDER_DETAIL_CODE = 123;
    private static final int DAILY_OFFER_DETAIL_CODE = 124;
    
    private final static String TAG = "MainActivity";
    
    private OrdersFragment ordersFragment;
    private DailyOffersFragment dailyOffersFragment;
    
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
        navigationView.setCheckedItem(R.id.nav_orders);
        
        selectItem(0);
       // setContentView(R.layout.activity_order_detail_activity);
        
    }
    
    private void selectItem(int position) {
        
        Fragment fragment = null;
        
        selectedId = position;
        
        switch (position) {
            case 0:
                ordersFragment = new OrdersFragment();
                fragment = ordersFragment;
                getSupportActionBar().setTitle(R.string.orders);
                break;
                
            case 1:
                dailyOffersFragment = new DailyOffersFragment();
                fragment = dailyOffersFragment;
                getSupportActionBar().setTitle(R.string.daily_offers);
                break;
            
            default:
                break;
        }
        
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
            
            navigationView.setCheckedItem(position);
            
//            mDrawerList.setItemChecked(position, true);
//            mDrawerList.setSelection(position);
//            setTitle(mNavigationDrawerItemTitles[position]);
//            mDrawerLayout.closeDrawer(mDrawerList);
        
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }
    
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            
            if (selectedId == 1) {
                Intent i = new Intent(getApplicationContext(), DailyOfferDetailActivity.class);
                i.putExtra(DailyOfferDetailActivity.PAGE_TYPE_KEY,DailyOfferDetailActivity.MODE_NEW);
                startActivity(i);
            }
            if(selectedId == 0){
                Intent i = new Intent(getApplicationContext(), OrderDetailActivity.class);
                startActivityForResult(i,ORDER_DETAIL_CODE);
            }
            return true;
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
        }
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        }
//        else if (id == R.id.nav_gallery) {
//
//        }
//        else if (id == R.id.nav_slideshow) {
//
//        }
//        else if (id == R.id.nav_manage) {
//
//        }
//        else if (id == R.id.nav_share) {
//
//        }
//        else if (id == R.id.nav_send) {
//
//        }
        
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
                int timeMinutes = data.getIntExtra(OrderDetailActivity.TIME_MIN_KEY, 0);
                int timeHour = data.getIntExtra(OrderDetailActivity.TIME_H_KEY, 0);
                
                String dish = data.getStringExtra(OrderDetailActivity.DISH_KEY);
                String customer = data.getStringExtra(OrderDetailActivity.CUSTOMER_KEY);
                String rider = data.getStringExtra(OrderDetailActivity.RIDER_KEY);
                
                String detail = dish + " " + customer + " " + rider;
                
                Order order = new Order(0, timeHour, timeMinutes, 0, 0);
                
                if (ordersFragment != null) {
                    ordersFragment.addOrder(order);
                }
    
                Toast.makeText(getApplicationContext(), detail, Toast.LENGTH_SHORT).show();
        }
    }
}
