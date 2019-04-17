package it.polito.maddroid.lab2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static it.polito.maddroid.lab2.DailyOfferDetailActivity.AUTHORITY;
import static it.polito.maddroid.lab2.DataManager.IMAGES_DIR;

public class RestaurantFragment extends Fragment {

    private static final String TAG = "RestaurantFragment";

    private static int PHOTO_REQUEST_CODE = 128;
    //shared prefs and constants
    private SharedPreferences sharedPreferences;

    public static final String SHARED_PREFS = "Lab2_prefs_restaurateur" ;

    public static final String NAME_KEY = "NAME_KEY";
    public static final String EMAIL_KEY = "EMAIL_KEY";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String PHONE_KEY = "PHONE_KEY";
    public static final String ADDRESS_KEY = "ADDRESS_KEY";
    public static final String TIME_TABLE_KEY = "TIME_TABLE_KEY";
    public static final String MORNING_TIME_KEY = "MORNING_TIME_KEY";
    public static final String EVENING_TIME_KEY = "EVENING_TIME_KEY";
    public static final String PHOTO_CHANGED_KEY = "PHOTO_CHANGED_KEY";
    
    //views
    private EditText etName;
    private EditText etMail;
    private EditText etPhone;
    private EditText etAddress;
    private EditText etDescription;
    private ImageView ivAvatar;
    private TextView tvDescriptionCount;
    private EditText etTimeTable;
    private EditText etMorningTime;
    private EditText etEveningTime;

    private int DESCRIPTION_MAX_LENGTH;
    
    private boolean photoChanged = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        etName = view.findViewById(R.id.et_name);
        etDescription = view.findViewById(R.id.et_description);
        etPhone = view.findViewById(R.id.et_phone);
        etMail = view.findViewById(R.id.et_mail);
        etAddress = view.findViewById(R.id.et_address);
        etTimeTable = view.findViewById(R.id.et_timeTable);
        etMorningTime = view.findViewById(R.id.et_morning_time);
        etEveningTime = view.findViewById(R.id.et_evening_time);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvDescriptionCount = view.findViewById(R.id.tv_description_count);
        
        //load from shared prefs or savedstate
        String name;
        String description;
        String mail;
        String address;
        String phone;
        String timeTable;
        String morningTime;
        String eveningTime;
        
        if (savedInstanceState == null) {
            sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    
            name = sharedPreferences.getString(NAME_KEY, "");
            description = sharedPreferences.getString(DESCRIPTION_KEY, "");
            mail = sharedPreferences.getString(EMAIL_KEY, "");
            address = sharedPreferences.getString(ADDRESS_KEY, "");
            phone = sharedPreferences.getString(PHONE_KEY, "");
            timeTable = sharedPreferences.getString(TIME_TABLE_KEY, "");
            morningTime = sharedPreferences.getString(MORNING_TIME_KEY, "");
            eveningTime = sharedPreferences.getString(EVENING_TIME_KEY, "");
    
        } else {
            name = savedInstanceState.getString(NAME_KEY, "");
            description = savedInstanceState.getString(DESCRIPTION_KEY, "");
            mail = savedInstanceState.getString(EMAIL_KEY, "");
            address = savedInstanceState.getString(ADDRESS_KEY, "");
            phone = savedInstanceState.getString(PHONE_KEY, "");
            timeTable = savedInstanceState.getString(TIME_TABLE_KEY, "");
            morningTime = savedInstanceState.getString(MORNING_TIME_KEY, "");
            eveningTime = savedInstanceState.getString(EVENING_TIME_KEY, "");
            
            photoChanged = savedInstanceState.getBoolean(PHOTO_CHANGED_KEY);
        }
    
        if (!name.isEmpty()) {
            etName.setText(name);
        }
    
        if (!description.isEmpty()) {
            etDescription.setText(description);
        }
    
        if (!mail.isEmpty()) {
            etMail.setText(mail);
        }
    
        if (!phone.isEmpty()) {
            etPhone.setText(phone);
        }
        
        if (!address.isEmpty()) {
            etAddress.setText(address);
        }
        
        if (!timeTable.isEmpty()) {
            etTimeTable.setText(timeTable);
        }
        
        if (!morningTime.isEmpty()) {
            etMorningTime.setText(morningTime);
        }
        
        if (!eveningTime.isEmpty()) {
            etEveningTime.setText(eveningTime);
        }
    
        //set avatar image from file
        updateAvatarImage();

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

        ivAvatar.setOnClickListener(v -> startActivityToGetImage());

