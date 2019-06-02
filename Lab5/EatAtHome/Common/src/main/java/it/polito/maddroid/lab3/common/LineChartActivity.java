package it.polito.maddroid.lab3.common;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LineChartActivity extends AppCompatActivity {

    private LineChart mChart;
    private List<Float> listIncome;
    private String vectorDate[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        listIncome = (List<Float>) intent.getExtras().getSerializable(EAHCONST.ARRAY_INCOME_KEY);
        setVectorDate();
        setContentView(R.layout.activity_line_chart);
        mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);
        renderData();
    }

    private void setVectorDate() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(listIncome.size() == 7)
        {
            vectorDate = new String[8];
            actionBar.setTitle(R.string.rider_stats_daily);
        }
        else{
            if(listIncome.size() == 12)
            {
                vectorDate = new String[13];
                actionBar.setTitle(R.string.rider_stats_yearly);
            }
            else{
                vectorDate = new String[32];
                actionBar.setTitle(R.string.rider_stats_monthly);
            }
        }
        for(int i = 0, j = listIncome.size()-1 ; i <= listIncome.size(); ++i, --j) {
            if(listIncome.size() != 12)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, (-j));
                int firstDay = cal.get(Calendar.DAY_OF_MONTH);
                if(firstDay < 10)
                    vectorDate[i] = "0"+firstDay +"-";
                else
                    vectorDate[i] = ""+firstDay +"-";
                if(cal.get(Calendar.MONTH)+1 < 10)
                    vectorDate[i] = vectorDate[i]+"0"+(cal.get(Calendar.MONTH)+1);
                else
                    vectorDate[i] = vectorDate[i]+(cal.get(Calendar.MONTH)+1);

            }
            else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.MONTH, (-j));
                int mese = cal.get(Calendar.MONTH) + 1 ;
                if(mese < 10)
                    vectorDate[i] = "0" + mese+"-"+cal.get(Calendar.YEAR);
                else
                    vectorDate[i] = "" + mese+"-"+cal.get(Calendar.YEAR);

            }

        }
    }

    private void renderData() {
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        Description description = new Description();
        description.setText("");
        mChart.setDescription(description);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMaximum((float) listIncome.size());
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return computeDateGraph((int) value);
            }
        });
        xAxis.setAxisMinimum(0f);
        xAxis.setDrawLimitLinesBehindData(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setAxisMaximum(getMax());
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);

        mChart.getAxisRight().setEnabled(false);
        setData();
    }

    private String computeDateGraph(int value) {
        return vectorDate[value];
    }

    private float getMax() {
        float max = 0.0f;
        for (int i = 0; i < listIncome.size(); i++)
            if (max < listIncome.get(i))
                max = listIncome.get(i);

        return max + (15*max)/100;
    }

    private void setData() {

        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < listIncome.size(); i++)
            values.add(new Entry(i, listIncome.get(i)));

        LineDataSet set1;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.setDrawValues(false);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Daily Income (€)");
            set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.DKGRAY);
            set1.setCircleColor(Color.DKGRAY);
            set1.setLineWidth(1f);
            set1.setDrawValues(false);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_orange);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            mChart.setData(data);
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
}

