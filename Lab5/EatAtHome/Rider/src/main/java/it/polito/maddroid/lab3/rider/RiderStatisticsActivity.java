package it.polito.maddroid.lab3.rider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.ReviewsActivity;
import it.polito.maddroid.lab3.common.Utility;

public class RiderStatisticsActivity extends AppCompatActivity {

    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;

    private TextView kmToday;
    private TextView kmMonthly;
    private TextView kmTotal;
    private TextView incomeToday;
    private TextView bestTravel;
    private TextView incomeMonthly;
    private TextView incomeTotal;
    private RatingBar ratingBar;
    private TextView tvRating;
    private static ProgressBar pbLoading;


    private float kmDay;
    private float kmMonth;
    private float allKm;
    private float avgGrade;
    private long totGrade;
    private float bestWork;
    private float profitDay;
    private float profitMonth;
    private float profitTotal;
    private int count = 0;
    private float waitingCount;

    public static final String KM_TODAY_KEY = "KM_TODAY_KEY";
    public static final String KM_MONTH_KEY = "KM_MONTH_KEY";
    public static final String KM_TOTAL_KEY = "KM_TOTAL_KEY";
    public static final String AVG_GRADE_KEY = "AVG_GRADE_KEY";
    public static final String COUNT_GRADE_KEY = "COUNT_GRADE_KEY";
    public static final String PROFIT_TODAY_KEY = "PROFIT_TODAY_KEY";
    public static final String PROFIT_MONTH_KEY = "PROFIT_MONTH_KEY";
    public static final String PROFIT_TOTAL_KEY = "PROFIT_TOTAL_KEY";
    public static final String BEST_WORK_KEY = "BEST_WORK_KEY";
    private String TAG = "RIDERSTATISTICS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_statistics);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.rider_stats);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pbLoading = findViewById(R.id.pb_loading_stats);
        kmToday = findViewById(R.id.tv_km_today);
        bestTravel = findViewById(R.id.tv_best_work);
        kmMonthly = findViewById(R.id.tv_km_month);
        kmTotal = findViewById(R.id.tv_km_total);
        incomeToday = findViewById(R.id.tv_today_profit);
        incomeMonthly = findViewById(R.id.tv_month_profit);
        incomeTotal = findViewById(R.id.tv_total_profit);
        ratingBar = findViewById(R.id.rating_bar);
        tvRating = findViewById(R.id.tv_ratings);

        if (savedInstanceState != null) {

            kmDay = savedInstanceState.getFloat(KM_TODAY_KEY);
            kmMonth = savedInstanceState.getFloat(KM_MONTH_KEY);
            allKm = savedInstanceState.getFloat(KM_TOTAL_KEY);
            profitDay = savedInstanceState.getFloat(PROFIT_TODAY_KEY);
            profitMonth = savedInstanceState.getFloat(PROFIT_MONTH_KEY);
            profitTotal = savedInstanceState.getFloat(PROFIT_TOTAL_KEY);
            bestWork = savedInstanceState.getFloat(BEST_WORK_KEY);
            avgGrade = savedInstanceState.getFloat(AVG_GRADE_KEY);
            totGrade = savedInstanceState.getLong(COUNT_GRADE_KEY);
            kmToday.setText("" + kmDay);
            kmMonthly.setText("" + kmMonth);
            kmTotal.setText("" + allKm);
            bestTravel.setText(""+bestWork);
            incomeToday.setText("" + profitDay);
            incomeMonthly.setText("" + profitMonth);
            incomeTotal.setText("" + profitTotal);

        } else {
            kmDay = 0;
            kmMonth = 0;
            allKm = 0;
            profitDay = 0;
            profitMonth = 0;
            profitTotal = 0;
            bestWork = 0;
            avgGrade = 0.0f;
            totGrade = 0;

            calculateStats();
        }


