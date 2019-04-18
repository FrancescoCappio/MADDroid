package it.polito.maddroid.lab3.restaurateur;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.SplashScreenActivity;


public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(getApplicationContext(), SplashScreenActivity.class);
        
        i.putExtra(EAHCONST.LAUNCH_APP_KEY, EAHCONST.LAUNCH_APP_RESTAURATEUR);
        i.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, AccountInfoActivity.class);
        
        startActivity(i);
    }
}
