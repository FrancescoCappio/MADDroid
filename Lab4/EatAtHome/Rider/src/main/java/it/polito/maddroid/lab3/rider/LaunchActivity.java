package it.polito.maddroid.lab3.rider;


import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.SplashScreenActivity;


public class LaunchActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(getApplicationContext(), SplashScreenActivity.class);
    
        i.putExtra(EAHCONST.LAUNCH_APP_KEY, EAHCONST.LAUNCH_APP_RIDER);
        i.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
        startActivity(i);
        
        finish();
    }
}
