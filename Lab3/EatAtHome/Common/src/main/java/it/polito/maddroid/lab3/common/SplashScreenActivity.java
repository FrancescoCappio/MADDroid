package it.polito.maddroid.lab3.common;


import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class SplashScreenActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        new Handler().postDelayed(() -> {
            Intent i = getIntent();
            
            Intent launchIntent = new Intent(getApplicationContext(), LoginActivity.class);
            // transfer extras from Intent i
            launchIntent.putExtras(i);
            startActivity(launchIntent);
            
            finish();
    
        }, 1500);
    }
}
