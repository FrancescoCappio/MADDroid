package it.polito.maddroid.lab2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    public static final String SHARED_PREFS = "Lab1_prefs_restaurateur" ;

    public static final String NAME_KEY = "NAME_KEY";
    public static final String EMAIL_KEY = "EMAIL_KEY";
    public static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    public static final String PHONE_KEY = "PHONE_KEY";
    public static final String ADDRESS_KEY = "ADDRESS_KEY";
    public static final String TIME_TABLE_KEY = "TIME_TABLE_KEY";
    public static final String MORNING_TIME_KEY = "MORNING_TIME_KEY";
    public static final String EVENING_TIME_KEY = "EVENING_TIME_KEY";
    public static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";


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
    private FloatingActionButton fabAddPhoto;
    private Button btSaveDeatial;


    private int DESCRIPTION_MAX_LENGTH;


    private boolean editMode = true;




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
        fabAddPhoto = view.findViewById(R.id.fab_add_photo);
        btSaveDeatial = view.findViewById(R.id.ib_Confirm_detail);


        //set avatar image from file
        updateAvatarImage();

        //load from shared prefs
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String name = sharedPreferences.getString(NAME_KEY, "");
        String description = sharedPreferences.getString(DESCRIPTION_KEY, "");
        String mail = sharedPreferences.getString(EMAIL_KEY, "");
        String address = sharedPreferences.getString(ADDRESS_KEY, "");
        String phone = sharedPreferences.getString(PHONE_KEY, "");
        String timeTable = sharedPreferences.getString(TIME_TABLE_KEY, "");
        String morningTime = sharedPreferences.getString(MORNING_TIME_KEY, "");
        String eveningTime = sharedPreferences.getString(EVENING_TIME_KEY, "");

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
        btSaveDeatial.setOnClickListener(v -> {
            saveAvatarImage();
            saveDataSharedPrefs();
            //show toast to user
            Toast.makeText(getContext().getApplicationContext(), R.string.data_saved, Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).selectItem(0);


        } );

        return view;

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

        //show toast to user
        //Toast.makeText(getContext().getApplicationContext(), R.string.data_saved, Toast.LENGTH_SHORT).show();
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
                    updateAvatarImage();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read bitmap");
                    e.printStackTrace();
                }


            } else {
                Log.d(TAG, "Image successfully captured with camera");
                //update shown image
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

        try {
            ExifInterface exif = new ExifInterface(img.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            bitmap = rotateBitmap(bitmap, orientation);
        } catch (IOException e) {
            Log.e(TAG, "Cannot obtain exif info to check image rotation");
            e.printStackTrace();
        }

        ivAvatar.setImageBitmap(bitmap);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


}
