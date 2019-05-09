package it.polito.maddroid.lab3.rider;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.EAHFirebaseMessagingService;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.Utility;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference storageReference;
    
    private LinearLayout llNavHeaderMain;
    private NavigationView navigationView;
    private TextView tvAccountEmail;
    private ImageView ivAvatar;
    
    
    public static String FILE_PROVIDER_AUTHORITY = "it.polito.maddroid.eatathome.fileprovider.rider";
    
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
        
        tvAccountEmail.setText(currentUser.getEmail());
    
        EAHFirebaseMessagingService.setActivityToLaunch(MainActivity.class);
    
        StorageReference riversRef = storageReference.child("avatar_" + currentUser.getUid() +".jpg");
        GlideApp.with(getApplicationContext())
                .load(riversRef)
                .into(ivAvatar);
    
        cancelAllTheNotifications();
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
            //TODO: start account info
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
        
        if (id == R.id.nav_delivery) {
            // Handle the camera action
        }
        else if (id == R.id.nav_deliveries_done) {
        
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
    
    private void checkIfUserHasCompletedAccountInfo() {
        String userId = currentUser.getUid();
        dbRef.child(EAHCONST.USERS_SUB_TREE).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userEmail = (String) dataSnapshot.child(EAHCONST.USERS_MAIL).getValue();

                if (userEmail == null) {
                    //the user has not filled its account info yet
                    
                    //TODO: start account info
                    Intent i = new Intent(getApplicationContext(), AccountInfoActivity.class);
                    i.putExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, true);
                    i.putExtra(EAHCONST.ACCOUNT_INFO_EMPTY, true);
//
                    startActivity(i);
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
}
