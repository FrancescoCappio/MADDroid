package it.polito.maddroid.lab3.common;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RatingActivity extends AppCompatActivity {
    
    public static final String TAG = "RatingActivity";
    
    public static final String RATING_MODE_KEY = "RATING_MODE";
    public static final String RATING_MODE_RESTAURANT = "MODE_RESTAURANT";
    public static final String RATING_MODE_RIDER = "MODE_RIDER";
    public static final String RATED_UID_KEY = "RATED_UID_KEY";
    public static final String RATER_TYPE_KEY = "RATER_TYPE_KEY";
    public static final String RATER_TYPE_USER = "RATER_TYPE_USER";
    public static final String RATER_TYPE_RESTAURANT = "RATER_TYPE_RESTAURANT";
    public static final String RATER_UID_KEY = "RATER_UID_KEY";
    public static final String RATING_ORDER_KEY = "RATING_ORDER_KEY";
    
    private String currentMode;
    private String currentUID;
    
    private String currentRaterType;
    private String currentRaterUID;
    private String currentOrderId;
    
    private float currentRating = -1.0f;
    
    //firebase vars
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    //Views
    private RatingBar ratingBar;
    private EditText etComment;
    private TextView tvCharCount;
    
    private int COMMENT_MAX_LENGTH;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
    
        Intent intent = getIntent();
        
        if (intent.getStringExtra(RATING_MODE_KEY) == null ||
                intent.getStringExtra(RATED_UID_KEY) == null ||
                intent.getStringExtra(RATER_TYPE_KEY) == null ||
                intent.getStringExtra(RATER_UID_KEY) == null ||
                intent.getStringExtra(RATING_ORDER_KEY) == null) {
            Log.e(TAG, "Cannot start ratingActivity without all needed extras");
            finish();
            return;
        }
        
        // get intent extras
        currentMode = intent.getStringExtra(RATING_MODE_KEY);
        currentUID = intent.getStringExtra(RATED_UID_KEY);
        currentRaterType = intent.getStringExtra(RATER_TYPE_KEY);
        currentRaterUID = intent.getStringExtra(RATER_UID_KEY);
        currentOrderId = intent.getStringExtra(RATING_ORDER_KEY);
    
        // get firebase vars references
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        ActionBar actionBar = getSupportActionBar();
    
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            
            if (currentMode.equals(RATING_MODE_RIDER))
                actionBar.setTitle(R.string.rate_your_rider);
            else
                actionBar.setTitle(R.string.rate_your_restaurant);
        }
    
        Resources res = getResources();
        COMMENT_MAX_LENGTH = res.getInteger(R.integer.rating_max_length);
        
        getReferencesToViews();
        
        setupEventListeners();
    }
    
    private void getReferencesToViews() {
        ratingBar = findViewById(R.id.rating_bar);
        etComment = findViewById(R.id.et_comment);
        tvCharCount = findViewById(R.id.tv_char_comment_count);
    }
    
    private void setupEventListeners() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            currentRating = rating;
            Log.d(TAG, "Choosed rating: " + currentRating);
        });
    
        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        
            @Override
            public void afterTextChanged(Editable s) {
                updateCharCount();
            }
        });
    }
    
    private void updateCharCount() {
        int count = etComment.getText().length();
        
        String cnt = count + "/" + COMMENT_MAX_LENGTH;
        
        tvCharCount.setText(cnt);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account_info_menu, menu);
        
        MenuItem editMenu = menu.findItem(R.id.menu_edit);
        editMenu.setVisible(false);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        if (item.getItemId() == android.R.id.home) {
            //emulate back pressed
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_confirm) {
            Log.d(TAG, "Confirm pressed");
            uploadRating();
            
            return true;
        }
        return false;
    }
    
    private void uploadRating() {
        
        if (currentRating < 0.1f) {
            Utility.showAlertToUser(this, R.string.alert_no_score);
            return;
        }
        
        if (etComment.getText().toString().isEmpty()) {
            Utility.showAlertToUser(this, R.string.alert_no_comment);
            return;
        }
        
        EAHCONST.Review review = new EAHCONST.Review(currentUser.getUid(), (int) currentRating, etComment.getText().toString(), new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        
        String ratingID = Utility.generateUUID();
        
        String path1;
        
        String path2 = EAHCONST.generatePath(
                EAHCONST.RATINGS_OF_CUSTOMERS_SUBTREE,
                currentUser.getUid());
        if (currentMode.equals(RATING_MODE_RIDER)) {
            path2 = EAHCONST.generatePath(path2, EAHCONST.RIDERS_RATINGS, currentUID, ratingID);
            path1 = EAHCONST.generatePath(EAHCONST.RIDERS_RATINGS_SUBTREE, currentUID, ratingID);
        } else {
            path2 = EAHCONST.generatePath(path2, EAHCONST.RESTAURANT_RATINGS, currentUID, ratingID);
            path1 = EAHCONST.generatePath(EAHCONST.RESTAURANTS_RATINGS_SUBTREE, currentUID, ratingID);
        }
        
        Map<String, Object> updateMap = new HashMap<>();
        
        updateMap.put(path1, review);
        updateMap.put(path2, review);
        
        dbRef.updateChildren(updateMap, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e(TAG, "Error while saving rating");
                Utility.showAlertToUser(RatingActivity.this, R.string.notify_review_upload_ko);
                return;
            }
            
            Log.d(TAG, "Saved correctly");
            finish();
        });
        
    }
}