        tvRating.setOnClickListener(v -> openReviewsActivity());

    }


    private void calculateStats() {
        setActivityLoading(true);

        dbRef.child(EAHCONST.RIDERS_SUB_TREE).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child(EAHCONST.RIDER_REVIEW_AVG).getValue() != null)&&(dataSnapshot.child(EAHCONST.RIDER_REVIEW_COUNT).getValue() != null))
                {
                    totGrade = (long) dataSnapshot.child(EAHCONST.RIDER_REVIEW_COUNT).getValue();
                    avgGrade = dataSnapshot.child(EAHCONST.RIDER_REVIEW_AVG).getValue(Double.class).floatValue();
                }
                ratingBar.setRating(avgGrade);
                tvRating.setText(""+totGrade+" "+ (totGrade == 1 ? getString(R.string.reviews) : getString(R.string.reviews)));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child(EAHCONST.ORDERS_RIDER_SUBTREE).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    float kmtoRest = 0;
                    float kmtoCust = 0;
                    float profit = 0;

                    String orderId = ds.getKey();
                    String restaurantId = (String) ds.child(EAHCONST.RIDER_ORDER_RESTAURATEUR_ID).getValue();
                    if (ds.child(EAHCONST.RIDER_KM_REST).getValue() != null)
                        kmtoRest =  ds.child(EAHCONST.RIDER_KM_REST).getValue(Double.class).floatValue();
                    if (ds.child(EAHCONST.RIDER_KM_REST_CUST).getValue() != null)
                        kmtoCust =  ds.child(EAHCONST.RIDER_KM_REST_CUST).getValue(Double.class).floatValue();
                    if (ds.child(EAHCONST.RIDER_INCOME).getValue() != null)
                        profit = ds.child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue();
                    if(bestWork < (kmtoCust + kmtoRest))
                        bestWork = kmtoCust + kmtoRest;
                    allKm = kmtoCust + kmtoRest + allKm;
                    profitTotal = profitTotal + profit;

                    if (kmtoCust != 0 || kmtoRest != 0)
                    {
                        count++;
                        computeDayAndMonth(kmtoCust, kmtoRest, restaurantId, orderId, profit);
                    }

                    kmTotal.setText("" + allKm);
                    incomeTotal.setText("" + profitTotal);
                    bestTravel.setText(""+bestWork+" km");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "DATABASE ERROR " + databaseError);
            }
        });


    }

    private void computeDayAndMonth(float kmtoCust, float kmtoRest, String restaurantId, String orderId, float profit) {

        dbRef.child(EAHCONST.ORDERS_REST_SUBTREE).child(restaurantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                String currentM = date.split("-")[1];
                count--;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(orderId)) {
                        String dateDb = (String) ds.child(EAHCONST.REST_ORDER_DATE).getValue();
                        String monthDb = dateDb.split("-")[1];
                        if (dateDb.equals(date)) {
                            kmDay = kmDay + kmtoCust + kmtoRest;
                            kmMonth = kmMonth + kmtoCust + kmtoRest;
                            profitMonth = profitMonth + profit;
                            profitDay = profitDay + profit;
                        } else if (currentM.equals(monthDb))
                        {
                            kmMonth = kmMonth + kmtoCust + kmtoRest;
                            profitMonth = profitMonth + profit;
                        }

                        break;
                    }


                }
                if (count == 0) {
                    kmMonthly.setText("" + kmMonth);
                    kmToday.setText("" + kmDay);
                    incomeToday.setText("" + profitDay);
                    incomeMonthly.setText("" + profitMonth);
                    setActivityLoading(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "DATABASE ERROR " + databaseError);
            }
        });


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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(KM_TODAY_KEY, kmDay);
        outState.putFloat(KM_MONTH_KEY, kmMonth);
        outState.putFloat(KM_TOTAL_KEY, allKm);
        outState.putFloat(PROFIT_TODAY_KEY, profitDay);
        outState.putFloat(PROFIT_MONTH_KEY, profitMonth);
        outState.putFloat(PROFIT_TOTAL_KEY, profitTotal);
        outState.putFloat(AVG_GRADE_KEY, avgGrade);
        outState.putLong(COUNT_GRADE_KEY, totGrade);

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

    private void openReviewsActivity() {
        if (totGrade == 0) {
            Utility.showAlertToUser(this, R.string.alert_no_reviews);
            return;
        }

        Intent ratingIntent = new Intent(RiderStatisticsActivity.this, ReviewsActivity.class);
        ratingIntent.putExtra(ReviewsActivity.RATING_MODE_KEY, ReviewsActivity.RATING_MODE_RIDER);
        ratingIntent.putExtra(ReviewsActivity.RATED_UID_KEY, currentUser.getUid());
        startActivity(ratingIntent);

    }

}
