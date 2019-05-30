package it.polito.maddroid.lab3.restaurateur;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BusyHoursActivity extends AppCompatActivity {

    public static final String BUSY_HOURS_KEY = "BUSY_HOURS_KEY";
    private HashMap<String, HashMap<Integer,Integer>> weekHours;

    private BarChart barChart;
    private Spinner spinner;
    private TextView tvPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busy_hours);


        Intent intent = getIntent();
        weekHours = (HashMap<String, HashMap<Integer,Integer>>) intent.getExtras().getSerializable(BUSY_HOURS_KEY);
        
        
        barChart = findViewById(R.id.bar_chart);
        spinner = findViewById(R.id.spinner);
        tvPlaceholder = findViewById(R.id.tv_placeholder);
        
        tvPlaceholder.setVisibility(View.GONE);
        
        // setup bar chart
        barChart.zoomToCenter(3f,1f);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinner.getSelectedItem().toString();
                setupBarChartForDay(selected.substring(0,3));
            }
    
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
        
            }
        });
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.busy_hours);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
    
    private void setupBarChartForDay(String day) {
    
        Map<Integer, Integer> dataPoints = weekHours.get(day);
        
        if (dataPoints == null)
            return;
    
    
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        
        XAxis xAxis = barChart.getXAxis();
        
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter((value, axis) -> {
            int i = (int) value;
            return String.format("%02d", i);
        });
        xAxis.setTextSize(13f);
        
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setSpaceBottom(0f);
        yAxis.setSpaceTop(10f);
        yAxis.setTextSize(13f);
        
        barChart.getAxisRight().setEnabled(false);
    
        List<BarEntry> yVals = new ArrayList<>();
        
        float max = -1;
        float xMax = -1;
        
        List<Integer> xValues = new ArrayList<>();
        xValues.addAll(dataPoints.keySet());
        
        Collections.sort(xValues);
        
        for (int x : xValues) {
            int y = dataPoints.get(x);
            yVals.add(new BarEntry(x, y));
            if (y > max) {
                max = y;
                xMax = x;
            }
        }
        barChart.moveViewToX(xMax);
        
        if (max < 0.5f) {
            tvPlaceholder.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.INVISIBLE);
        } else {
            tvPlaceholder.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);
        }
        
        BarDataSet set1 = new BarDataSet(yVals, "Orders received");
        set1.setColor(ContextCompat.getColor(this, R.color.eah_orange));
        set1.setDrawValues(false);
    
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
    
        BarData data = new BarData(dataSets);
        barChart.setData(data);
        
        barChart.getLegend().setTextSize(16f);
        barChart.invalidate();
    }
}
