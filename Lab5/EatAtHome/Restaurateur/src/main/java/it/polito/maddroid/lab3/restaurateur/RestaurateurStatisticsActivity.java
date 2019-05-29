package it.polito.maddroid.lab3.restaurateur;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import it.polito.maddroid.lab3.common.DateTool;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.ReviewsActivity;
import it.polito.maddroid.lab3.common.Utility;

public class RestaurateurStatisticsActivity extends AppCompatActivity {

    private String TAG = "RestaurateurStatisticsActivity";

    public static final String QUANTITY_TODAY_KEY = "QUANTITY_TODAY_KEY";
    public static final String QUANTITY_MONTH_KEY = "QUANTITY_MONTH_KEY";
    public static final String QUANTITY_YEAR_KEY = "QUANTITY_YEAR_KEY";
    public static final String QUANTITY_TOTAL_KEY = "QUANTITY_TOTAL_KEY";
    public static final String PROFIT_TODAY_KEY = "PROFIT_TODAY_KEY";
    public static final String PROFIT_MONTH_KEY = "PROFIT_MONTH_KEY";
    public static final String PROFIT_YEAR_KEY = "PROFIT_YEAR_KEY";
    public static final String PROFIT_TOTAL_KEY = "PROFIT_TOTAL_KEY";

    public static final String AVG_GRADE_KEY = "AVG_GRADE_KEY";
    public static final String COUNT_GRADE_KEY = "COUNT_GRADE_KEY";

    public static final String BEST_WORK_KEY = "BEST_WORK_KEY";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;

    private TextView tvQuantityToday;
    private TextView tvQuantityMonthly;
    private TextView tvQuantityYearly;
    private TextView tvQuantityTotal;

    private TextView tvIncomeToday;
    private TextView tvIncomeMonthly;
    private TextView tvIncomeYearly;
    private TextView tvIncomeTotal;

    private TextView tvBestOrder;
    private RatingBar rbRatingBar;
    private TextView tvRating;
    private static ProgressBar pbLoading;

    private Button btBusyHour;
    private Button btDailyStat;
    private Button btmonthlyStat;
    private Button btYearlyStat;

    private float waitingCount;

    private float avgGrade;
    private long totGrade;

    private float profitDay;
    private float profitMonth;
    private float profitYear;
    private float profitTotal;
    private float bestIncomingOrder;

    private int quantityDay;
    private int quantityMonth;
    private int quantityYear;
    private int quantityTotal;

