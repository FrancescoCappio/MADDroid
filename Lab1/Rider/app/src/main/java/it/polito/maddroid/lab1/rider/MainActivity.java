package it.polito.maddroid.lab1.rider;


import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //shared prefs and constants
    private SharedPreferences sharedPreferences;

    public static final String SHARED_PREFS = "Lab1_prefs_rider" ;

    public static final String NAME_KEY = "NAME_KEY";
    public static final String EMAIL_KEY = "EMAIL_KEY";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String PHONE_KEY = "PHONE_KEY";
    public static final String BIKE_KEY = "BIKE_KEY";
    public static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";

    //this path must be one of the paths specified in "file_paths.xml"
    private static final String AVATAR_DIR = "images";
    //content provider authority
    private static final String AUTHORITY = "it.polito.maddroid.lab1.rider.fileprovider";

    private static int PHOTO_REQUEST_CODE = 128;

    private boolean editMode = false;

    private MenuItem menuEdit;
    private MenuItem menuSave;

    //views
    private EditText etName;
    private EditText etMail;
    private EditText etPhone;
    private EditText etBike;
    private EditText etDescription;
    private ImageView ivAvatar;

    private FloatingActionButton fabAddPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etPhone = findViewById(R.id.et_phone);
        etMail = findViewById(R.id.et_mail);
        etBike = findViewById(R.id.et_bike);
        ivAvatar = findViewById(R.id.iv_avatar);
        fabAddPhoto = findViewById(R.id.fab_add_photo);

        //set avatar image from file
        updateAvatarImage();

        //load from shared prefs
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String name = sharedPreferences.getString(NAME_KEY, "");
        String description = sharedPreferences.getString(DESCRIPTION_KEY, "");
        String mail = sharedPreferences.getString(EMAIL_KEY, "");
        String bike = sharedPreferences.getString(BIKE_KEY, "");
        String phone = sharedPreferences.getString(PHONE_KEY, "");

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
        if (!bike.isEmpty()) {
            etBike.setText(bike);
        }
        getSupportActionBar().setTitle(R.string.profile_info);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_account_info, menu);

        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_save);

        //enable/disable edit depending on the state
        setEditEnabled(editMode);

        //add on click action to imageview
        ivAvatar.setOnClickListener(v -> startActivityToGetImage());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.menu_edit) {
            setEditEnabled(true);
        } else if (item.getItemId() == R.id.menu_save) {
            setEditEnabled(false);

            saveDataSharedPrefs();

            saveAvatarImage();
        }

        return true;
    }

    private void saveAvatarImage() {
        File main = getAvatarFile();
        File tmp = getAvatarTmpFile();

        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tmp), null, options);

            if (bitmap == null) {
                Log.e(TAG, "NULL BITMAP");
                return;
            }
            FileOutputStream fs = new FileOutputStream(main);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fs);
            fs.flush();
            fs.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOexception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void setEditEnabled(boolean enabled) {
        editMode = enabled;
        menuEdit.setVisible(!enabled);
        menuSave.setVisible(enabled);

        if (!enabled) {
            etDescription.setFocusable(enabled);
            etPhone.setFocusable(enabled);
            etMail.setFocusable(enabled);
            etName.setFocusable(enabled);
            etBike.setFocusable(enabled);
           
        } else {
            etDescription.setFocusableInTouchMode(enabled);
            etPhone.setFocusableInTouchMode(enabled);
            etMail.setFocusableInTouchMode(enabled);
            etName.setFocusableInTouchMode(enabled);
            etBike.setFocusableInTouchMode(enabled);
        }
        
        ivAvatar.setEnabled(enabled);

        if (enabled)
            fabAddPhoto.show();
        else
            fabAddPhoto.hide();
    }

    private void saveDataSharedPrefs() {
        //we should save everything in the share prefs
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String mail = etMail.getText().toString();
        String phone = etPhone.getText().toString();
        String bike = etBike.getText().toString();


        editor.putString(NAME_KEY, name);

        editor.putString(DESCRIPTION_KEY, description);

        editor.putString(EMAIL_KEY, mail);

        editor.putString(PHONE_KEY, phone);

        editor.putString(BIKE_KEY, bike);

        editor.apply();

        //show toast to user
        Toast.makeText(getApplicationContext(), R.string.data_saved, Toast.LENGTH_SHORT).show();
    }

    private void startActivityToGetImage() {
        File myImageFile = getAvatarTmpFile();

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

    private File getAvatarTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + AVATAR_DIR + File.separator);
        root.mkdirs();
        final String fname = "avatar_tmp.jpg";

        return new File(root, fname);
    }

    private File getAvatarFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + AVATAR_DIR + File.separator);
        root.mkdirs();
        final String fname = "avatar.jpg";

        return new File(root, fname);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    FileOutputStream fs = new FileOutputStream(getAvatarTmpFile());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 99, fs);
                    fs.flush();
                    fs.close();

                    updateAvatarImage();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read bitmap");
                    e.printStackTrace();
                }


            } else {
                Log.d(TAG, "Image successfully captured with camera");
                updateAvatarImage();
            }

        }
    }

    private void updateAvatarImage() {
        File img;
        if (!editMode) {
            img = getAvatarFile();
        } else {
            img = getAvatarTmpFile();
        }

        if (!img.exists() || !img.isFile()) {
            Log.d(TAG, "Cannot load unexisting file as avatar");
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(img.getAbsolutePath(), options);

        ivAvatar.setImageBitmap(bitmap);
    }


    //manage layout changes
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String mail = etMail.getText().toString();
        String phone = etPhone.getText().toString();
        String bike= etBike.getText().toString();

        if (!name.isEmpty()) {
            outState.putString(NAME_KEY, name);
        }

        if (!description.isEmpty()) {
            outState.putString(DESCRIPTION_KEY, description);
        }

        if (!mail.isEmpty()) {
            outState.putString(EMAIL_KEY, mail);
        }

        if (!phone.isEmpty()) {
            outState.putString(PHONE_KEY, phone);
        }

        if (!bike.isEmpty()) {
            outState.putString(BIKE_KEY, bike);
        }
        //save edit mode status
        outState.putBoolean(EDIT_MODE_KEY, editMode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String name = savedInstanceState.getString(NAME_KEY, "");
        String description = savedInstanceState.getString(DESCRIPTION_KEY, "");
        String mail = savedInstanceState.getString(EMAIL_KEY, "");
        String phone = savedInstanceState.getString(PHONE_KEY, "");
        String bike= savedInstanceState.getString(BIKE_KEY,"");

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


        if (!bike.isEmpty()) {
            etBike.setText(bike);
        }
        //restore editMode
        editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
    }
}
