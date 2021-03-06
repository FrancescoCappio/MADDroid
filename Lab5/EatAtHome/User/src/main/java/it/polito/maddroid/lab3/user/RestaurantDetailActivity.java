package it.polito.maddroid.lab3.user;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.RestaurantCategory;
import it.polito.maddroid.lab3.common.ReviewsActivity;
import it.polito.maddroid.lab3.common.Utility;


public class RestaurantDetailActivity extends AppCompatActivity {
    
    // static const
    private static final String TAG = "RestaurantDetailActvty";
    
    // general purpose attributes
    private int waitingCount = 0;
    private List<RestaurantCategory> allCategories;
    private Restaurant currentRestaurant;
    List<Dish> dishes;
    private DishOrderListAdapter adapter;
    List<Dish> choosenDishes;
    
    // toolbars
    AppBarLayout appBarLayout;
    
    // views
    private TextView tvDescription;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvAddress;
    private TextView tvTimetable;
    private TextView tvCategories;
    private ImageView ivPhoto;
    private RecyclerView rvOrderDishes;
    private CardView cvTotalCost;
    private TextView tvTotalCost;
    private TextView tvRating;
    private RatingBar ratingBar;
    private ImageView ivFavorite;
    
    private MenuItem menuOrder;
    private boolean appBarExpanded;

    // Firebase attributes
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userUID;
    
    // String keys to store instances info
    private static final String NAME_KEY = "NAME_KEY";
    private static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    private static final String PHONE_KEY = "PHONE_KEY";
    private static final String EMAIL_KEY = "EMAIL_KEY";
    private static final String ADDRESS_KEY = "ADDRESS_KEY";
    private static final String CATEGORY_KEY = "CATEGORY_KEY";
    private static final String DISHES_KEY = "DISHES_KEY";
    
    public static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userUID = currentUser.getUid();
        
        dishes = new ArrayList<>();
    
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitleEnabled(false);
        
        getReferencesToViews();
        
        rvOrderDishes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        
        setupEventListeners();
        
        if (savedInstanceState == null) {
            currentRestaurant = (Restaurant) getIntent().getSerializableExtra(RESTAURANT_KEY);
        } else {
            currentRestaurant = (Restaurant) savedInstanceState.getSerializable(RESTAURANT_KEY);
            tvCategories.setText(savedInstanceState.getString(CATEGORY_KEY, ""));
        }
    
        tvDescription.setText(currentRestaurant.getDescription());
        tvAddress.setText(currentRestaurant.getAddress());
        tvPhoneNumber.setText(currentRestaurant.getPhone());
        tvEmail.setText(currentRestaurant.getEmail());
        tvRating.setText(currentRestaurant.getReviewCount() + " " + (currentRestaurant.getReviewCount() == 1 ? getString(R.string.reviews) : getString(R.string.reviews)));
        ratingBar.setRating(currentRestaurant.getReviewAvg());
        
        if (currentRestaurant.getTimeTableString() != null && !currentRestaurant.getTimeTableString().isEmpty())
            tvTimetable.setText(Utility.extractTimeTable(currentRestaurant.getTimeTableString()));
        else
            tvTimetable.setText(R.string.while_supplies_last);
    
        //download and set restaurant image
        StorageReference riversRef = mStorageRef.child("avatar_" + currentRestaurant.getRestaurantID() +".jpg");
    
        GlideApp.with(getApplicationContext())
                .load(riversRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivPhoto);
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(currentRestaurant.getName());
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    
        adapter = new DishOrderListAdapter(new DishDiffUtilCallBack(), currentRestaurant, this::choosenDishesUpdated);
    
        rvOrderDishes.setAdapter(adapter);
    
        if (savedInstanceState != null) {
            dishes = (List<Dish>) savedInstanceState.getSerializable(DISHES_KEY);
            adapter.submitList(dishes);
            
            choosenDishes = adapter.getChosenDishes();
            updateTotalVisibility();
        } else {
            downloadDishesInfo();
        }
    
