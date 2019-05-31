package it.polito.maddroid.lab3.rider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.LineChartActivity;
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
    private TextView kmYearly;
    private TextView kmTotal;
    private TextView incomeToday;
    private TextView bestTravel;
    private TextView incomeMonthly;
    private TextView incomeYearly;
    private TextView incomeTotal;
    private RatingBar ratingBar;
    private TextView tvRating;
    private Button btshowDay;
    private Button btshowMonth;
    private Button btshowYear;
    private static ProgressBar pbLoading;


    private float kmDay;
    private float kmMonth;
    private float kmYear;
    private float allKm;
    private float avgGrade;
    private long totGrade;
    private float bestWork;
    private float profitDay;
    private float profitMonth;
    private float profitYear;
    private float profitTotal;
    private int count = 0;
    private float waitingCount;
    private boolean calculate = true;

    public static final String KM_TODAY_KEY = "KM_TODAY_KEY";
    public static final String KM_MONTH_KEY = "KM_MONTH_KEY";
    public static final String KM_TOTAL_KEY = "KM_TOTAL_KEY";
    public static final String AVG_GRADE_KEY = "AVG_GRADE_KEY";
    public static final String DAY_KEY = "DAY_KEY";
    public static final String MONTH_KEY = "MONTH_KEY";
    public static final String YEAR_KEY = "YEAR_KEY";
    public static final String PROFIT_YEAR_KEY = "YEAR_KEY";
    public static final String KM_YEAR_KEY = "YEAR_KEY";
    public static final String COUNT_GRADE_KEY = "COUNT_GRADE_KEY";
    public static final String PROFIT_TODAY_KEY = "PROFIT_TODAY_KEY";
    public static final String PROFIT_MONTH_KEY = "PROFIT_MONTH_KEY";
    public static final String PROFIT_TOTAL_KEY = "PROFIT_TOTAL_KEY";
    public static final String BEST_WORK_KEY = "BEST_WORK_KEY";
    public static final String ARRAY_INCOME_KEY = "ARRAY_INCOME_KEY";
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
        kmYearly = findViewById(R.id.tv_km_year);
        kmTotal = findViewById(R.id.tv_km_total);
        incomeToday = findViewById(R.id.tv_today_profit);
        incomeMonthly = findViewById(R.id.tv_month_profit);
        incomeYearly = findViewById(R.id.tv_year_profit);
        incomeTotal = findViewById(R.id.tv_total_profit);
        ratingBar = findViewById(R.id.rating_bar);
        tvRating = findViewById(R.id.tv_ratings);
        btshowDay = findViewById(R.id.bt_daily_statistics);
        btshowMonth = findViewById(R.id.bt_monthly_statistics);
        btshowYear = findViewById(R.id.bt_yearly_statistics);


        if (savedInstanceState != null) {

            kmDay = savedInstanceState.getFloat(KM_TODAY_KEY);
            kmMonth = savedInstanceState.getFloat(KM_MONTH_KEY);
            kmYear = savedInstanceState.getFloat(KM_YEAR_KEY);
            allKm = savedInstanceState.getFloat(KM_TOTAL_KEY);
            profitDay = savedInstanceState.getFloat(PROFIT_TODAY_KEY);
            profitMonth = savedInstanceState.getFloat(PROFIT_MONTH_KEY);
            profitYear = savedInstanceState.getFloat(PROFIT_YEAR_KEY);
            profitTotal = savedInstanceState.getFloat(PROFIT_TOTAL_KEY);
            bestWork = savedInstanceState.getFloat(BEST_WORK_KEY);
            avgGrade = savedInstanceState.getFloat(AVG_GRADE_KEY);
            totGrade = savedInstanceState.getLong(COUNT_GRADE_KEY);
            kmToday.setText("" + kmDay);
            kmMonthly.setText("" + kmMonth);
            kmYearly.setText("" + kmYear);
            kmTotal.setText("" + allKm);
            bestTravel.setText(""+bestWork);
            incomeToday.setText("" + profitDay);
            incomeMonthly.setText("" + profitMonth);
            incomeYearly.setText("" + profitYear);
            incomeTotal.setText("" + profitTotal);
            tvRating.setText(""+totGrade);

        } else {
            kmDay = 0;
            kmMonth = 0;
            kmYear = 0;
            allKm = 0;
            profitDay = 0;
            profitMonth = 0;
            profitYear = 0;
            profitTotal = 0;
            bestWork = 0;
            avgGrade = 0.0f;
            totGrade = 0;

            calculateStats();
        }


        tvRating.setOnClickListener(v -> openReviewsActivity());
        btshowDay.setOnClickListener(v -> openLineChart(DAY_KEY) );
        btshowMonth.setOnClickListener(v -> openLineChart(MONTH_KEY) );
        btshowYear.setOnClickListener(v -> openLineChart(YEAR_KEY) );

    }

    private void openLineChart(String timeKey) {
        if(timeKey.equals(DAY_KEY))
        {
            setActivityLoading(true);
            //prendo gli ultimi sette day
            Intent intentRiderDay = new Intent(this, LineChartActivity.class);
            ArrayList<Float> incomeDays = new ArrayList<>();

            dbRef.child(EAHCONST.RIDERS_INCOME_SUB_TREE).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot == null)
                    {

                    }
                    else
                    {

                        for(int i = 6; i >= 0 ; --i)
                        {

                            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                            String parseDate[] = date.split("-");
                            int firstDay = Integer.parseInt(parseDate[0]);
                            firstDay = firstDay - i ;
                            if(firstDay <= 0 )
                            {
                                if(parseDate[1].equals("05")|| parseDate[1].equals("07") ||  parseDate[1].equals("10") || parseDate[1].equals("12"))
                                    firstDay = 30;
                                if(parseDate[1].equals("03"))
                                    firstDay = 28;
                                if(parseDate[1].equals("01")||parseDate[1].equals("02")||parseDate[1].equals("04")||parseDate[1].equals("06")||parseDate[1].equals("08") ||parseDate[1].equals("09")||parseDate[1].equals("11"))
                                    firstDay = 31;

                                if(parseDate[1].equals("01"))
                                    parseDate[2] = String.valueOf(Integer.parseInt(parseDate[2])-1);

                                parseDate[1] = String.valueOf(Integer.parseInt(parseDate[1])-1);
                            }
                            if(dataSnapshot.hasChild(parseDate[2]))
                            {
                                if (dataSnapshot.child(parseDate[2]).hasChild(parseDate[1])){
                                    if(dataSnapshot.child(parseDate[2]).child(parseDate[1]).hasChild(String.valueOf(firstDay)))
                                        incomeDays.add(6-i, dataSnapshot.child(parseDate[2]).child(parseDate[1]).child(String.valueOf(firstDay)).child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue());
                                    else
                                        incomeDays.add(6-i,0.0f);
                                }
                                else
                                {
                                    incomeDays.add(6-i,0.0f);
                                }
                            }
                            else
                                incomeDays.add(6-i, 0.0f);
                        }
                        setActivityLoading(false);
                        intentRiderDay.putExtra(EAHCONST.ARRAY_INCOME_KEY, incomeDays);
                        startActivity(intentRiderDay);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });




        }
        else
        {
            if(timeKey.equals(MONTH_KEY))
            {
                // prendo gli ultimi 30 day
                setActivityLoading(true);
                Intent intentRiderDay = new Intent(this, LineChartActivity.class);
                ArrayList<Float> incomeDays = new ArrayList<>();

                dbRef.child(EAHCONST.RIDERS_INCOME_SUB_TREE).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot == null)
                        {

                        }
                        else
                        {

                            for(int i = 30; i >= 0 ; --i)
                            {

                                String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                                String parseDate[] = date.split("-");
                                int firstDay = Integer.parseInt(parseDate[0]);
                                firstDay = firstDay - i ;
                                if(firstDay <= 0 )
                                {
                                    if(parseDate[1].equals("05")|| parseDate[1].equals("07") ||  parseDate[1].equals("10") || parseDate[1].equals("12"))
                                        firstDay = 30;
                                    if(parseDate[1].equals("03"))
                                        firstDay = 28;
                                    if(parseDate[1].equals("01")||parseDate[1].equals("02")||parseDate[1].equals("04")||parseDate[1].equals("06")||parseDate[1].equals("08") ||parseDate[1].equals("09")||parseDate[1].equals("11"))
                                        firstDay = 31;

                                    if(parseDate[1].equals("01"))
                                        parseDate[2] = String.valueOf(Integer.parseInt(parseDate[2])-1);

                                    parseDate[1] = String.valueOf(Integer.parseInt(parseDate[1])-1);
                                }
                                if(dataSnapshot.hasChild(parseDate[2]))
                                {
                                    if (dataSnapshot.child(parseDate[2]).hasChild(parseDate[1])){
                                        if(dataSnapshot.child(parseDate[2]).child(parseDate[1]).hasChild(String.valueOf(firstDay)))
                                            incomeDays.add(30-i, dataSnapshot.child(parseDate[2]).child(parseDate[1]).child(String.valueOf(firstDay)).child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue());
                                        else
                                            incomeDays.add(30-i,0.0f);
                                    }
                                    else
                                    {
                                        incomeDays.add(30-i,0.0f);
                                    }
                                }
                                else
                                    incomeDays.add(30-i, 0.0f);
                            }
                            setActivityLoading(false);
                            intentRiderDay.putExtra(EAHCONST.ARRAY_INCOME_KEY, incomeDays);
                            startActivity(intentRiderDay);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
            else {
                // prendo gli ultimi 12 month
                setActivityLoading(true);
                Intent intentRiderDay = new Intent(this, LineChartActivity.class);
                ArrayList<Float> incomeDays = new ArrayList<>();

                dbRef.child(EAHCONST.RIDERS_INCOME_SUB_TREE).child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String month;
                        if(dataSnapshot == null)
                        {

                        }
                        else
                        {
                            for(int i = 11; i >= 0 ; --i)
                            {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(new Date());
                                cal.add(Calendar.MONTH, (-i));
                                int mese = cal.get(Calendar.MONTH) + 1 ;
                                if(mese < 10)
                                    month = "0" + mese;
                                else
                                    month = "" + mese;
                                if(dataSnapshot.hasChild(String.valueOf(cal.get(Calendar.YEAR))))
                                {
                                    if(dataSnapshot.child(String.valueOf(cal.get(Calendar.YEAR))).hasChild(month))
                                    {
                                        System.out.println(String.valueOf(cal.get(Calendar.YEAR)));
                                        System.out.println(month);
                                        System.out.println(dataSnapshot.child(String.valueOf(cal.get(Calendar.YEAR))).child(month).child(EAHCONST.RIDER_INCOME).getValue());
                                        incomeDays.add(11-i,dataSnapshot.child(String.valueOf(cal.get(Calendar.YEAR))).child(month).child(EAHCONST.RIDER_INCOME).getValue(Double.class).floatValue());
                                    }
                                    else
                                        incomeDays.add(11-i, 0.0f);
                                }
                                else
                                    incomeDays.add(11-i, 0.0f);
                            }
                            setActivityLoading(false);
                            intentRiderDay.putExtra(EAHCONST.ARRAY_INCOME_KEY, incomeDays);
                            startActivity(intentRiderDay);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }
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
                        computeDayAndMonthAndYear(kmtoCust, kmtoRest, restaurantId, orderId, profit);
                    }

                    kmTotal.setText("" +  String.format("%.02f",allKm));
                    incomeTotal.setText("" +  String.format("%.02f",profitTotal));
                    bestTravel.setText(""+ String.format("%.02f",bestWork)+" km");
                }
                setActivityLoading(false);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "DATABASE ERROR " + databaseError);
            }
        });


    }

    private void computeDayAndMonthAndYear(float kmtoCust, float kmtoRest, String restaurantId, String orderId, float profit) {

        dbRef.child(EAHCONST.ORDERS_REST_SUBTREE).child(restaurantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                String currentM = date.split("-")[1];
                String currentY = date.split("-")[2];
                count--;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(orderId)) {
                        String dateDb = (String) ds.child(EAHCONST.REST_ORDER_DATE).getValue();
                        String monthDb = dateDb.split("-")[1];
                        String yearDb = dateDb.split("-")[2];
                        if (dateDb.equals(date)) {
                            kmDay = kmDay + kmtoCust + kmtoRest;
                            kmMonth = kmMonth + kmtoCust + kmtoRest;
                            kmYear = kmtoCust + kmtoRest + kmYear;
                            profitMonth = profitMonth + profit;
                            profitDay = profitDay + profit;
                            profitYear = profitYear + profit;
                        } else if (currentM.equals(monthDb))
                        {
                            kmMonth = kmMonth + kmtoCust + kmtoRest;
                            profitMonth = profitMonth + profit;
                            kmYear = kmtoCust + kmtoRest + kmYear;
                            profitYear = profitYear + profit;

                        }
                        else if(yearDb.equals(currentY))
                        {
                            kmYear = kmtoCust + kmtoRest + kmYear;
                            profitYear = profitYear + profit;
                        }

                        break;
                    }


                }
                if (count == 0) {
                    kmMonthly.setText("" + String.format("%.02f", kmMonth));
                    kmToday.setText("" + String.format("%.02f", kmDay));
                    kmYearly.setText(""+String.format("%.02f",kmYear));
                    incomeToday.setText("" + String.format("%.02f", profitDay));
                    incomeMonthly.setText("" + String.format("%.02f",profitMonth));
                    incomeYearly.setText("" + String.format("%.02f",profitYear));
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
