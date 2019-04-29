package it.polito.maddroid.lab3.user;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.SplashScreenActivity;
import it.polito.maddroid.lab3.common.Utility;


public class RestaurantDetailActivity extends AppCompatActivity {
    
    // static const
    private static final String TAG = "RestaurantDetailActvty";
    
    // general purpose attributes
    private int waitingCount = 0;
    private List<String> categories;
    private Restaurant currentRestaurant;
    
    // views
    private TextView tvDescription;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvAddress;
    private TextView tvTimetable;
    private TextView tvCategories;
    private ImageView ivPhoto;
    
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;
    
    // String keys to store instances info
    private static final String NAME_KEY = "NAME_KEY";
    private static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    private static final String PHONE_KEY = "PHONE_KEY";
    private static final String EMAIL_KEY = "EMAIL_KEY";
    private static final String ADDRESS_KEY = "ADDRESS_KEY";
    private static final String CATEGORY_KEY = "CATEGORY_KEY";
    private static final String TIMETABLE_KEY = "TIMETABLE_KEY";
    
    public static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        getReferencesToViews();
        
        setupClickListeners();
        
        if (savedInstanceState == null) {
            currentRestaurant = (Restaurant) getIntent().getSerializableExtra(RESTAURANT_KEY);
            
            // download list of categories this restaurant belongs to
            downloadCategoriesInfo();
            
            //TODO: also download timetables info
    
            tvDescription.setText(currentRestaurant.getDescription());
            tvAddress.setText(currentRestaurant.getAddress());
            tvPhoneNumber.setText(currentRestaurant.getPhone());
            tvEmail.setText(currentRestaurant.getEmail());
    
            //download and set restaurant image
            StorageReference riversRef = mStorageRef.child("avatar_" + currentRestaurant.getRestaurantID() +".jpg");
    
            GlideApp.with(getApplicationContext())
                    .load(riversRef)
                    .into(ivPhoto);
    
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(currentRestaurant.getName());
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        
        
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
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(NAME_KEY, currentRestaurant.getName());
        outState.putString(DESCRIPTION_KEY, tvDescription.getText().toString());
        outState.putString(PHONE_KEY, tvPhoneNumber.getText().toString());
        outState.putString(EMAIL_KEY, tvEmail.getText().toString());
        outState.putString(ADDRESS_KEY, tvAddress.getText().toString());
        outState.putString(TIMETABLE_KEY, tvTimetable.getText().toString());
        outState.putString(CATEGORY_KEY, tvCategories.getText().toString());
        outState.putSerializable(RESTAURANT_KEY, currentRestaurant);
        
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(currentRestaurant.getName());
        }

        tvDescription.setText(savedInstanceState.getString(DESCRIPTION_KEY, ""));
        tvAddress.setText(savedInstanceState.getString(ADDRESS_KEY, ""));
        tvPhoneNumber.setText(savedInstanceState.getString(PHONE_KEY, ""));
        tvEmail.setText(savedInstanceState.getString(EMAIL_KEY, ""));
        tvTimetable.setText(savedInstanceState.getString(TIMETABLE_KEY, ""));
        tvCategories.setText(savedInstanceState.getString(CATEGORY_KEY, ""));
        
        currentRestaurant = (Restaurant) savedInstanceState.getSerializable(RESTAURANT_KEY);
        
    }
    
    
    private void getReferencesToViews() {
        
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvEmail = findViewById(R.id.tv_mail);
        tvPhoneNumber = findViewById(R.id.tv_phone);
        tvTimetable = findViewById(R.id.tv_timetable);
        tvCategories = findViewById(R.id.tv_categories);
        ivPhoto = findViewById(R.id.iv_avatar);
        
    }
    
    private void setupClickListeners() {
        //TODO: add click listeners to phone number (to direct call), email addres (to direct new email)
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

    private void downloadCategoriesInfo() {
        
        //TODO: fix this
        
        Query queryRef = dbRef
                .child(EAHCONST.CATEGORIES_SUB_TREE)
                .orderByChild(currentRestaurant.getRestaurantID());
        
        setActivityLoading(true);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setActivityLoading(false);
                Log.d(TAG, "onDataChange Called");
                ArrayList<String> tmp = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String catName = (String) ds.child(EAHCONST.CATEGORIES_NAME).getValue();
                    tmp.add(catName);
                }
                categories = tmp;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }
}
