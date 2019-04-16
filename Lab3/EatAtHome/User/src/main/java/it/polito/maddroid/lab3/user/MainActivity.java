package it.polito.maddroid.lab3.user;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.polito.maddroid.lab3.common.SplashScreenActivity;


public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(getApplicationContext(), SplashScreenActivity.class);
        startActivity(i);
    }
}
