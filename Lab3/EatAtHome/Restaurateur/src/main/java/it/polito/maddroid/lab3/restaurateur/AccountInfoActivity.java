package it.polito.maddroid.lab3.restaurateur;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.RestaurantCategory;
import it.polito.maddroid.lab3.common.SplashScreenActivity;
import it.polito.maddroid.lab3.common.Utility;


public class AccountInfoActivity extends AppCompatActivity {
    
    // static const
    private static final String TAG = "AccountInfoActivity";
    private static final int PHOTO_REQUEST_CODE = 121;
    private int DESCRIPTION_MAX_LENGTH;
    
    // general purpose attributes
    private boolean editMode = false;
    private boolean mandatoryAccountInfo = true;
    private int waitingCount = 0;
    private String userId;
    private String timeTableRest;
    boolean photoChanged = false;
    boolean photoPresent = false;
    private List<RestaurantCategory> categories;
    private List<String> previousSelectedCategoriesId;
    private List<String> currentSelectedCategoriesId;
    
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
    private Button btCategories;
    private Button btTimeTable;
    private TextView tvLoginEmail;
    
    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;
    
    // String keys to store instances info
    private static final String NAME_KEY = "NAME_KEY";
    private static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    private static final String PHONE_KEY = "PHONE_KEY";
    private static final String EMAIL_KEY = "EMAIL_KEY";
    private static final String ADDRESS_KEY = "ADDRESS_KEY";
    private static final String PHOTO_PRESENT_KEY = "PHOTO_PRESENT_KEY";
    private static final String PHOTO_CHANGED_KEY = "PHOTO_CHANGED_KEY";
    private static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    private static final String MANDATORY_INFO_KEY = "MANDATORY_INFO_KEY";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        
        downloadCategoriesInfo();
        