        allCategories = new ArrayList<>();
    
        // download list of categories this restaurant belongs to
        downloadCategoriesInfo();

        checkFavorite();
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
                
            case R.id.menu_order:
                actionCompleteOrder();
                return true;
        }
        
        return false;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(NAME_KEY, currentRestaurant.getName());
        outState.putString(CATEGORY_KEY, tvCategories.getText().toString());
        outState.putSerializable(RESTAURANT_KEY, currentRestaurant);
        
        outState.putSerializable(DISHES_KEY, (Serializable) dishes);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.restaurant_detail_menu, menu);

        menuOrder = menu.findItem(R.id.menu_order);

        return true;
    }
    
    private void getReferencesToViews() {
        
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvEmail = findViewById(R.id.tv_mail);
        tvPhoneNumber = findViewById(R.id.tv_phone);
        tvTimetable = findViewById(R.id.tv_timetable);
        tvCategories = findViewById(R.id.tv_categories);
        ivPhoto = findViewById(R.id.iv_avatar);
        rvOrderDishes = findViewById(R.id.rv_order_dishes);
        cvTotalCost = findViewById(R.id.cv_total_cost);
        tvTotalCost = findViewById(R.id.tv_total_cost);
    
        appBarLayout = findViewById(R.id.app_bar);
        tvRating = findViewById(R.id.tv_rating);
        ratingBar = findViewById(R.id.rating_bar);
        ivFavorite = findViewById(R.id.bt_favorite);
        
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private void setupEventListeners() {
        
        tvPhoneNumber.setOnClickListener(v -> {
            Intent intent = Utility.generateIntentPhoneNumber(currentRestaurant.getPhone());
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        
        tvEmail.setOnClickListener(v -> {
            Intent intent = Utility.generateIntentEmail(currentRestaurant.getEmail());
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            
            appBarLayout.post(() -> {
                if (Math.abs(verticalOffset) == appBarLayout1.getTotalScrollRange()) {
                    // completely collapsed
                    appBarExpanded = false;
    
                } else if (verticalOffset == 0) {
                    // completely expanded
                    appBarExpanded = true;
    
                } else {
                    appBarExpanded = false;
                }

                updateTotalVisibility();
            });
            
        });
        
        cvTotalCost.setOnClickListener(v -> actionCompleteOrder());
        
        tvRating.setOnClickListener(v -> openReviewsActivity());

        ivFavorite.setOnClickListener(v -> setFavoriteAction());
    }

    private void setFavoriteAction() {
        String ivFavoriteTag = (String) ivFavorite.getTag();
        String updatePath = EAHCONST.generatePath
                (EAHCONST.CUSTOMERS_SUB_TREE, userUID, EAHCONST.CUSTOMER_FAVORITE_RESTAURANT, currentRestaurant.getRestaurantID());

        if (ivFavoriteTag.equals(getString(R.string.no))){
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put(updatePath, currentRestaurant.getRestaurantID());
            dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Success adding to favorite List");
            });
            setFavoriteIcon(true);
        } else {
            dbRef.child(updatePath).removeValue().addOnSuccessListener(aVoid ->{
                Log.d(TAG, "Success removing to favorite List");
            });
            setFavoriteIcon(false);
        }
    }

    private void setFavoriteIcon(boolean bool){
        if(bool){
            ivFavorite.setImageResource(R.drawable.ic_favorite_24dp);
            ivFavorite.setTag(getString(R.string.yes));
        }
        else{
            ivFavorite.setImageResource(R.drawable.ic_not_favorite_24dp);
            ivFavorite.setTag(getString(R.string.no));
        }
    }

    private void checkFavorite(){
        Query queryRef = dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(userUID).child(EAHCONST.CUSTOMER_FAVORITE_RESTAURANT);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentRestaurant.getRestaurantID()))
                    setFavoriteIcon(true);
                else
                    setFavoriteIcon(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }

    private void openReviewsActivity() {
        
        if (currentRestaurant.getReviewCount() == 0) {
            Utility.showAlertToUser(this, R.string.alert_no_reviews);
            return;
        }
        
        Intent ratingIntent = new Intent(RestaurantDetailActivity.this, ReviewsActivity.class);
        ratingIntent.putExtra(ReviewsActivity.RATING_MODE_KEY, ReviewsActivity.RATING_MODE_RESTAURANT);
        ratingIntent.putExtra(ReviewsActivity.RATED_UID_KEY, currentRestaurant.getRestaurantID());
        startActivity(ratingIntent);
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
        setActivityLoading(true);
        Query queryRef = dbRef
                .child(EAHCONST.CATEGORIES_SUB_TREE)
                .orderByChild(EAHCONST.CATEGORIES_NAME);
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    String catId = ds.getKey();
                    String catName = (String) ds.child(EAHCONST.CATEGORIES_NAME).getValue();
                    
                    RestaurantCategory rc = new RestaurantCategory(catId, catName);
                    
                    allCategories.add(rc);
                }
                setupCategoriesString();
                setActivityLoading(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }
    
    public void setupCategoriesString() {
        if (currentRestaurant != null && currentRestaurant.getCategoriesIds() != null)
            tvCategories.setText(Utility.getCategoriesNamesMatchingIds(currentRestaurant.getCategoriesIds(), allCategories));
    }
    
    private void downloadDishesInfo() {
        setActivityLoading(true);
        Query queryRef = dbRef
                .child(EAHCONST.DISHES_SUB_TREE).child(currentRestaurant.getRestaurantID())
                .orderByKey();
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                dishes = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    String dishId = ds.getKey();
                    String dishName = (String) ds.child(EAHCONST.DISH_NAME).getValue();
                    
                    
                    Float f = ds.child(EAHCONST.DISH_PRICE).getValue(Float.class);
                    float price = 0;
                    if (f != null) {
                        price = f;
                    }
                    String dishDescription = (String) ds.child(EAHCONST.DISH_DESCRIPTION).getValue();
                    
                    Dish dish = new Dish(Integer.parseInt(dishId),dishName,price,dishDescription);
                    
                    dishes.add(dish);
                    
                }
                adapter.submitList(dishes);
                setActivityLoading(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }
    
    private void choosenDishesUpdated() {
        choosenDishes = adapter.getChosenDishes();
        
        updateTotalVisibility();
    }
    
    private String computeTotalCost() {
        float totalCost = 0;
    
        for (Dish d : choosenDishes) {
            totalCost += d.getQuantity() * d.getPrice();
        }
    
        return String.format(Locale.US, "%.02f", totalCost) + " €";
    }
    
    private void updateTotalVisibility() {
        if (choosenDishes == null || choosenDishes.isEmpty()) {
            cvTotalCost.setVisibility(View.GONE);
            if (menuOrder != null)
                menuOrder.setVisible(false);
            
            return;
        }
        
        String completeOrder = getString(R.string.complete_order);
        
        String total = computeTotalCost();
        
        String msg = completeOrder + " " + total;
        
        tvTotalCost.setText(msg);
        
        if (menuOrder != null)
            menuOrder.setTitle(msg);
        
        if (appBarExpanded) {
            cvTotalCost.setVisibility(View.VISIBLE);
            if (menuOrder != null)
                menuOrder.setVisible(false);
        } else {
            cvTotalCost.setVisibility(View.GONE);
            if (menuOrder != null)
                menuOrder.setVisible(true);
        }
    }
    
    private void actionCompleteOrder() {
        Intent intent = new Intent(getApplicationContext(), CompleteOrderActivity.class);
        
        if (choosenDishes != null && !choosenDishes.isEmpty()) {
            intent.putExtra(CompleteOrderActivity.DISHES_KEY, (Serializable) choosenDishes);
            intent.putExtra(CompleteOrderActivity.RESTAURANT_KEY, currentRestaurant);
            startActivity(intent);
        }
    }
}
