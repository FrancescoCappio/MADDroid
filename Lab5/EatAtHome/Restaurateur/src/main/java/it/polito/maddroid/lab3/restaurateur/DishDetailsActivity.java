package it.polito.maddroid.lab3.restaurateur;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Utility;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DishDetailsActivity extends AppCompatActivity {

    private static final String TAG = "DishDetailsActivity";
    private static final int PHOTO_REQUEST_CODE = 121;
    private int DESCRIPTION_MAX_LENGTH;

    private int waitingCount = 0;

    private MenuItem menuEdit;
    private MenuItem menuSave;
    private MenuItem menuDelete;
    private boolean editMode = false;
    private String pageType;

    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";
    public final static String DISH_KEY = "DISH_KEY";

    //views
    private EditText etName;
    private EditText etPrice;
    private EditText etDescription;
    private ImageView ivDishPhoto;
    private TextView tvDescriptionCount;
    private FloatingActionButton fabAddPhoto;

    
    private Dish currentDish;
    private int lastDishId = -2;
    private String userUID;


    boolean photoPresent = false;
    boolean photoChanged = false;


    // Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    private StorageReference mStorageRef;

    //SAVE_INSTANCE
    public static final String NAME_KEY = "NAME_KEY";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String PRICE_KEY = "PRICE_KEY";
    public static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    public static final String SAVE_IMAGE_KEY = "SAVE_IMAGE_KEY";
    public static final String SAVE_CHANGE_IMAGE_KEY = "SAVE_CHANGE_IMAGE_KEY";


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

        DESCRIPTION_MAX_LENGTH = res.getInteger(R.integer.description_max_length);

        manageLaunchIntent();
    
        //do not show the keyboard on activity open
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void getReferencesToViews() {

        //get references to views
        etName = findViewById(R.id.et_dish_name);
        etPrice = findViewById(R.id.et_dish_price);
        etDescription = findViewById(R.id.et_dish_description);
        ivDishPhoto = findViewById(R.id.iv_dish_Image);
        tvDescriptionCount = findViewById(R.id.tv_description_count);
        fabAddPhoto = findViewById(R.id.fab_add_photo);

    }

    private void setupClickListeners() {


        ivDishPhoto.setOnClickListener(v -> {
            Utility.startActivityToGetImage(this, MainActivity.FILE_PROVIDER_AUTHORITY, getImageTmpFile(), PHOTO_REQUEST_CODE);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.save_menu, menu);

        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_confirm);
        menuDelete = menu.findItem(R.id.menu_delete);

        setEditEnabled(editMode);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        Intent data = new Intent();

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_edit:
                setEditEnabled(true);
                break;

            case R.id.menu_delete:

                setActivityLoading(true);
                dbRef.child(EAHCONST.DISHES_SUB_TREE).child(currentUser.getUid()).
                        child(String.valueOf(currentDish.getDishID())).removeValue().addOnSuccessListener(aVoid -> {
                            Utility.showAlertToUser(DishDetailsActivity.this, R.string.Deleted);
                            deleteDishImage(userUID,currentDish.getDishID());
                            setActivityLoading(false);
                        }).addOnFailureListener(e -> {
                            Log.d(TAG, "Error od delete Dish");
                            setActivityLoading(false);
    
                        });

                setResult(RESULT_OK, data);
                finish();
                break;

            case R.id.menu_confirm:

                setActivityLoading(true);

                String name = etName.getText().toString();
                String sPrice = etPrice.getText().toString();
                
                String description = etDescription.getText().toString();

                if (name.isEmpty() || sPrice.isEmpty() || description.isEmpty()) {
                    Utility.showAlertToUser(DishDetailsActivity.this, R.string.fill_fields);
                    setActivityLoading(false);
                    return true;
                }
    
                float price;
                
                try {
                    if (sPrice.contains("€"))
                        sPrice = sPrice.split(" ")[0];
                    price = Float.parseFloat(sPrice);
                } catch (NumberFormatException ex) {
                    Utility.showAlertToUser(DishDetailsActivity.this, R.string.alert_price_not_valid);
                    setActivityLoading(false);
                    return true;
                }
                
                if(pageType.equals(MODE_NEW)){

                    if (!photoPresent) {
                        Utility.showAlertToUser(DishDetailsActivity.this, R.string.insert_image);
                        setActivityLoading(false);
                        return true;
                    }
                    
                    if (lastDishId == -2) {
                        Utility.showAlertToUser(DishDetailsActivity.this, R.string.alert_not_ready_save);
                        setActivityLoading(false);
                        return true;
                    }

                    uploadDishImage(userUID, lastDishId);

                    Map<String,Object> updateMap = new HashMap<>();
                    
                    updateMap.put(EAHCONST.generatePath(EAHCONST.DISHES_SUB_TREE, userUID, String.valueOf(lastDishId), EAHCONST.DISH_NAME), name);
                    updateMap.put(EAHCONST.generatePath(EAHCONST.DISHES_SUB_TREE, userUID, String.valueOf(lastDishId), EAHCONST.DISH_PRICE), price);
                    updateMap.put(EAHCONST.generatePath(EAHCONST.DISHES_SUB_TREE, userUID, String.valueOf(lastDishId), EAHCONST.DISH_DESCRIPTION), description);
                    
                    dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {

                        Log.d(TAG, "Success registering user info");
                        Utility.showAlertToUser(this,R.string.notify_save_ok);
                        setActivityLoading(false);

                    }).addOnFailureListener(e -> {

                        Log.e(TAG, "Database error while registering user info: " + e.getMessage());
                        Utility.showAlertToUser(this, R.string.notify_save_ko);
                        setActivityLoading(false);
                    });

                } else {
                    setActivityLoading(true);

                    Map<String,Object> updateMap = new HashMap<>();

                    updateMap.put(EAHCONST.generatePath(EAHCONST.DISHES_SUB_TREE, userUID, String.valueOf(currentDish.getDishID()), EAHCONST.DISH_NAME), name);
                    updateMap.put(EAHCONST.generatePath(EAHCONST.DISHES_SUB_TREE, userUID, String.valueOf(currentDish.getDishID()), EAHCONST.DISH_PRICE), price);
                    updateMap.put(EAHCONST.generatePath(EAHCONST.DISHES_SUB_TREE, userUID, String.valueOf(currentDish.getDishID()), EAHCONST.DISH_DESCRIPTION), description);
                    
                    dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {

                        Log.d(TAG, "Success updating user info");
                        Utility.showAlertToUser(this,R.string.notify_save_ok);
                        setActivityLoading(false);

                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Database error while registering user info: " + e.getMessage());
                        Utility.showAlertToUser(this, R.string.notify_save_ko);
                        setActivityLoading(false);
                    });

                    if (photoChanged)
                        uploadDishImage(userUID,currentDish.getDishID());

                }

                setResult(RESULT_OK, data);
                finish();
                break;
        }
        return true;
    }


    private void setEditEnabled(boolean enabled) {

        editMode = enabled;
        if (menuEdit != null)
            menuEdit.setVisible(!enabled);
        if (menuSave != null)
            menuSave.setVisible(enabled);

        if (pageType.equals(MODE_NEW)){
            if (menuDelete != null)
                menuDelete.setVisible(!enabled);
        }else {
            if (menuDelete != null)
                menuDelete.setVisible(enabled);
        }

        etDescription.setEnabled(enabled);
        etPrice.setEnabled(enabled);
        etName.setEnabled(enabled);
        ivDishPhoto.setEnabled(enabled);

        if (enabled)
            fabAddPhoto.show();
        else
            fabAddPhoto.hide();
    }

    private void manageLaunchIntent() {

        Intent launchIntent = getIntent();
        pageType = launchIntent.getStringExtra(PAGE_TYPE_KEY);
        userUID = currentUser.getUid();

        if (pageType.equals(MODE_NEW)) {
            getLastDishID();
            editMode = true;
        } else {
            currentDish = (Dish) launchIntent.getSerializableExtra(DISH_KEY);
            writeDishData();
            downloadDishImage(userUID, currentDish.getDishID());
        }
        setEditEnabled(editMode);
    }

    private void getLastDishID() {
        String userUID = currentUser.getUid();

        Query queryRef = dbRef
                .child(EAHCONST.DISHES_SUB_TREE).child(userUID)
                .orderByKey().limitToLast(1);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                
                if (!dataSnapshot.hasChildren())
                    lastDishId = -1;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String dishId = ds.getKey();
                    
                    if (dishId != null)
                        lastDishId = Integer.parseInt(dishId) + 1;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }

    private void writeDishData() {
        etName.setText(currentDish.getName());
        etPrice.setText(String.format(Locale.US,"%.02f", currentDish.getPrice()) + " €");
        etDescription.setText(currentDish.getDescription());
    }
    
    private void downloadDishImage(String UID, int DISH_ID){
        
        File localFile = getImageTmpFile();
        setActivityLoading(true);
        
        StorageReference riversRef = mStorageRef.child("dish_" + UID +"_"+DISH_ID +".jpg");
        
        riversRef.getFile(localFile)
            .addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image downloaded successfully");
                updateImage();
                photoPresent = true;
                setActivityLoading(false);
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error while downloading Image image: " + exception.getMessage());
                Utility.showAlertToUser(DishDetailsActivity.this, R.string.notify_image_download_ko);
                setActivityLoading(false);
            });
    }

    private void uploadDishImage(String UID, int DISH_ID) {
        Uri file = Uri.fromFile(getImageTmpFile());
        StorageReference riversRef = mStorageRef.child("dish_" + UID +"_"+DISH_ID +".jpg");


        riversRef.putFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image uploaded successfully");
                    Utility.showAlertToUser(DishDetailsActivity.this, R.string.notify_image_upload_ok);

                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Could not upload Image: " + exception.getMessage());
                    Utility.showAlertToUser(DishDetailsActivity.this, R.string.notify_image_upload_ko);
                });
    }

    private File getImageTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + "images" + File.separator);
        root.mkdirs();
        final String fname = "DishImage_tmp.jpg";
        return new File(root, fname);
    }

    private void updateImage() {
        File img = getImageTmpFile();

        if (!img.exists() || !img.isFile()) {
            Log.d(TAG, "Cannot load unexisting file as Image");
            return;
        }

        Glide.with(getApplicationContext())
                .load(img)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivDishPhoto);

    }

    private void updateDescriptionCount() {
        int count = etDescription.getText().length();

        String cnt = count + "/" + DESCRIPTION_MAX_LENGTH;

        tvDescriptionCount.setText(cnt);
    }

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
            updateImage();
        }
    }

    private void copyImageToTmpLocation(Uri selectedImageUri) throws IOException {
        InputStream is = getContentResolver().openInputStream(selectedImageUri);
        FileOutputStream fs = new FileOutputStream(getImageTmpFile());

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
        File myImageFile = getImageTmpFile();

        final Uri outputFileUri = FileProvider.getUriForFile(this,
                MainActivity.FILE_PROVIDER_AUTHORITY,
                myImageFile);

        CropImage.activity(outputFileUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    private void deleteDishImage(String userUID, int currentDishId) {

        StorageReference riversRef = mStorageRef.child("dish_" + userUID +"_"+ currentDishId +".jpg");
        riversRef.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Dish image is deleted from FIREBASE"))
                .addOnFailureListener(e -> Log.e(TAG, "there is an error on delete image from FIREBASE"));
    }

    private synchronized void setActivityLoading(boolean loading) {

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String price = etPrice.getText().toString();

        if (!name.isEmpty()) {
            outState.putString(NAME_KEY, name);
        }

        if (!description.isEmpty()) {
            outState.putString(DESCRIPTION_KEY, description);
        }

        if (!price.isEmpty()) {
            outState.putString(PRICE_KEY, price);
        }

        //save edit mode status
        outState.putBoolean(EDIT_MODE_KEY, editMode);
        //save CURRENT dish
        outState.putSerializable(DISH_KEY, currentDish);
        // if to save image or not
        outState.putBoolean(SAVE_IMAGE_KEY,photoPresent);
        // if image change or not
        outState.putBoolean(SAVE_CHANGE_IMAGE_KEY,photoChanged);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String name = savedInstanceState.getString(NAME_KEY, "");
        String description =savedInstanceState.getString(DESCRIPTION_KEY, "");
        String price = savedInstanceState.getString(PRICE_KEY, "");

        if (!name.isEmpty()) {
            etName.setText(name);
        }

        if (!description.isEmpty()) {
            etDescription.setText(description);
        }

        if (!price.isEmpty()) {
            etPrice.setText(price);
        }

        //restore editMode
        editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
        currentDish = (Dish) savedInstanceState.getSerializable(DISH_KEY);

        //restore info on image save
        photoPresent = savedInstanceState.getBoolean(SAVE_IMAGE_KEY);
        photoChanged = savedInstanceState.getBoolean(SAVE_CHANGE_IMAGE_KEY);

        if (photoPresent)
            updateImage();
    }
}
