package it.polito.maddroid.lab3.common;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomerDetailActivity extends AppCompatActivity {
    
    public static final String TAG = "CustomerDetailActivity";
    public static final String CUSTOMER_KEY = "CUSTOMER_KEY";
    
    private Customer currentCustomer;
    
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvAddressNotes;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvDescription;
    private ImageView ivAvatar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        
        Intent intent = getIntent();
        
        if (intent.getSerializableExtra(CUSTOMER_KEY) == null) {
            Log.e(TAG, "Cannot open customer detail activity for null customer");
            finish();
            return;
        }
        
        currentCustomer = (Customer) intent.getSerializableExtra(CUSTOMER_KEY);
    
        ActionBar actionBar = getSupportActionBar();
        
        if (actionBar != null) {
            actionBar.setTitle(R.string.customer_info);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        getReferencesToViews();
        
        setDataToViews();
        
        setOnClickListeners();
    }
    
    private void getReferencesToViews() {
        tvName = findViewById(R.id.tv_name);
        tvAddress = findViewById(R.id.tv_address);
        tvAddressNotes = findViewById(R.id.tv_address_notes);
        tvDescription = findViewById(R.id.tv_description);
        tvEmail = findViewById(R.id.tv_email);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
    }
    
    private void setDataToViews() {
        tvName.setText(currentCustomer.getName());
        tvPhoneNumber.setText(currentCustomer.getPhoneNumber());
        tvEmail.setText(currentCustomer.getEmailAddress());
        tvDescription.setText(currentCustomer.getDescription());
        tvAddress.setText(currentCustomer.getEmailAddress());
        tvAddressNotes.setText(currentCustomer.getAddressNotes());
    }
    
    private void setOnClickListeners() {
        tvPhoneNumber.setOnClickListener(v -> {
            String phone = currentCustomer.getPhoneNumber();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
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
}
