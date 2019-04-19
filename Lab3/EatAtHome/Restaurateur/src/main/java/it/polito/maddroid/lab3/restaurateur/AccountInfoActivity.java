package it.polito.maddroid.lab3.restaurateur;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.LoginActivity;
import it.polito.maddroid.lab3.common.SplashScreenActivity;
import it.polito.maddroid.lab3.common.Utility;


public class AccountInfoActivity extends AppCompatActivity {
    
    // static final vars
    private static final String TAG = "AccountInfoActivity";
    private static final int PHOTO_REQUEST_CODE = 121;
    
    // general purpose attributes
    private boolean editMode = true;
    private boolean mandatoryAccountInfo = true;
    private int waitingCount = 0;
    boolean photoChanged = false;
    
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
    private TextView tvLoginEmail;
    
    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        
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
        
        if (mandatoryAccountInfo && !photoChanged) {
            Utility.showAlertToUser(this, R.string.image_empty_alert);
            return;
        }
        
        setActivityLoading(true);
        
        // now add users info in users tree
        EAHCONST.USER_TYPE userType = EAHCONST.USER_TYPE.RESTAURATEUR;
        
        String userEmail = currentUser.getEmail();
        
        String userId = currentUser.getUid();
    
        if (photoChanged)
            uploadAvatar(userId);
        
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
            setActivityLoading(false);
            Utility.showAlertToUser(this,R.string.notify_save_ok);
            
        }).addOnFailureListener(e -> {
            
            Log.e(TAG, "Database error while registering user info: " + e.getMessage());
            setActivityLoading(false);
            Utility.showAlertToUser(this, R.string.notify_save_ko);
            
        });
        
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
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error while downloading avatar image: " + exception.getMessage());
                Utility.showAlertToUser(AccountInfoActivity.this, R.string.notify_avatar_download_ko);
                setActivityLoading(false);
            });
    }
}
