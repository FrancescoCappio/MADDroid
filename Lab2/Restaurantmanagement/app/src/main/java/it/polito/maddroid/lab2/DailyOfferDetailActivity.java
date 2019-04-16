package it.polito.maddroid.lab2;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class DailyOfferDetailActivity extends AppCompatActivity {
    private static final String TAG = "DailyOfferDetail";
    
    private MenuItem menuEdit;
    private MenuItem menuSave;
    private MenuItem menuDelete;
    private boolean editMode = false;
    private String pageType;
    
    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";
    public final static String OFFER_ID_KEY = "OFFER_ID_KEY";


    //SAVE_INSTANCE
    public static final String NAME_KEY = "NAME_KEY";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String QUANTITY_KEY = "QUANTITY_KEY";
    public static final String PRICE_KEY = "PRICE_KEY";
    public static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    public static final String CURRENT_OFFER_ID = "CURRENT_OFFER_ID";
    public static final String SAVE_IMAGE_KEY = "SAVE_IMAGE_KEY";
    

    //views
    private EditText etName;
    private EditText etQuantity;
    private EditText etPrice;
    private EditText etDescription;
    private ImageView ivDishPhoto;
    private TextView tvDescriptionCount;
    private FloatingActionButton fabAddPhoto;

    private int DESCRIPTION_MAX_LENGTH;
    
    //content provider authority
    public static final String AUTHORITY = "it.polito.maddroid.lab2.fileprovider";
    
    private int currentOfferId = -1;

    private static int PHOTO_REQUEST_CODE = 128;
    
    DataManager dataManager;
    
    private boolean saveImage = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_offer_detail);

        //get references to views
        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);
        ivDishPhoto = findViewById(R.id.iv_avatar);
        tvDescriptionCount = findViewById(R.id.tv_description_count);
        fabAddPhoto = findViewById(R.id.fab_add_photo);
    
        //get reference to dataManager
        dataManager = DataManager.getInstance(getApplicationContext());
        
        // obtain and check launch mode
        Intent i  = getIntent();
        pageType = i.getStringExtra(PAGE_TYPE_KEY);
        
        if (pageType.equals(MODE_NEW)) {
            currentOfferId = dataManager.getNextDailyOfferId();
            editMode = true;
        } else {
            currentOfferId = i.getIntExtra(OFFER_ID_KEY, -1);
            updateDishImage();
            updateDishData();
        }

        //get values from resources
        Resources res = getResources();
        DESCRIPTION_MAX_LENGTH = res.getInteger(R.integer.description_max_length);
        updateDescriptionCount();
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateDescriptionCount();
            }
        });
    
        // set title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (pageType.equals(MODE_NEW))
                getSupportActionBar().setTitle(R.string.new_dish);
            else
                getSupportActionBar().setTitle(R.string.detail);
            
            // add back button
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        
    }

    private void updateDishData() {
        
        DailyOffer currentDailyoffer = dataManager.getDailyOfferWithId(currentOfferId);
        etName.setText(currentDailyoffer.getName());
        etQuantity.setText(""+currentDailyoffer.getQuantity());
        etPrice.setText(""+currentDailyoffer.getPrice());
        etDescription.setText(currentDailyoffer.getDescription());
        //tvDescriptionCount = findViewById(R.id.tv_description_count);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);

        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_confirm);
        menuDelete = menu.findItem(R.id.menu_delete);

        //enable/disable edit depending on the state
        setEditEnabled(editMode);

        //add on click action to imageview
        ivDishPhoto.setOnClickListener(v -> startActivityToGetImage());
        
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
                DataManager.getInstance(this).deleteDailyOfferWithId(this, currentOfferId);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
                
            case R.id.menu_confirm:
                String Name = etName.getText().toString();
                String Quantity = etQuantity.getText().toString();
                String price = etPrice.getText().toString();
                String description = etDescription .getText().toString();

                if(pageType.equals(MODE_NEW)){
                    
                    DailyOffer offer = new DailyOffer(currentOfferId,Name,description,Integer.parseInt(Quantity), Float.parseFloat(price));
                    
                    // add offer to our list
                    dataManager.addNewDailyOffer(getApplicationContext(), offer);
                    // save image for the offer
                    if (saveImage)
                        dataManager.saveDishImage(getApplicationContext(), currentOfferId);
        
                } else {
                    
                    DailyOffer offer = new DailyOffer(currentOfferId,Name,description,Integer.parseInt(Quantity), Float.parseFloat(price));
    
                    // add offer to our list
                    dataManager.setDailyOfferWithId(getApplicationContext(), offer);
                    // save image for the offer
                    if (saveImage)
                        dataManager.saveDishImage(getApplicationContext(), currentOfferId);
                    
                }
                
                setResult(RESULT_OK, data);
                finish();
                break;
        }
        return true;
    }

    private void setEditEnabled(boolean enabled) {
        editMode = enabled;
        menuEdit.setVisible(!enabled);
        menuSave.setVisible(enabled);

        if (pageType.equals(MODE_NEW)){
            menuDelete.setVisible(!enabled);
        }else {
            menuDelete.setVisible(enabled);
        }


        etDescription.setEnabled(enabled);
        etPrice.setEnabled(enabled);
        etQuantity.setEnabled(enabled);
        etName.setEnabled(enabled);
        ivDishPhoto.setEnabled(enabled);


        if (enabled)
            fabAddPhoto.show();
        else
            fabAddPhoto.hide();
    }

    private void startActivityToGetImage() {
        File myImageFile = DataManager.getDishTmpFile(getApplicationContext());

        final Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(),
                AUTHORITY,
                myImageFile);


        Log.d(TAG, "Uri: " + outputFileUri.toString());

        // Camera
        final List<Intent> totIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            totIntents.add(intent);
        }

        // Filesystem
        final Intent filePickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filePickIntent.setType("image/*");
        final List<ResolveInfo> listGalleries = packageManager.queryIntentActivities(filePickIntent, 0);
        for(ResolveInfo res : listGalleries) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(filePickIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            totIntents.add(intent);
        }

        Intent lastIntent = totIntents.remove(totIntents.size() - 1);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(lastIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, totIntents.toArray(new Parcelable[totIntents.size()]));

        startActivityForResult(chooserIntent, PHOTO_REQUEST_CODE);
    }
    
    private void startActivityToCropImage() {
        File myImageFile = DataManager.getDishTmpFile(getApplicationContext());
    
        final Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(),
                AUTHORITY,
                myImageFile);
    
        CropImage.activity(outputFileUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
        
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }

        if (requestCode == PHOTO_REQUEST_CODE) {
            manageCameraResult(data);
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
            saveImage = true;
            updateDishImage();
        }
    }
    
    private void manageCameraResult(Intent data) {
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
                return;
            }


        } else {
            Log.d(TAG, "Image successfully captured with camera");
        }
        startActivityToCropImage();
    }
    
    private void copyImageToTmpLocation(Uri selectedImageUri) throws IOException {
        InputStream is = getContentResolver().openInputStream(selectedImageUri);
        FileOutputStream fs = new FileOutputStream(DataManager.getDishTmpFile(getApplicationContext()));
        
        if (is == null) {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
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
    
    private void updateDishImage() {
        
        if (currentOfferId == -1) {
            Log.e(TAG, "Cannot show image with null id");
            return;
        }
        
        File img;
        if (!editMode || !saveImage) {
            img = DataManager.getDishImageFile(getApplicationContext(), currentOfferId);
        } else {
            img = DataManager.getDishTmpFile(getApplicationContext());
        }

        if (!img.exists() || !img.isFile()) {
            Log.d(TAG, "Cannot load unexisting file as avatar");
            return;
        }
        
        Glide.with(getApplicationContext())
                .load(img)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivDishPhoto);
    }

    private void updateDescriptionCount() {
        int count = etDescription.getText().length();

        String cnt = count + "/" + DESCRIPTION_MAX_LENGTH;

        tvDescriptionCount.setText(cnt);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String quantity = etQuantity.getText().toString();
        String price = etPrice.getText().toString();
        int currentID = currentOfferId;

        if (!name.isEmpty()) {
            outState.putString(NAME_KEY, name);
        }

        if (!description.isEmpty()) {
            outState.putString(DESCRIPTION_KEY, description);
        }

        if (!quantity.isEmpty()) {
            outState.putString(QUANTITY_KEY, quantity);
        }

        if (!price.isEmpty()) {
            outState.putString(PRICE_KEY, price);
        }

        //save edit mode status
        outState.putBoolean(EDIT_MODE_KEY, editMode);
        //save CURRENT ID
        outState.putInt(CURRENT_OFFER_ID,currentID);
        // if to save image or not
        outState.putBoolean(SAVE_IMAGE_KEY,saveImage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String name = savedInstanceState.getString(NAME_KEY, "");
        String description =savedInstanceState.getString(DESCRIPTION_KEY, "");
        String quantity =savedInstanceState.getString(QUANTITY_KEY, "");
        String price = savedInstanceState.getString(PRICE_KEY, "");

        if (!name.isEmpty()) {
            etName.setText(name);
        }

        if (!description.isEmpty()) {
            etDescription.setText(description);
        }

        if (!quantity.isEmpty()) {
            etQuantity.setText(quantity);
        }

        if (!price.isEmpty()) {
            etPrice.setText(price);
        }

        //restore editMode
        editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
        currentOfferId = savedInstanceState.getInt(CURRENT_OFFER_ID);
        
        //restore info on image save
        saveImage = savedInstanceState.getBoolean(SAVE_IMAGE_KEY);

        updateDishImage();
    }
}
