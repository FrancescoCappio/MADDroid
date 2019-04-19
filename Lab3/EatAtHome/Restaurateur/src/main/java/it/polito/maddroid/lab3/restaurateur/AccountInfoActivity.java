package it.polito.maddroid.lab3.restaurateur;


import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.SplashScreenActivity;
import it.polito.maddroid.lab3.common.Utility;


public class AccountInfoActivity extends AppCompatActivity {
    
    private static final String TAG = "AccountInfoActivity";
    
    // general purpose attributes
    private boolean editMode = true;
    private boolean mandatoryAccountInfo = true;
    
    // menu items
    private MenuItem menuEdit;
    private MenuItem menuConfirm;
    
    // views
    private EditText etName;
    private EditText etPhone;
    private EditText etMail;
    private EditText etDescription;
    private EditText etAddress;
    private TextView tvDescriptionCount;
    private ImageView ivPhoto;
    private FloatingActionButton fabPhoto;
    private Button btLogout;
    private ProgressBar pbLoading;
    private TextView tvLoginEmail;
    
    // Firebase attributes
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        if (currentUser == null) {
            // this should not be possible. The user should be logged in to be here
            Utility.showAlertToUser(this, R.string.login_alert);
            
            // launch loginactivity
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            loginIntent.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
            startActivity(loginIntent);
            
            // exit
            finish();
        }
        
        getSupportActionBar().setTitle(R.string.account_info);
        
        getReferencesToViews();
        
        tvLoginEmail.setText(currentUser.getEmail());
        
        setupClickListeners();
        
        manageLaunchIntent();
    }
    
    private void manageLaunchIntent() {
        Intent launchIntent = getIntent();
        
        editMode = launchIntent.getBooleanExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, false);
        
        setEditEnabled(editMode);
        
        mandatoryAccountInfo = launchIntent.getBooleanExtra(EAHCONST.ACCOUNT_INFO_EMPTY, false);
        
        if (!mandatoryAccountInfo) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Utility.showAlertToUser(this, R.string.account_info_alert);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
    
        MenuInflater menuInflater = getMenuInflater();
        
        menuInflater.inflate(R.menu.account_info_menu, menu);
        
        menuEdit = menu.findItem(R.id.menu_edit);
        menuConfirm = menu.findItem(R.id.menu_confirm);
        
        setMenuItemsVisibility();
        
        return true;
    }
    
    private void getReferencesToViews() {
        
        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        etDescription = findViewById(R.id.et_description);
        etMail = findViewById(R.id.et_mail);
        etPhone = findViewById(R.id.et_phone);
        
        fabPhoto = findViewById(R.id.fab_add_photo);
        ivPhoto = findViewById(R.id.iv_avatar);
        
        tvDescriptionCount = findViewById(R.id.tv_description_count);
        
        tvLoginEmail = findViewById(R.id.tv_login_email);
        btLogout = findViewById(R.id.bt_logout);
        
        pbLoading = findViewById(R.id.pb_loading);
        
    }
    
    private void setupClickListeners() {
        btLogout.setOnClickListener(v -> {
            
            //we create an alert dialog to ask user a confirmation before exiting
            
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MainAppTheme_NoActionBar));
            
            builder.setTitle(R.string.logout);
            builder.setMessage(R.string.logout_confirm_request);
            
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                logoutAction();
                dialog.dismiss();
            });
            
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                dialog.dismiss();
            });
            
            // show the dialog
            builder.create().show();
        });
    }
    
    private void logoutAction() {
        mAuth.signOut();
        
        // launch splash screen activity again and then loginactivity
        Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
        
        intent.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        finishAndRemoveTask();
    }
    
    private void setMenuItemsVisibility() {
        menuEdit.setVisible(!editMode);
        menuConfirm.setVisible(editMode);
    }
    
    private void setEditEnabled(boolean enabled) {
        
        if (menuConfirm != null)
            menuConfirm.setVisible(enabled);
        if (menuEdit != null)
            menuEdit.setVisible(!enabled);
    
        etName.setEnabled(enabled);
        etDescription.setEnabled(enabled);
        etMail.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            
            case android.R.id.home:
                //emulate back pressed
                onBackPressed();
                break;
            
            case R.id.menu_confirm:
                manageUserConfirm();
                setEditEnabled(false);
                break;
                
            case R.id.menu_edit:
                setEditEnabled(true);
                break;
        }
        
        return true;
    }
    
    private void manageUserConfirm() {
        
        // first we get info from edittexts
        String restaurantName = etName.getText().toString();
        String restaurantPhone = etPhone.getText().toString();
        String restaurantAddress = etAddress.getText().toString();
        String restaurantEmail = etMail.getText().toString();
        String restaurantDescription = etDescription.getText().toString();
        
        if (restaurantPhone.isEmpty() || restaurantName.isEmpty() || restaurantAddress.isEmpty() || restaurantDescription.isEmpty() || restaurantEmail.isEmpty()) {
            Utility.showAlertToUser(this, R.string.fields_empty_alert);
            return;
        }
        
        pbLoading.setVisibility(View.VISIBLE);
        
        // now add users info in users tree
        EAHCONST.USER_TYPE userType = EAHCONST.USER_TYPE.RESTAURATEUR;
        
        String userEmail = currentUser.getEmail();
        
        String userId = currentUser.getUid();
        
        Map<String,Object> updateMap = new HashMap<>();
    
        // userEmail cannot be null because we permit only registration by email
        assert userEmail != null;
        
        updateMap.put(EAHCONST.generatePath(EAHCONST.USERS_SUB_TREE, userId, EAHCONST.USERS_MAIL), userEmail);
        updateMap.put(EAHCONST.generatePath(EAHCONST.USERS_SUB_TREE, userId, EAHCONST.USERS_TYPE), userType);
        
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_NAME), restaurantName);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_ADDRESS), restaurantAddress);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_DESCRIPTION), restaurantDescription);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_EMAIL), restaurantEmail);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_PHONE), restaurantPhone);
        
        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            
            Log.d(TAG, "Success registering user info");
            pbLoading.setVisibility(View.INVISIBLE);
            Utility.showAlertToUser(this,R.string.notify_save_ok);
            
        }).addOnFailureListener(e -> {
            
            Log.e(TAG, "Database error while registering user info: " + e.getMessage());
            pbLoading.setVisibility(View.INVISIBLE);
            Utility.showAlertToUser(this, R.string.notify_save_ko);
            
        });
        
//        uploadAvatar(restaurantUID);
        
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
