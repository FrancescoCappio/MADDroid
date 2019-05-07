package it.polito.maddroid.lab3.restaurateur;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.polito.maddroid.lab3.common.Dish;

public class OrderDetailsActivity extends AppCompatActivity {

    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";
    public final static String ORDER_KEY = "ORDER_KEY";

    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;

    private TextView etRider;
    private TextView etCustomer;
    private TextView etTotPrice;
    private TextView etDeliveryAddress;
    private TextView etDeliveryTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_details);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        getReferencesToViews();
        setupClickListeners();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.dish_info);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Resources res = getResources();

        


    }


    private void manageLaunchIntent() {
        Intent launchIntent = getIntent();


    }


/*
    private void setupClickListeners() {

    }
*/

    private void getReferencesToViews() {
            //get references to views
        etRider = findViewById(R.id.tv_rider);
        etCustomer = findViewById(R.id.tv_customer);
        etTotPrice = findViewById(R.id.tv_payment_total);
        etDeliveryAddress = findViewById(R.id.tv_address);
        etDeliveryTime = findViewById(R.id.tv_time);;

    }



}
