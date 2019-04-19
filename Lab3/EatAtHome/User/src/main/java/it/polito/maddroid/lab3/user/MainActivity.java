package it.polito.maddroid.lab3.user;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.Utility;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    LinearLayout llNavHeaderMain;
    NavigationView navigationView;
    TextView tvAccountEmail;
    
    public static String FILE_PROVIDER_AUTHORITY = "it.polito.maddroid.eatathome.fileprovider.user";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupNavigation();
    
        getReferencesToViews();
        
        setupClickListeners();
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        
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
        //TODO: check that the logged in user is a User (not rider, not restaurateur) -> else alert and exit
        
        tvAccountEmail.setText(currentUser.getEmail());
        //TODO: set avatar image as navigation view header's image
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
        tvAccountEmail = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_restaurant_email);
        
    }
    
    private void setupClickListeners() {
        
        llNavHeaderMain.setOnClickListener(v -> {
            //TODO: start account info
//            Intent i = new Intent(getApplicationContext(), AccountInfoActivity.class);
//            i.putExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, false);
//            startActivity(i);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        
        return super.onOptionsItemSelected(item);
    }
    
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        
        if (id == R.id.nav_orders) {
            // Handle the camera action
        }
        else if (id == R.id.nav_menu) {
        
        }
        else if (id == R.id.nav_settings) {
        
        }
        else if (id == R.id.nav_app_info) {
        
        }
        
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    
    private void checkIfUserHasCompletedAccountInfo() {
        String userId = currentUser.getUid();
        dbRef.child(EAHCONST.USERS_SUB_TREE).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userEmail = (String) dataSnapshot.child(EAHCONST.USERS_MAIL).getValue();
                
                if (userEmail == null) {
                    //the user has not filled its account info yet
                    //TODO: start account info
//                    Intent i = new Intent(getApplicationContext(), AccountInfoActivity.class);
//                    i.putExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, true);
//                    i.putExtra(EAHCONST.ACCOUNT_INFO_EMPTY, true);
//
//                    startActivity(i);
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
}
