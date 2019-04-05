package it.polito.maddroid.lab2;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;


public class DailyOfferDetailActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_offer_detail_activty);
        
        
        getSupportActionBar().setTitle("New Dish");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }
}