        return view;

    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putBoolean(PHOTO_CHANGED_KEY, photoChanged);
        
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String address = etAddress.getText().toString();
        String mail = etMail.getText().toString();
        String phone = etPhone.getText().toString();
        String timeTable = etTimeTable.getText().toString();
        String morningTime = etMorningTime.getText().toString();
        String eveningTime = etEveningTime.getText().toString();
        
        outState.putString(NAME_KEY, name);
    
        outState.putString(DESCRIPTION_KEY, description);
    
        outState.putString(EMAIL_KEY, mail);
    
        outState.putString(PHONE_KEY, phone);
    
        outState.putString(ADDRESS_KEY, address);
    
        outState.putString(TIME_TABLE_KEY, timeTable);
    
        outState.putString(MORNING_TIME_KEY, morningTime);
    
        outState.putString(EVENING_TIME_KEY, eveningTime);
    }
    
    private void saveAvatarImage() {
        File main = getAvatarFile();
        File tmp = getAvatarTmpFile();

        try {
            FileInputStream fis = new FileInputStream(tmp);

            FileOutputStream fos = new FileOutputStream(main);

            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = fis.read(buffer);
                if (bytesRead == -1)
                    break;
                fos.write(buffer, 0, bytesRead);
            }

            fos.flush();
            fos.close();
            fis.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOexception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void updateDescriptionCount() {
        int count = etDescription.getText().length();

        String cnt = count + "/" + DESCRIPTION_MAX_LENGTH;

        tvDescriptionCount.setText(cnt);
    }

    private void saveDataSharedPrefs() {
        //we should save everything in the share prefs
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String address = etAddress.getText().toString();
        String mail = etMail.getText().toString();
        String phone = etPhone.getText().toString();
        String timeTable = etTimeTable.getText().toString();
        String morningTime = etMorningTime.getText().toString();
        String eveningTime = etEveningTime.getText().toString();

        
        editor.putString(NAME_KEY, name);

        editor.putString(DESCRIPTION_KEY, description);

        editor.putString(EMAIL_KEY, mail);

        editor.putString(PHONE_KEY, phone);

        editor.putString(ADDRESS_KEY, address);

        editor.putString(TIME_TABLE_KEY, timeTable);

        editor.putString(MORNING_TIME_KEY, morningTime);

        editor.putString(EVENING_TIME_KEY, eveningTime);

        editor.apply();

    }

    private void startActivityToGetImage() {
        File myImageFile = getAvatarTmpFile();

        final Uri outputFileUri = FileProvider.getUriForFile(getContext().getApplicationContext(),
                AUTHORITY,
                myImageFile);


        Log.d(TAG, "Uri: " + outputFileUri.toString());

        // Camera
        final List<Intent> totIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getContext().getPackageManager();
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

    private File getAvatarFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getContext().getApplicationContext().getFilesDir() + File.separator + IMAGES_DIR + File.separator);
        root.mkdirs();
        final String fname = "RestaurantAvatar.jpg";

        return new File(root, fname);
    }

    private File getAvatarTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getContext().getApplicationContext().getFilesDir() + File.separator + IMAGES_DIR + File.separator);
        root.mkdirs();
        final String fname = "RestaurantAvatar_tmp.jpg";

        return new File(root, fname);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    InputStream is = getContext().getContentResolver().openInputStream(selectedImageUri);
                    FileOutputStream fs = new FileOutputStream(getAvatarTmpFile());

                    if (is == null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
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

                    //update shown image
                    photoChanged = true;
                    updateAvatarImage();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read bitmap");
                    e.printStackTrace();
                }


            } else {
                Log.d(TAG, "Image successfully captured with camera");
                //update shown image
                photoChanged = true;
                updateAvatarImage();
            }

        }
    }

    private void updateAvatarImage() {
        File img;
        
        if (!photoChanged)
            img = getAvatarFile();
        else
            img = getAvatarTmpFile();
        
        if (!img.exists() || !img.isFile()) {
            Log.d(TAG, "Cannot load unexisting file as avatar");
            return;
        }
        
        Context cnt = getContext();
        
        if (cnt != null) {
            Glide.with(cnt)
                    .load(img)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivAvatar);
        }
    }
    
    
    public void saveData() {
        if (photoChanged)
            saveAvatarImage();
        
        saveDataSharedPrefs();
        //show toast to user
        Snackbar.make(etAddress, R.string.data_saved, Snackbar.LENGTH_SHORT).show();
    }
}
