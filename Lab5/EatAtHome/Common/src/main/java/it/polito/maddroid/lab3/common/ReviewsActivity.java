package it.polito.maddroid.lab3.common;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ReviewsActivity extends AppCompatActivity {
    
    public static final String TAG = "ReviewsActivity";
    
    public static final String RATING_MODE_KEY = "RATING_MODE";
    public static final String RATING_MODE_RESTAURANT = "MODE_RESTAURANT";
    public static final String RATING_MODE_RIDER = "MODE_RIDER";
    public static final String RATED_UID_KEY = "RATED_UID_KEY";
    
    private String currentMode;
    private String currentUID;
    
    private int reviewCount;
    private List<EAHCONST.Review> reviews;
    
    private DatabaseReference dbRef;
    
    private int waitingCount = 0;
    
    //views
    private ProgressBar pbExcellent;
    private ProgressBar pbVeryGood;
    private ProgressBar pbAverage;
    private ProgressBar pbPoor;
    private ProgressBar pbTerrible;
    private RecyclerView rvReviews;
    
    private ReviewListAdapter adapter;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
    
        Intent intent = getIntent();
    
        if (intent.getStringExtra(RATING_MODE_KEY) == null || intent.getStringExtra(RATED_UID_KEY) == null) {
            Log.e(TAG, "Cannot start ratingActivity without mode or uid");
            finish();
            return;
        }
        
        // get intent extras
        currentMode = intent.getStringExtra(RATING_MODE_KEY);
        currentUID = intent.getStringExtra(RATED_UID_KEY);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        downloadReviews();
        
        getReferencesToViews();
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.all_reviews);
        }
        
        rvReviews.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        
        adapter = new ReviewListAdapter(new ReviewDiffUtil());
        
        rvReviews.setAdapter(adapter);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
    
        if (item.getItemId() == android.R.id.home) {
            //emulate back pressed
            onBackPressed();
            return true;
        }
        return false;
    }
    
    private void getReferencesToViews() {
        pbExcellent = findViewById(R.id.pb_excellent);
        pbVeryGood = findViewById(R.id.pb_very_good);
        pbAverage = findViewById(R.id.pb_average);
        pbPoor = findViewById(R.id.pb_poor);
        pbTerrible = findViewById(R.id.pb_terrible);
        
        rvReviews = findViewById(R.id.rv_reviews);
    }
    
    private void downloadReviews() {
        
        reviews = new ArrayList<>();
        
        DatabaseReference dbRef1;
        
        setActivityLoading(true);
        
        if (currentMode.equals(RATING_MODE_RIDER))
            dbRef1 = dbRef.child(EAHCONST.RIDERS_RATINGS_SUBTREE).child(currentUID);
        else
            dbRef1 = dbRef.child(EAHCONST.RESTAURANTS_RATINGS_SUBTREE).child(currentUID);
        
        dbRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    setActivityLoading(false);
                    return;
                }
                
                reviewCount = (int) dataSnapshot.getChildrenCount();
                
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    EAHCONST.Review review = ds.getValue(EAHCONST.Review.class);
                    reviews.add(review);
                    downloadReviewAuthorType(review);
                }
                computeStatistics();
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setActivityLoading(false);
                Log.e(TAG, "Database Error while downloading reviews: " + databaseError.getMessage());
            }
        });
        
    }
    
    private void downloadReviewAuthorType(EAHCONST.Review review) {
        if (review == null)
            return;
        dbRef.child(EAHCONST.USERS_SUB_TREE).child(review.getAuthorUID()).child(EAHCONST.USERS_TYPE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    EAHCONST.USER_TYPE userType = dataSnapshot.getValue(EAHCONST.USER_TYPE.class);
                    downloadReviewAuthorName(review, userType);
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database Error while downloading user type: " + databaseError.getMessage());
            }
        });
    }
    
    private void downloadReviewAuthorName(EAHCONST.Review review, EAHCONST.USER_TYPE userType) {
        
        DatabaseReference dbRef1;
        if (userType == EAHCONST.USER_TYPE.CUSTOMER) {
            dbRef1 = dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(review.getAuthorUID()).child(EAHCONST.CUSTOMER_NAME);
        } else if (userType == EAHCONST.USER_TYPE.RESTAURATEUR) {
            dbRef1 = dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(review.getAuthorUID()).child(EAHCONST.RESTAURANT_NAME);
        } else {
            return;
        }
        
        dbRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    review.setAuthorName(dataSnapshot.getValue(String.class));
                    checkAllNamesDownloaded();
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database Error while downloading user type: " + databaseError.getMessage());
            }
        });
    }
    
    private void checkAllNamesDownloaded() {
        if (reviews.size() != reviewCount)
            return;
        
        for (EAHCONST.Review r : reviews) {
            if (r.getAuthorName() == null)
                return;
        }
    
        Collections.sort(reviews);
        adapter.submitList(reviews);
        setActivityLoading(false);
    }
    
    private void computeStatistics() {
        int total = reviews.size();
        
        pbExcellent.setMax(total);
        pbVeryGood.setMax(total);
        pbAverage.setMax(total);
        pbPoor.setMax(total);
        pbTerrible.setMax(total);
        
        int countExcellent = 0;
        int countVeryGood = 0;
        int countAverage = 0;
        int countPoor = 0;
        int countTerrible = 0;
        
        for (EAHCONST.Review r : reviews) {
            switch (r.getRate()) {
                case 5:
                    countExcellent++;
                    break;
                case 4:
                    countVeryGood++;
                    break;
                case 3:
                    countAverage++;
                    break;
                case 2:
                    countPoor++;
                    break;
                case 1:
                    countTerrible++;
                    break;
            }
        }
        
        pbExcellent.setProgress(countExcellent);
        pbVeryGood.setProgress(countVeryGood);
        pbAverage.setProgress(countAverage);
        pbPoor.setProgress(countPoor);
        pbTerrible.setProgress(countTerrible);
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
    
    
}