        currentSelectedCategoriesId = new ArrayList<>();
        previousSelectedCategoriesId = new ArrayList<>();
        
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
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.account_info);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        getReferencesToViews();
    
        Resources res = getResources();
        DESCRIPTION_MAX_LENGTH = res.getInteger(R.integer.description_max_length);
        
        tvLoginEmail.setText(currentUser.getEmail());
        
        setupClickListeners();
        
        if (savedInstanceState == null)
            manageLaunchIntent();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(NAME_KEY, etName.getText().toString());
        outState.putString(DESCRIPTION_KEY, etDescription.getText().toString());
        outState.putString(PHONE_KEY, etPhone.getText().toString());
        outState.putString(EMAIL_KEY, etMail.getText().toString());
        outState.putString(ADDRESS_KEY, etAddress.getText().toString());
        
        outState.putBoolean(MANDATORY_INFO_KEY, mandatoryAccountInfo);
        outState.putBoolean(EDIT_MODE_KEY, editMode);
        
        outState.putBoolean(PHOTO_PRESENT_KEY, photoPresent);
        outState.putBoolean(PHOTO_CHANGED_KEY, photoChanged);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        etName.setText(savedInstanceState.getString(NAME_KEY, ""));
        etDescription.setText(savedInstanceState.getString(DESCRIPTION_KEY, ""));
        etAddress.setText(savedInstanceState.getString(ADDRESS_KEY, ""));
        etPhone.setText(savedInstanceState.getString(PHONE_KEY, ""));
        etMail.setText(savedInstanceState.getString(EMAIL_KEY, ""));
        
        editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
        mandatoryAccountInfo = savedInstanceState.getBoolean(MANDATORY_INFO_KEY);
        
        photoPresent = savedInstanceState.getBoolean(PHOTO_PRESENT_KEY);
        photoChanged = savedInstanceState.getBoolean(PHOTO_CHANGED_KEY);
        
        if (photoPresent)
            updateAvatarImage();
        
        setEditEnabled(editMode);
    }
    
    private void manageLaunchIntent() {
        Intent launchIntent = getIntent();
        
        editMode = launchIntent.getBooleanExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, false);
        
        setEditEnabled(editMode);
        
        mandatoryAccountInfo = launchIntent.getBooleanExtra(EAHCONST.ACCOUNT_INFO_EMPTY, false);
        
        if (!mandatoryAccountInfo) {
            // the user has probably already inserted some info in the past
            retrieveRestaurantsInfo();
            downloadAvatar(currentUser.getUid());
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
        
        setEditEnabled(editMode);
        
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
        btCategories = findViewById(R.id.bt_restaurant_categories);
        btTimeTable = findViewById(R.id.bt_timetable);
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
        
        ivPhoto.setOnClickListener(v -> {
            Utility.startActivityToGetImage(this, MainActivity.FILE_PROVIDER_AUTHORITY, getAvatarTmpFile(), PHOTO_REQUEST_CODE);
        });
        
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateDescriptionCount();
            }
        });

        btCategories.setOnClickListener(v ->
                {
                    if (categories.size() == 0) {
                        Utility.showAlertToUser(this, R.string.no_category_alert);
                        return;
                    }
                    
                    String [] multiChoiceItems = new String[categories.size()];
                    boolean[] checkedItems = new boolean[categories.size()];
                    for (int i = 0; i < categories.size();i++)
                    {
                        multiChoiceItems[i] = categories.get(i).getName();
                        checkedItems[i] = currentSelectedCategoriesId.contains(categories.get(i).getId());
                    }
                    
                    new AlertDialog.Builder(this)
                            .setTitle("Select your restaurant categories")
                            .setMultiChoiceItems(multiChoiceItems, checkedItems, (dialog, index, isChecked) -> {
                                if(isChecked){
                                    if (!currentSelectedCategoriesId.contains(categories.get(index).getId()))
                                        currentSelectedCategoriesId.add(categories.get(index).getId());
                                }
                                else{
                                    if (currentSelectedCategoriesId.contains(categories.get(index).getId()))
                                        currentSelectedCategoriesId.remove(categories.get(index).getId());
                                }
                            })
                            .setPositiveButton(R.string.action_confirm, null)
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                }
        );

    btTimeTable.setOnClickListener(v->
            {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.timetable, null);
                TextView days[] = new  TextView[7];
                TextView openFirst[] = new  TextView[7];
                TextView openSecond[] = new  TextView[7];
                TextView closeFirst[] = new  TextView[7];
                TextView closeSecond[] = new  TextView[7];
                Switch swDays[] = new Switch[7];
                Switch swDaysCont[] = new Switch[7];

                days[0] = alertLayout.findViewById(R.id.tv_day_0);
                days[1] = alertLayout.findViewById(R.id.tv_day_1);
                days[2] = alertLayout.findViewById(R.id.tv_day_2);
                days[3] = alertLayout.findViewById(R.id.tv_day_3);
                days[4] = alertLayout.findViewById(R.id.tv_day_4);
                days[5] = alertLayout.findViewById(R.id.tv_day_5);
                days[6] = alertLayout.findViewById(R.id.tv_day_6);

                swDays[0] = alertLayout.findViewById(R.id.sw_day_0);
                swDays[1] = alertLayout.findViewById(R.id.sw_day_1);
                swDays[2] = alertLayout.findViewById(R.id.sw_day_2);
                swDays[3] = alertLayout.findViewById(R.id.sw_day_3);
                swDays[4] = alertLayout.findViewById(R.id.sw_day_4);
                swDays[5] = alertLayout.findViewById(R.id.sw_day_5);
                swDays[6] = alertLayout.findViewById(R.id.sw_day_6);

                swDaysCont[0] = alertLayout.findViewById(R.id.sw_continued_day_0);
                swDaysCont[1] = alertLayout.findViewById(R.id.sw_continued_day_1);
                swDaysCont[2] = alertLayout.findViewById(R.id.sw_continued_day_2);
                swDaysCont[3] = alertLayout.findViewById(R.id.sw_continued_day_3);
                swDaysCont[4] = alertLayout.findViewById(R.id.sw_continued_day_4);
                swDaysCont[5] = alertLayout.findViewById(R.id.sw_continued_day_5);
                swDaysCont[6] = alertLayout.findViewById(R.id.sw_continued_day_6);

                openFirst[0] = alertLayout.findViewById(R.id.tv_openL_time0);
                closeFirst[0] = alertLayout.findViewById(R.id.tv_closeL_time0);
                openSecond[0] = alertLayout.findViewById(R.id.tv_openD_time0);
                closeSecond[0] = alertLayout.findViewById(R.id.tv_closeD_time0);
                openFirst[1] = alertLayout.findViewById(R.id.tv_openL_time1);
                closeFirst[1] = alertLayout.findViewById(R.id.tv_closeL_time1);
                openSecond[1] = alertLayout.findViewById(R.id.tv_openD_time1);
                closeSecond[1] = alertLayout.findViewById(R.id.tv_closeD_time1);
                openFirst[2] = alertLayout.findViewById(R.id.tv_openL_time2);
                closeFirst[2] = alertLayout.findViewById(R.id.tv_closeL_time2);
                openSecond[2] = alertLayout.findViewById(R.id.tv_openD_time2);
                closeSecond[2] = alertLayout.findViewById(R.id.tv_closeD_time2);
                openFirst[3] = alertLayout.findViewById(R.id.tv_openL_time3);
                closeFirst[3] = alertLayout.findViewById(R.id.tv_closeL_time3);
                openSecond[3] = alertLayout.findViewById(R.id.tv_openD_time3);
                closeSecond[3] = alertLayout.findViewById(R.id.tv_closeD_time3);
                openFirst[4] = alertLayout.findViewById(R.id.tv_openL_time4);
                closeFirst[4] = alertLayout.findViewById(R.id.tv_closeL_time4);
                openSecond[4] = alertLayout.findViewById(R.id.tv_openD_time4);
                closeSecond[4] = alertLayout.findViewById(R.id.tv_closeD_time4);
                openFirst[5] = alertLayout.findViewById(R.id.tv_openL_time5);
                closeFirst[5] = alertLayout.findViewById(R.id.tv_closeL_time5);
                openSecond[5] = alertLayout.findViewById(R.id.tv_openD_time5);
                closeSecond[5] = alertLayout.findViewById(R.id.tv_closeD_time5);
                openFirst[6] = alertLayout.findViewById(R.id.tv_openL_time6);
                closeFirst[6] = alertLayout.findViewById(R.id.tv_closeL_time6);
                openSecond[6] = alertLayout.findViewById(R.id.tv_openD_time6);
                closeSecond[6] = alertLayout.findViewById(R.id.tv_closeD_time6);

                swDays[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[0].setEnabled(true);
                            openSecond[0].setEnabled(true);
                            closeFirst[0].setEnabled(true);
                            closeSecond[0].setEnabled(true);
                            swDaysCont[0].setEnabled(true);

                        }
                        else {

                            openFirst[0].setEnabled(false);
                            openSecond[0].setEnabled(false);
                            closeFirst[0].setEnabled(false);
                            closeSecond[0].setEnabled(false);
                            swDaysCont[0].setEnabled(false);

                        }
                    }
                });

                openFirst[0].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[0].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[0].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[0].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDays[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[1].setEnabled(true);
                            openSecond[1].setEnabled(true);
                            closeFirst[1].setEnabled(true);
                            closeSecond[1].setEnabled(true);
                            swDaysCont[1].setEnabled(true);


                        }
                        else {

                            openFirst[1].setEnabled(false);
                            openSecond[1].setEnabled(false);
                            closeFirst[1].setEnabled(false);
                            closeSecond[1].setEnabled(false);
                            swDaysCont[1].setEnabled(false);

                        }
                    }
                });

                openFirst[1].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[1].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[1].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[1].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDays[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[2].setEnabled(true);
                            openSecond[2].setEnabled(true);
                            closeFirst[2].setEnabled(true);
                            closeSecond[2].setEnabled(true);
                            swDaysCont[2].setEnabled(true);
                        }
                        else {
                            //TODO resettare il tempo? o lasciarlo cosi?
                            openFirst[2].setEnabled(false);
                            openSecond[2].setEnabled(false);
                            closeFirst[2].setEnabled(false);
                            closeSecond[2].setEnabled(false);
                            swDaysCont[2].setEnabled(false);

                        }
                    }
                });

                openFirst[2].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[2].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[2].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[2].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDays[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[3].setEnabled(true);
                            openSecond[3].setEnabled(true);
                            closeFirst[3].setEnabled(true);
                            closeSecond[3].setEnabled(true);
                            swDaysCont[3].setEnabled(true);

                        }
                        else {

                            openFirst[3].setEnabled(false);
                            openSecond[3].setEnabled(false);
                            closeFirst[3].setEnabled(false);
                            closeSecond[3].setEnabled(false);
                            swDaysCont[3].setEnabled(false);

                        }
                    }
                });

                openFirst[3].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[3].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[3].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[3].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDays[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[4].setEnabled(true);
                            openSecond[4].setEnabled(true);
                            closeFirst[4].setEnabled(true);
                            closeSecond[4].setEnabled(true);
                            swDaysCont[4].setEnabled(true);

                        }
                        else {

                            openFirst[4].setEnabled(false);
                            openSecond[4].setEnabled(false);
                            closeFirst[4].setEnabled(false);
                            closeSecond[4].setEnabled(false);
                            swDaysCont[4].setEnabled(false);

                        }
                    }
                });

                openFirst[4].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[4].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[4].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[4].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDays[5].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[5].setEnabled(true);
                            openSecond[5].setEnabled(true);
                            closeFirst[5].setEnabled(true);
                            closeSecond[5].setEnabled(true);
                            swDaysCont[5].setEnabled(true);

                        }
                        else {

                            openFirst[5].setEnabled(false);
                            openSecond[5].setEnabled(false);
                            closeFirst[5].setEnabled(false);
                            closeSecond[5].setEnabled(false);
                            swDaysCont[5].setEnabled(false);

                        }
                    }
                });

                openFirst[5].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[5].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[5].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[5].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDays[6].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            openFirst[6].setEnabled(true);
                            openSecond[6].setEnabled(true);
                            closeFirst[6].setEnabled(true);
                            closeSecond[6].setEnabled(true);
                            swDaysCont[6].setEnabled(true);

                        }
                        else {

                            openFirst[6].setEnabled(false);
                            openSecond[6].setEnabled(false);
                            closeFirst[6].setEnabled(false);
                            closeSecond[6].setEnabled(false);
                            swDaysCont[6].setEnabled(false);

                        }
                    }
                });

                openFirst[6].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                openSecond[6].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeFirst[6].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );
                closeSecond[6].setOnClickListener(tv-> showTimePickerDialog((TextView) tv) );

                swDaysCont[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[0].setEnabled(false);
                            closeSecond[0].setEnabled(false);
                        } else {
                            openSecond[0].setEnabled(true);
                            closeSecond[0].setEnabled(true);
                        }
                    }
                });

                swDaysCont[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[1].setEnabled(false);
                            closeSecond[1].setEnabled(false);
                        } else {
                             openSecond[1].setEnabled(true);
                            closeSecond[1].setEnabled(true);
                        }
                    }
                });

                swDaysCont[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[2].setEnabled(false);
                            closeSecond[2].setEnabled(false);
                        } else {
                            openSecond[2].setEnabled(true);
                            closeSecond[2].setEnabled(true);
                        }
                    }
                });

                swDaysCont[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[3].setEnabled(false);
                            closeSecond[3].setEnabled(false);
                        } else {
                            openSecond[3].setEnabled(true);
                            closeSecond[3].setEnabled(true);
                        }
                    }
                });

                swDaysCont[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[4].setEnabled(false);
                            closeSecond[4].setEnabled(false);
                        } else {
                            openSecond[4].setEnabled(true);
                            closeSecond[4].setEnabled(true);
                        }
                    }
                });

                swDaysCont[5].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[5].setEnabled(false);
                            closeSecond[5].setEnabled(false);
                        } else {
                            openSecond[5].setEnabled(true);
                            closeSecond[5].setEnabled(true);
                        }
                    }
                });

                swDaysCont[6].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            openSecond[6].setEnabled(false);
                            closeSecond[6].setEnabled(false);
                        } else {
                            openSecond[6].setEnabled(true);
                            closeSecond[6].setEnabled(true);
                        }
                    }
                });


                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Restaurant Time Table");
                // this is set the view from XML inside AlertDialog
                alert.setView(alertLayout);
                // disallow cancel of AlertDialog on click of back button and outside touch
                alert.setCancelable(false);
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = new String();
                        for (int i = 0 ; i < 7; i++){
                            if(swDays[i].isChecked())
                            {
                                s = s + "Day"+i+",";

                                if(swDaysCont[i].isChecked())
                                {
                                   /* if(! control(openFirst[i], closeFirst[i]))
                                    {
                                        Toast.makeText(getBaseContext(), " the closing time MUST be after Opening time", Toast.LENGTH_SHORT).show();
                                        return;
                                    }*/
                                    s = s + openFirst[i].getText()+"_";
                                    s = s + closeFirst[i].getText()+";";

                                }
                                else
                                {
                                   /* if((! control(openFirst[i], closeFirst[i]))&&(!control(openSecond[i],closeSecond[i])))
                                    {
                                        Toast.makeText(getBaseContext(), " the closing time MUST be after Opening time", Toast.LENGTH_SHORT).show();
                                        return;
                                    }*/
                                    s = s + openFirst[i].getText()+"_";
                                    s = s + closeFirst[i].getText()+",";
                                    s = s + openSecond[i].getText()+"_";
                                    s = s + closeSecond[i].getText()+";";

                                }
                            }
                            else
                            {
                                s = s + "Day"+i+" closed;";
                            }
                        }
                        timeTableRest = s;
                        
                        //TODO: remove this
                        Toast.makeText(getBaseContext(), " Confirm pressed, timetable saved in DB", Toast.LENGTH_SHORT).show();
                        String s1 = Utility.extractTimeTable(s);
                        System.out.println(s1);
                        System.out.println(Utility.openRestaurant(s,0,10,00));
                        System.out.println(Utility.openRestaurant(s,2,10,30));
                        System.out.println(Utility.openRestaurant(s,5,19,45));
                        System.out.println(Utility.openRestaurant(s,4,01,10));
                        System.out.println(Utility.openRestaurant(s,4,17,30));

                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();

            }
            );

    }

    /*private boolean control(TextView textView, TextView textView1) {
        String time1 = (String) textView.getText();
        String time2 = (String) textView1.getText();

        int hour1, hour2;
        int min1, min2;
        hour1 = Integer.parseInt(time1.substring(0,2));
        hour2 = Integer.parseInt(time2.substring(0,2));
        min1 = Integer.parseInt(time1.substring(3,5));
        min2 = Integer.parseInt(time2.substring(3,5));

        if((hour1 > hour2 && hour)
    }*/

    private void logoutAction() {
        mAuth.signOut();

        // launch splash screen activity again and then loginactivity
        Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);

        intent.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finishAndRemoveTask();
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
        btCategories.setEnabled(enabled);
        btTimeTable.setEnabled(enabled);
        
        if (enabled)
            fabPhoto.show();
        else
            fabPhoto.hide();
        
        ivPhoto.setEnabled(enabled);
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
                if (manageUserConfirm()) {
                    editMode = false;
                    setEditEnabled(false);
                }
                break;
                
            case R.id.menu_edit:
                editMode = true;
                setEditEnabled(true);
                break;
        }
        
        return true;
    }
    
    private boolean manageUserConfirm() {
        
        // first we get info from edittexts
        String restaurantName = etName.getText().toString();
        String restaurantPhone = etPhone.getText().toString();
        String restaurantAddress = etAddress.getText().toString();
        String restaurantEmail = etMail.getText().toString();
        String restaurantDescription = etDescription.getText().toString();
        
        
        // now we check if at leat one category has been selected
        boolean categorySelected = !currentSelectedCategoriesId.isEmpty();
        
        if (restaurantPhone.isEmpty() || restaurantName.isEmpty() || restaurantAddress.isEmpty() || restaurantDescription.isEmpty() || restaurantEmail.isEmpty()) {
            Utility.showAlertToUser(this, R.string.fields_empty_alert);
            return false;
        }
        
        // we also check if the photo has been set
        if (mandatoryAccountInfo && !photoChanged) {
            Utility.showAlertToUser(this, R.string.image_empty_alert);
            return false;
        }
        
        if (!categorySelected) {
            Utility.showAlertToUser(this, R.string.no_category_alert);
            return false;
        }

        if (timeTableRest.isEmpty()){
            Utility.showAlertToUser(this, R.string.no_timetable_alert);
            return false;
        }
        
        setActivityLoading(true);
        
        // now add users info in users tree
        EAHCONST.USER_TYPE userType = EAHCONST.USER_TYPE.RESTAURATEUR;
        
        String userEmail = currentUser.getEmail();
        
        userId = currentUser.getUid();
    
        Map<String,Object> updateMap = new HashMap<>();
        
        // check categories to be removed
        for (String id : previousSelectedCategoriesId) {
            if (!currentSelectedCategoriesId.contains(id))
                updateMap.put(EAHCONST.generatePath(EAHCONST.CATEGORIES_ASSOCIATIONS_SUB_TREE, id, userId), null);
        }
        
        // check categories to be added
        for (String id : currentSelectedCategoriesId) {
            if (!previousSelectedCategoriesId.contains(id))
                updateMap.put(EAHCONST.generatePath(EAHCONST.CATEGORIES_ASSOCIATIONS_SUB_TREE, id, userId), userId);
        }
        
        if (photoChanged)
            uploadAvatar(userId);
        
        // userEmail cannot be null because we permit only registration by email
        assert userEmail != null;
        
        updateMap.put(EAHCONST.generatePath(EAHCONST.USERS_SUB_TREE, userId, EAHCONST.USERS_MAIL), userEmail);
        updateMap.put(EAHCONST.generatePath(EAHCONST.USERS_SUB_TREE, userId, EAHCONST.USERS_TYPE), userType);
        
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_NAME), restaurantName);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_ADDRESS), restaurantAddress);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_DESCRIPTION), restaurantDescription);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_EMAIL), restaurantEmail);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_PHONE), restaurantPhone);
        
        // put timetable inside both restaurants and timetables subtree
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_TIMETABLE), timeTableRest);
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_TIMETABLES_SUB_TREE, userId), timeTableRest);
    
        //produce new categories string
        StringBuilder sb = new StringBuilder();
        for (String id : currentSelectedCategoriesId)
            sb.append(id).append(";");
    
        updateMap.put(EAHCONST.generatePath(EAHCONST.RESTAURANTS_SUB_TREE, userId, EAHCONST.RESTAURANT_CATEGORIES), sb.toString());
        

        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {
            
            Log.d(TAG, "Success registering user info");
            setActivityLoading(false);
            
            //now align the 2 categories lists
            previousSelectedCategoriesId.clear();
            previousSelectedCategoriesId.addAll(currentSelectedCategoriesId);
            
            Utility.showAlertToUser(this,R.string.notify_save_ok);
            
        }).addOnFailureListener(e -> {
            
            Log.e(TAG, "Database error while registering user info: " + e.getMessage());
            setActivityLoading(false);
            Utility.showAlertToUser(this, R.string.notify_save_ko);
            
        });
        return true;
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    
    private void retrieveRestaurantsInfo() {
        // first we check if the user has already inserted info in the past, in this case we load them
        
        setActivityLoading(true);
        
        String userId = currentUser.getUid();
        
        // we want to read but we are not interested in updates
        dbRef.child(EAHCONST.RESTAURANTS_SUB_TREE).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                
                String restaurantName = (String) dataSnapshot.child(EAHCONST.RESTAURANT_NAME).getValue();
                String restaurantAddress = (String) dataSnapshot.child(EAHCONST.RESTAURANT_ADDRESS).getValue();
                String restaurantEmail = (String) dataSnapshot.child(EAHCONST.RESTAURANT_EMAIL).getValue();
                String restaurantPhone = (String) dataSnapshot.child(EAHCONST.RESTAURANT_PHONE).getValue();
                String restaurantDescription = (String) dataSnapshot.child(EAHCONST.RESTAURANT_DESCRIPTION).getValue();
                
                String categoriesRest = (String) dataSnapshot.child(EAHCONST.RESTAURANT_CATEGORIES).getValue();
                
                if (restaurantName != null) {
                    etName.setText(restaurantName);
                }
                
                if (restaurantAddress != null) {
                    etAddress.setText(restaurantAddress);
                }
                
                if (restaurantEmail != null) {
                    etMail.setText(restaurantEmail);
                }
                
                if (restaurantPhone != null) {
                    etPhone.setText(restaurantPhone);
                }
                
                if (restaurantDescription != null) {
                    etDescription.setText(restaurantDescription);
                }
                
                if (categoriesRest != null) {
                    for (String id : categoriesRest.split(";")) {
                        currentSelectedCategoriesId.add(id);
                        previousSelectedCategoriesId.add(id);
                    }
                }
                
                setActivityLoading(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setActivityLoading(false);
            }
        });
    }
    
    private synchronized void setActivityLoading(boolean loading) {
        // this method is necessary to show the user when the activity is doing a network operation
        // as downloading data or uploading data
        // how to use: call with loading = true to notify that a new transmission has been started
        // call with loading = false to notify end of transmission
        
        ProgressBar pbLoading = findViewById(R.id.pb_loading);
        if (loading) {
            if (waitingCount == 0)
                pbLoading.setVisibility(View.VISIBLE);
            waitingCount++;
        } else {
            waitingCount--;
            if (waitingCount == 0)
                pbLoading.setVisibility(View.INVISIBLE);
        }
    }
    
    private File getAvatarTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + "images" + File.separator);
        root.mkdirs();
        final String fname = "RestaurantAvatar_tmp.jpg";
        return new File(root, fname);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }
    
        if (requestCode == PHOTO_REQUEST_CODE) {
            final boolean isCamera;
            if (data == null || data.getData() == null) {
                isCamera = true;
            } else {
                isCamera = false;
            }
        
            Uri selectedImageUri;
            if (!isCamera) {
                selectedImageUri = data.getData();
            
                if (selectedImageUri == null) {
                    Log.e(TAG, "Selectedimageuri is null");
                    return;
                }
                Log.d(TAG, "Result URI: " + selectedImageUri.toString());
            
                try {
                    // we need to copy the image into our directory, we try 2 methods to do this:
                    // 1. if possible we copy manually with input stream and output stream so that the exif interface is not lost
                    // 2. if we can't access the exif interface then we try to decode the bitmap and we encode it again in our directory
                    copyImageToTmpLocation(selectedImageUri);
                
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read bitmap");
                    e.printStackTrace();
                }
            
            
            } else {
                Log.d(TAG, "Image successfully captured with camera");
                //update shown image
            }
            startActivityToCropImage();
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();
            Log.d(TAG, "Result uri: " + resultUri);
            try {
                copyImageToTmpLocation(resultUri);
            } catch (IOException e) {
                Log.e(TAG, "Cannot read bitmap");
                e.printStackTrace();
            }
            photoChanged = true;
            photoPresent = true;
            updateAvatarImage();
        }
    }
    
    private void updateAvatarImage() {
        File img = getAvatarTmpFile();
    
        if (!img.exists() || !img.isFile()) {
            Log.d(TAG, "Cannot load unexisting file as avatar");
            return;
        }
    
        Glide.with(getApplicationContext())
                .load(img)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivPhoto);
        
    }
    
    private void copyImageToTmpLocation(Uri selectedImageUri) throws IOException {
        InputStream is = getContentResolver().openInputStream(selectedImageUri);
        FileOutputStream fs = new FileOutputStream(getAvatarTmpFile());
        
        if (is == null) {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fs);
        } else {
            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = is.read(buffer);
                if (bytesRead == -1)
                    break;
                fs.write(buffer, 0, bytesRead);
            }
        }
        
        fs.flush();
        fs.close();
    }
    
    private void startActivityToCropImage() {
        File myImageFile = getAvatarTmpFile();
        
        final Uri outputFileUri = FileProvider.getUriForFile(this,
                MainActivity.FILE_PROVIDER_AUTHORITY,
                myImageFile);
        
        CropImage.activity(outputFileUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
        
    }
    
    private void uploadAvatar(String UID) {
        Uri file = Uri.fromFile(getAvatarTmpFile());
        StorageReference riversRef = mStorageRef.child("avatar_" + UID+".jpg");
        
        setActivityLoading(true);
        
        riversRef.putFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Avatar uploaded successfully");
                    Utility.showAlertToUser(AccountInfoActivity.this, R.string.notify_avatar_upload_ok);
                    setActivityLoading(false);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Could not upload avatar: " + exception.getMessage());
                    Utility.showAlertToUser(AccountInfoActivity.this, R.string.notify_avatar_upload_ko);
                    setActivityLoading(false);
                });
    }
    
    private void downloadAvatar(String UID) {
        File localFile = getAvatarTmpFile();
        
        StorageReference riversRef = mStorageRef.child("avatar_" + UID +".jpg");
        
        setActivityLoading(true);
    
        riversRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Avatar downloaded successfully");
                    updateAvatarImage();
                    setActivityLoading(false);
                    photoPresent = true;
                }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error while downloading avatar image: " + exception.getMessage());
            Utility.showAlertToUser(AccountInfoActivity.this, R.string.notify_avatar_download_ko);
            setActivityLoading(false);
        });
    }
    
    private void updateDescriptionCount() {
        int count = etDescription.getText().length();
        
        String cnt = count + "/" + DESCRIPTION_MAX_LENGTH;
        
        tvDescriptionCount.setText(cnt);
    }

    private void downloadCategoriesInfo() {
        
        Query queryRef = dbRef
                .child(EAHCONST.CATEGORIES_SUB_TREE)
                .orderByChild(EAHCONST.CATEGORIES_NAME);
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                
                List<RestaurantCategory> tmpList = new ArrayList<>();
                
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    String catId = ds.getKey();
                    String catName = (String) ds.child(EAHCONST.CATEGORIES_NAME).getValue();
                    
                    tmpList.add(new RestaurantCategory(catId, catName));
                }
                
                categories = tmpList;
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }

    public void showTimePickerDialog(TextView tv) {
        DialogFragment newFragment = new it.polito.maddroid.lab2.TimePickerFragment(tv);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
}
