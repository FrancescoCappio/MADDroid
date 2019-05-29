package it.polito.maddroid.lab3.restaurateur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

public class BusyHoursActivity extends AppCompatActivity {

    public static final String BUSY_HOURS_KEY = "BUSY_HOURS_KEY";
    private HashMap<String, HashMap<Integer,Integer>> weekHours;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busy_hours);


        Intent intent = getIntent();
        weekHours = (HashMap<String, HashMap<Integer,Integer>>) intent.getExtras().getSerializable(BUSY_HOURS_KEY);

    }
}