    private DateTool dt;
    private HashMap<String, HashMap<Integer,Integer>> weekHours;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurateur_statistics);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dt = new DateTool();


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.rider_stats);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getReferencesToViews();

        setOnClickListeners();

        if (savedInstanceState != null) {

            avgGrade = savedInstanceState.getFloat(AVG_GRADE_KEY);
            totGrade = savedInstanceState.getLong(COUNT_GRADE_KEY);
            bestIncomingOrder = savedInstanceState.getFloat(BEST_WORK_KEY);

            profitDay = savedInstanceState.getFloat(PROFIT_TODAY_KEY);
            profitMonth = savedInstanceState.getFloat(PROFIT_MONTH_KEY);
            profitYear = savedInstanceState.getFloat(PROFIT_YEAR_KEY);
            profitTotal = savedInstanceState.getFloat(PROFIT_TOTAL_KEY);

            quantityDay = savedInstanceState.getInt(QUANTITY_TODAY_KEY);
            quantityMonth = savedInstanceState.getInt(QUANTITY_MONTH_KEY);
            quantityYear = savedInstanceState.getInt(QUANTITY_YEAR_KEY);
            quantityTotal = savedInstanceState.getInt(QUANTITY_TOTAL_KEY);
            setDataToView();
        }
        else
            getRating();

    }

    private void setOnClickListeners() {
        tvRating.setOnClickListener(v -> openReviewsActivity());
        btBusyHour.setOnClickListener(v -> {
            if (weekHours == null) {
                Utility.showAlertToUser(this , R.string.alert_to_get_busy_hour);
                return;
            }

            Intent intent = new Intent(getApplicationContext(), BusyHoursActivity.class);
            intent.putExtra(BusyHoursActivity.BUSY_HOURS_KEY,(Serializable) weekHours);
            startActivity(intent);
        });
    }

    private void getReferencesToViews() {

        pbLoading = findViewById(R.id.pb_loading_stats);
        tvBestOrder = findViewById(R.id.tv_best_work);
        rbRatingBar = findViewById(R.id.rating_bar);
        tvRating = findViewById(R.id.tv_ratings);

        tvQuantityToday = findViewById(R.id.tv_quantity_today);
        tvQuantityMonthly = findViewById(R.id.tv_quantity_month);
        tvQuantityYearly = findViewById(R.id.tv_quantity_year);
        tvQuantityTotal = findViewById(R.id.tv_quantity_total);

        tvIncomeToday = findViewById(R.id.tv_today_profit);
        tvIncomeMonthly = findViewById(R.id.tv_month_profit);
        tvIncomeYearly = findViewById(R.id.tv_year_profit);
        tvIncomeTotal = findViewById(R.id.tv_total_profit);

        btBusyHour = findViewById(R.id.bt_busy_hour);
        btDailyStat = findViewById(R.id.bt_daily_statistics);
        btmonthlyStat = findViewById(R.id.bt_monthly_statistics);
        btYearlyStat = findViewById(R.id.bt_yearly_statistics);
    }


    private void getRating() {
        setActivityLoading(true);

        dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_AVG).getValue() != null)&&(dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_COUNT).getValue() != null))
                {
                    setVariableZero();
                    totGrade = (long) dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_COUNT).getValue();
                    avgGrade = dataSnapshot.child(EAHCONST.RESTAURANT_REVIEW_AVG).getValue(Double.class).floatValue();
                    calculateStats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "DATABASE ERROR " + databaseError);
                setActivityLoading(false);
            }
        });
    }

    private void calculateStats(){
        dbRef.child(EAHCONST.ORDERS_REST_SUBTREE).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                String currentYear = date.split("-")[2];
                String currentMonth = date.split("-")[1];

                bestIncomingOrder = Float.MIN_VALUE;

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String dateOrder = (String) ds.child(EAHCONST.REST_ORDER_DATE).getValue();
                    String timeOrder = (String) ds.child(EAHCONST.REST_ORDER_DELIVERY_TIME).getValue();

                    Timestamp dateTime = dt.stringToDate(dateOrder + " " + timeOrder);
                    if (dateTime != null)
                        calculateBusyHour(dateTime);

                    String orderMonth = dateOrder.split("-")[1];
                    String orderYear = dateOrder.split("-")[2];
                    String totalCostString = (String) ds.child(EAHCONST.REST_ORDER_TOTAL_COST).getValue();
                    float totalCost = Float.parseFloat(totalCostString.split(" ")[0]);
                    int quantityOrder = 0;

                    for (DataSnapshot dishesSnap : ds.child(EAHCONST.REST_ORDER_DISHES_SUBTREE).getChildren()) {
                        quantityOrder += ((Long) dishesSnap.getValue()).intValue();
                    }

                    //Calculate Best Incoming Order
                    if (totalCost > bestIncomingOrder)
                        bestIncomingOrder = totalCost;
                    // Calculate TOTAL Incoming and Quantity
                    profitTotal += totalCost;
                    quantityTotal += quantityOrder;

                    // Calculate DAILY Incoming and Quantity
                    if(dateOrder.equals(date)){
                        profitDay += totalCost;
                        quantityDay += quantityOrder;
                    }

                    // Calculate MONTHLY and YEARLY Incoming and Quantity
                    if (currentYear.equals(orderYear)){
                        profitYear += totalCost;
                        quantityYear += quantityOrder;

                        if(currentMonth.equals(orderMonth)){
                            profitMonth += totalCost;
                            quantityMonth += quantityOrder;
                        }
                    }
                }
                setDataToView();
                setActivityLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "DATABASE ERROR " + databaseError);
                setActivityLoading(false);
            }
        });
    }

    private HashMap<Integer,Integer> createHashmap(){
        HashMap<Integer,Integer> hours = new HashMap<>();
        for (int i= 0 ; i <24; i++){
            hours.put(i, 0);
        }
        return hours;
    }

    private void createListWeek (){

        weekHours = new HashMap<>();
        weekHours.put("Mon",createHashmap());
        weekHours.put("Tue",createHashmap());
        weekHours.put("Wed",createHashmap());
        weekHours.put("Thu",createHashmap());
        weekHours.put("Fri",createHashmap());
        weekHours.put("Sat",createHashmap());
        weekHours.put("Sun",createHashmap());
    }


    private void calculateBusyHour(Timestamp ts) {
        if (weekHours == null)
            createListWeek();
        String DayOfWeek = dt.DayOfTheWeek(ts);
        int hour = dt.getHour(ts);

        HashMap<Integer,Integer> hours = weekHours.get(DayOfWeek);
        hours.put(hour,hours.get(hour)+1);
    }

    private void setDataToView() {
        rbRatingBar.setRating(avgGrade);
        tvRating.setText(""+totGrade+" "+ (totGrade == 1 ? getString(R.string.reviews) : getString(R.string.reviews)));

        tvQuantityToday.setText("" + quantityDay + " Dishes");
        tvQuantityMonthly.setText("" + quantityMonth + " Dishes");
        tvQuantityYearly.setText("" + quantityYear + " Dishes");
        tvQuantityTotal.setText("" + quantityTotal + " Dishes");

        tvIncomeToday.setText(String.format(Locale.US,"%.02f", profitDay) + " €");
        tvIncomeMonthly.setText(String.format(Locale.US,"%.02f", profitMonth) + " €");
        tvIncomeYearly.setText(String.format(Locale.US,"%.02f", profitYear) + " €");
        tvIncomeTotal.setText(String.format(Locale.US,"%.02f", profitTotal) + " €");

        tvBestOrder.setText(String.format(Locale.US,"%.02f", bestIncomingOrder) + " €");
    }
    private void setVariableZero() {
        avgGrade = 0;
        totGrade = 0;

        profitDay = 0;
        profitMonth = 0 ;
        profitYear = 0;
        profitTotal = 0;
        bestIncomingOrder = Float.MIN_VALUE;

        quantityDay = 0;
        quantityMonth = 0;
        quantityYear = 0;
        quantityTotal = 0;
    }


    private void openReviewsActivity() {
        if (totGrade == 0) {
            Utility.showAlertToUser(this, R.string.alert_no_reviews);
            return;
        }

        Intent ratingIntent = new Intent(getApplicationContext(), ReviewsActivity.class);
        ratingIntent.putExtra(ReviewsActivity.RATING_MODE_KEY, ReviewsActivity.RATING_MODE_RESTAURANT);
        ratingIntent.putExtra(ReviewsActivity.RATED_UID_KEY, currentUser.getUid());
        startActivity(ratingIntent);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(QUANTITY_TODAY_KEY, quantityDay);
        outState.putInt(QUANTITY_MONTH_KEY, quantityMonth);
        outState.putInt(QUANTITY_YEAR_KEY, quantityYear);
        outState.putInt(QUANTITY_TOTAL_KEY, quantityTotal);

        outState.putFloat(PROFIT_TODAY_KEY, profitDay);
        outState.putFloat(PROFIT_MONTH_KEY, profitMonth);
        outState.putFloat(PROFIT_YEAR_KEY, profitYear);
        outState.putFloat(PROFIT_TOTAL_KEY, profitTotal);

        outState.putFloat(AVG_GRADE_KEY, avgGrade);
        outState.putLong(COUNT_GRADE_KEY, totGrade);
        outState.putFloat(BEST_WORK_KEY, bestIncomingOrder);
    }
}
