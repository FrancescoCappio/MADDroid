package it.polito.maddroid.lab2;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class DailyOfferDetailActivity extends AppCompatActivity {
    private static final String TAG = "DailyOfferDetail";


    private MenuItem menuEdit;
    private MenuItem menuSave;
    private boolean editMode = false;
    private String pageType;
    
    public final static String PAGE_TYPE_KEY = "PAGE_TYPE_KEY";
    public final static String MODE_NEW = "New";
    public final static String MODE_SHOW = "Show";
    public final static String OFFER_ID_KEY = "OFFER_ID_KEY";

    //views
    private EditText etName;
    private EditText etQuantity;
    private EditText etPrice;
    private EditText etDescription;
    private ImageView ivAvatar;
    private TextView tvDescriptionCount;
    private FloatingActionButton fabAddPhoto;


    //this path must be one of the paths specified in "file_paths.xml"
    private static final String AVATAR_DIR = "images";
    private static final String DATA_DIR = "data";
    //content provider authority
    private static final String AUTHORITY = "it.polito.maddroid.lab2.fileprovider";
    
    private int currentOfferId = -1;

    private static int PHOTO_REQUEST_CODE = 128;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_offer_detail_activty);

        etName =findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvDescriptionCount = findViewById(R.id.tv_description_count);
        fabAddPhoto = findViewById(R.id.fab_add_photo);
    
        Intent i  = getIntent();
        pageType = i.getStringExtra(PAGE_TYPE_KEY);
        
        if (pageType.equals(MODE_NEW)) {
            int newOfferId = getLastOfferId() +1;
            currentOfferId = newOfferId;
        } else {
            int offerId = i.getIntExtra(OFFER_ID_KEY, -1);
            currentOfferId = offerId;
        }

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);

        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_save);

        //enable/disable edit depending on the state
        setEditEnabled(editMode);

        //add on click action to imageview
        ivAvatar.setOnClickListener(v -> startActivityToGetImage());

        

        if(pageType.equals(MODE_NEW)) {
            getSupportActionBar().setTitle("New Dish");
            setEditEnabled(true);
        } else if (pageType.equals("Show")){
            getSupportActionBar().setTitle("Information");
            setEditEnabled(false);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        if (item.getItemId() == R.id.menu_edit)
            setEditEnabled(true);
        else if (item.getItemId() == R.id.menu_save)
        {
            String Name =etName.getText().toString();
            String Quantity=etQuantity.getText().toString();
            String price = etPrice.getText().toString();
            String description = etDescription .getText().toString();
            
            
            if(pageType.equals(MODE_NEW)){
                
                DailyOffer offer = new DailyOffer(currentOfferId,Name,description,Integer.parseInt(Quantity), Float.parseFloat(price));
                saveNewData(offer);
                saveAvatarImage();
                
            } else if (pageType.equals("Edit")){
                setEditEnabled(false);
            }
        }
        return true;
    }

    private int getLastOfferId(){
        String DataRead = readJsonData();
        int OfferId = 0;
        if (DataRead.isEmpty() || DataRead == null)
            OfferId = 0;
        else {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<DailyOffer>>() {
            }.getType();
            List<DailyOffer> history = gson.fromJson(DataRead, listType);
            DailyOffer lastOffer = history.get(history.size()-1);
            OfferId = lastOffer.getid();
        }

        return OfferId;
    }

    private void saveNewData(DailyOffer newData) {

        //Read data from JSON File
        String DataRead = readJsonData();

        //convert Json data to java object
        Gson gson = new Gson();
        Type listType = new TypeToken< ArrayList<DailyOffer> >(){}.getType();
        List<DailyOffer> history = gson.fromJson(DataRead, listType);
        
        if (history == null) {
            //the file is empty
            history = new ArrayList<>();
        }

        //Add new row to data
        history.add(newData);

        //Sort the data by DailyOfferId
        Collections.sort(history,new DailyOffer.Sortbyroll());

        //save data to new json
        String json = new Gson().toJson(history);
        saveDatatoJson(json);

    }

    private void saveDatatoJson(String mJsonResponse) {
        try {
            final File root = new File(getApplicationContext().getFilesDir() + File.separator + DATA_DIR + File.separator);
            root.mkdirs();
            final String fname = "DailoffersData.json";
            FileWriter file = new FileWriter(new File(root, fname));
            file.write(mJsonResponse);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readJsonData() {
        String mResponse ="";
        try {
            final File root = new File(getApplicationContext().getFilesDir() + File.separator + DATA_DIR + File.separator);
            root.mkdirs();
            final String fname = "DailoffersData.json";
            
            File dst = new File(root, fname);
            
            if (!dst.exists()) {
                return "";
            }
            
            FileInputStream is = new FileInputStream(dst);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            mResponse = new String(buffer);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mResponse;
    }

    private void saveAvatarImage() {
        
        if (currentOfferId == -1) {
            Log.e(TAG, "Cannot save image without id");
            return;
        }
        File main = getAvatarFile(currentOfferId);
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

    private void setEditEnabled(boolean enabled) {
        editMode = enabled;
        menuEdit.setVisible(!enabled);
        menuSave.setVisible(enabled);

        etDescription.setEnabled(enabled);
        etPrice.setEnabled(enabled);
        etQuantity.setEnabled(enabled);
        etName.setEnabled(enabled);
        ivAvatar.setEnabled(enabled);


        if (enabled)
            fabAddPhoto.show();
        else
            fabAddPhoto.hide();
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
                    // we need to copy the image into our directory, we try 2 methods to do this:
                    // 1. if possible we copy manually with input stream and output stream so that the exif interface is not lost
                    // 2. if we can't access the exif interface then we try to decode the bitmap and we encode it again in our directory
                    InputStream is = getContentResolver().openInputStream(selectedImageUri);
                    FileOutputStream fs = new FileOutputStream(getAvatarTmpFile());

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

    private File getAvatarFile(int id) {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + AVATAR_DIR + File.separator);
        root.mkdirs();
        final String fname = "avatar_" + id + ".jpg";

        return new File(root, fname);
    }

    private File getAvatarTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + AVATAR_DIR + File.separator);
        root.mkdirs();
        final String fname = "avatar_tmp.jpg";

        return new File(root, fname);
    }

    private void updateAvatarImage() {
        
        if (currentOfferId == -1) {
            Log.e(TAG, "Cannot show image with null id");
            return;
        }
        
        File img;
        if (!editMode) {
            img = getAvatarFile(currentOfferId);
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
