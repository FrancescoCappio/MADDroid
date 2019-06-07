package it.polito.maddroid.lab3.user;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;
import it.polito.maddroid.lab3.common.RestaurantCategory;
import it.polito.maddroid.lab3.common.ReviewsActivity;
import it.polito.maddroid.lab3.common.Utility;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRestaurantDetail extends Fragment {
    
    
    public static final String TAG = "DishesListFragment";
    
    private static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    
    private Restaurant currentRestaurant;
    
    private TextView tvAddress;
    private TextView tvDescription;
    private TextView tvEmail;
    private TextView tvPhoneNumber;
    private TextView tvTimetable;
    private TextView tvCategories;
    private ImageView ivPhoto;
    private TextView tvRating;
    private RatingBar ratingBar;
    private ProgressBar pbLoading;
    private ImageView ivFavorite;
    
    private int waitingCount;
    
    private List<RestaurantCategory> allCategories;
    
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;
    
    public FragmentRestaurantDetail() {
        // Required empty public constructor
    }
    
    public static FragmentRestaurantDetail newInstance(Restaurant restaurant) {
        Bundle args = new Bundle();
        FragmentRestaurantDetail fragment = new FragmentRestaurantDetail();
        args.putSerializable(RESTAURANT_KEY, restaurant);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        currentRestaurant = (Restaurant) args.getSerializable(RESTAURANT_KEY);
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
        
        getReferencesToViews(view);
    
        mStorageRef = FirebaseStorage.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference();
    
        setupEventListeners();
    
        tvDescription.setText(currentRestaurant.getDescription());
        tvAddress.setText(currentRestaurant.getAddress());
        tvPhoneNumber.setText(currentRestaurant.getPhone());
        tvEmail.setText(currentRestaurant.getEmail());
        tvRating.setText(currentRestaurant.getReviewCount() + " " + (currentRestaurant.getReviewCount() == 1 ? getString(R.string.reviews) : getString(R.string.reviews)));
        ratingBar.setRating(currentRestaurant.getReviewAvg());
    
        StorageReference riversRef = mStorageRef.child("avatar_" + currentRestaurant.getRestaurantID() +".jpg");
    
        GlideApp.with(getContext())
                .load(riversRef)
                .placeholder(R.drawable.round_logo)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(EAHCONST.DEFAULT_IMAGE_SIZE, EAHCONST.DEFAULT_IMAGE_SIZE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivPhoto);
    
        if (currentRestaurant.getTimeTableString() != null && !currentRestaurant.getTimeTableString().isEmpty())
            tvTimetable.setText(Utility.extractTimeTable(currentRestaurant.getTimeTableString()));
        else
            tvTimetable.setText(R.string.while_supplies_last);
    
        allCategories = new ArrayList<>();
        
        downloadCategoriesInfo();
    
        checkFavorite();
        
        return view;
    }
    
    private void setupEventListeners() {
        
        tvPhoneNumber.setOnClickListener(v -> {
            Intent intent = Utility.generateIntentPhoneNumber(currentRestaurant.getPhone());
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        
        tvEmail.setOnClickListener(v -> {
            Intent intent = Utility.generateIntentEmail(currentRestaurant.getEmail());
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        
        
        tvRating.setOnClickListener(v -> openReviewsActivity());
    
        ivFavorite.setOnClickListener(v -> setFavoriteAction());
    }
    
    private void openReviewsActivity() {
        
        if (currentRestaurant.getReviewCount() == 0) {
            Utility.showAlertToUser(getActivity(), R.string.alert_no_reviews);
            return;
        }
        
        Intent ratingIntent = new Intent(getContext(), ReviewsActivity.class);
        ratingIntent.putExtra(ReviewsActivity.RATING_MODE_KEY, ReviewsActivity.RATING_MODE_RESTAURANT);
        ratingIntent.putExtra(ReviewsActivity.RATED_UID_KEY, currentRestaurant.getRestaurantID());
        startActivity(ratingIntent);
    }
    
    
    private void getReferencesToViews(View view) {
        
        tvAddress = view.findViewById(R.id.tv_address);
        tvDescription = view.findViewById(R.id.tv_description);
        tvEmail = view.findViewById(R.id.tv_mail);
        tvPhoneNumber = view.findViewById(R.id.tv_phone);
        tvTimetable = view.findViewById(R.id.tv_timetable);
        tvCategories = view.findViewById(R.id.tv_categories);
        ivPhoto = view.findViewById(R.id.iv_restaurant_photo);
        
        tvRating = view.findViewById(R.id.tv_rating);
        ratingBar = view.findViewById(R.id.rating_bar);
        pbLoading = view.findViewById(R.id.pb_loading);
        ivFavorite = view.findViewById(R.id.iv_favorite);
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
    
    private void checkFavorite(){
        Query queryRef = dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(EAHCONST.CUSTOMER_FAVORITE_RESTAURANT);
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
    
    private void setFavoriteAction() {
        String ivFavoriteTag = (String) ivFavorite.getTag();
        String updatePath = EAHCONST.generatePath
                (EAHCONST.CUSTOMERS_SUB_TREE, FirebaseAuth.getInstance().getCurrentUser().getUid(), EAHCONST.CUSTOMER_FAVORITE_RESTAURANT, currentRestaurant.getRestaurantID());
        
        setActivityLoading(true);
        
        if (ivFavoriteTag.equals(getString(R.string.no))){
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put(updatePath, currentRestaurant.getRestaurantID());
            dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Success adding to favorite List");
                setFavoriteIcon(true);
                Utility.showAlertToUser(getActivity(), R.string.added_favorite);
                setActivityLoading(false);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error while adding favorite");
                Utility.showAlertToUser(getActivity(), R.string.not_ready_alert);
                setActivityLoading(false);
            });
            
        } else {
            dbRef.child(updatePath).removeValue().addOnSuccessListener(aVoid ->{
                Log.d(TAG, "Success removing to favorite List");
                setFavoriteIcon(false);
                Utility.showAlertToUser(getActivity(), R.string.removed_favorite);
                setActivityLoading(false);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error while removing favorite");
                Utility.showAlertToUser(getActivity(), R.string.not_ready_alert);
                setActivityLoading(false);
            });
        }
    }
}
