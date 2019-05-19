package it.polito.maddroid.lab3.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Address;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.GeocodingLocation;
import it.polito.maddroid.lab3.common.LoginActivity;
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
    boolean photoChanged = false;
    boolean photoPresent = false;
    private boolean positionDialogOpen = false;

    private AlertDialog possiblePosition;
    private List<Address> addressList;
    private String[] multiChoiceItems;
    private Address address;
    private int choice;

    // menu items
    private MenuItem menuEdit;
    private MenuItem menuConfirm;


    // views
    private EditText etName;
    private EditText etPhone;
    private EditText etMail;
    private EditText etDescription;
    private EditText etAddress;
    private EditText etAddressNotes;
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


    // String keys to store instances info
    private static final String NAME_KEY = "NAME_KEY";
    private static final String DESCRIPTION_KEY = "DESCRIPTION_KEY";
    private static final String PHONE_KEY = "PHONE_KEY";
    private static final String EMAIL_KEY = "EMAIL_KEY";
    private static final String ADDRESS_KEY = "ADDRESS_KEY";
    private static final String ADDRESSES_LIST_KEY = "ADDRESS_LIST_KEY";
    private static final String PHOTO_PRESENT_KEY = "PHOTO_PRESENT_KEY";
    private static final String CHOICE_KEY = "CHOICE_KEY";
    private static final String POSITIONS_KEY = "PSOITIONS_KEY";
    private static final String PHOTO_CHANGED_KEY = "PHOTO_CHANGED_KEY";
    private static final String POSITION_DIALOG_KEY = "POSITION_DIALOG_KEY";
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

        if (currentUser == null)
        {
            Utility.showAlertToUser(this ,R.string.login_alert);

            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            loginIntent.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
            startActivity(loginIntent);

            //exit
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
    
        //do not show the keyboard on activity open
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case android.R.id.home:
                //emulate back pressed
                onBackPressed();
                break;

            case R.id.menu_confirm:
                String userAddress = etAddress.getText().toString();
                if(userAddress.isEmpty())
                {
                    Utility.showAlertToUser(this, R.string.fields_empty_alert);
                    return true;
                }
                else
                {
                    GeocodingLocation locationAddress = new GeocodingLocation();
                    setActivityLoading(true);
                    locationAddress.getAddressFromLocation(userAddress, getApplicationContext(), new GeocoderHandler());
                }
                break;

            case R.id.menu_edit:
                editMode = true;
                setEditEnabled(true);
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        etAddressNotes.setEnabled(enabled);

        if (enabled)
            fabPhoto.show();
        else
            fabPhoto.hide();

        ivPhoto.setEnabled(enabled);
    }


    private void getReferencesToViews() {

        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        etAddressNotes = findViewById(R.id.et_address_notes);
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

    private void logoutAction() {
        mAuth.signOut();

        // launch splash screen activity again and then loginactivity
        Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
        intent.putExtra(EAHCONST.LAUNCH_APP_KEY, EAHCONST.LAUNCH_APP_USER);
        intent.putExtra(EAHCONST.LAUNCH_ACTIVITY_KEY, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finishAndRemoveTask();
    }

    private File getAvatarTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + "images" + File.separator);
        root.mkdirs();
        final String fname = "CustomerAvatar_tmp.jpg";
        return new File(root, fname);
    }

    private void updateDescriptionCount() {
        int count = etDescription.getText().length();

        String cnt = count + "/" + DESCRIPTION_MAX_LENGTH;

        tvDescriptionCount.setText(cnt);
    }

    private void manageLaunchIntent() {
        Intent launchIntent = getIntent();

        editMode = launchIntent.getBooleanExtra(EAHCONST.LAUNCH_EDIT_ENABLED_KEY, false);

        setEditEnabled(editMode);

        mandatoryAccountInfo = launchIntent.getBooleanExtra(EAHCONST.ACCOUNT_INFO_EMPTY, false);

        if (!mandatoryAccountInfo) {
            // the user has probably already inserted some info in the past
            retrieveCustomersInfo();
            downloadAvatar(currentUser.getUid());
        } else {
            Utility.showAlertToUser(this, R.string.account_info_alert);
        }
    }

    private void retrieveCustomersInfo() {
        // first we check if the user has already inserted info in the past, in this case we load them

        setActivityLoading(true);

        String userId = currentUser.getUid();

        // we want to read but we are not interested in updates
        dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String customerName = (String) dataSnapshot.child(EAHCONST.CUSTOMER_NAME).getValue();
                String customerAddress = (String) dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS).getValue();
                String customerEmail = (String) dataSnapshot.child(EAHCONST.CUSTOMER_EMAIL).getValue();
                String customerPhone = (String) dataSnapshot.child(EAHCONST.CUSTOMER_PHONE).getValue();
                String customerDescription = (String) dataSnapshot.child(EAHCONST.CUSTOMER_DESCRIPTION).getValue();
                String addressNotes = (String) dataSnapshot.child(EAHCONST.CUSTOMER_ADDRESS_NOTES).getValue();

                if (customerName != null) {
                    etName.setText(customerName);
                }

                if (customerAddress != null) {
                    etAddress.setText(customerAddress);
                }

                if (customerEmail != null) {
                    etMail.setText(customerEmail);
                }

                if (customerPhone != null) {
                    etPhone.setText(customerPhone);
                }

                if (customerDescription != null) {
                    etDescription.setText(customerDescription);
                }
                
                if (addressNotes != null) {
                    etAddressNotes.setText(addressNotes);
                }

                setActivityLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setActivityLoading(false);
            }
        });
    }

    private boolean manageUserConfirm() {

        // first we get info from edittexts
        String customerName = etName.getText().toString();
        String customerPhone = etPhone.getText().toString();
        String customerAddress = etAddress.getText().toString();
        String customerEmail = etMail.getText().toString();
        String customerDescription = etDescription.getText().toString();
        String customerAddressNotes = etAddressNotes.getText().toString();

        if (customerName.isEmpty() || customerPhone.isEmpty() || customerAddress.isEmpty() || customerEmail.isEmpty() || customerDescription.isEmpty() || customerAddressNotes.isEmpty()) {
            Utility.showAlertToUser(this, R.string.fields_empty_alert);
            return false;
        }

        if (mandatoryAccountInfo && !photoChanged) {
            Utility.showAlertToUser(this, R.string.image_empty_alert);
            return false;
        }

        setActivityLoading(true);

        // now add users info in users tree
        EAHCONST.USER_TYPE userType = EAHCONST.USER_TYPE.CUSTOMER;

        String userEmail = currentUser.getEmail();

        String userId = currentUser.getUid();

        if (photoChanged)
            uploadAvatar(userId);

        Map<String,Object> updateMap = new HashMap<>();

        // userEmail cannot be null because we permit only registration by email
        assert userEmail != null;

        updateMap.put(EAHCONST.generatePath(EAHCONST.USERS_SUB_TREE, userId, EAHCONST.USERS_MAIL), userEmail);
        updateMap.put(EAHCONST.generatePath(EAHCONST.USERS_SUB_TREE, userId, EAHCONST.USERS_TYPE), userType);

        updateMap.put(EAHCONST.generatePath(EAHCONST.CUSTOMERS_SUB_TREE, userId, EAHCONST.CUSTOMER_NAME), customerName);
        updateMap.put(EAHCONST.generatePath(EAHCONST.CUSTOMERS_SUB_TREE, userId, EAHCONST.CUSTOMER_ADDRESS), customerAddress);
        updateMap.put(EAHCONST.generatePath(EAHCONST.CUSTOMERS_SUB_TREE, userId, EAHCONST.CUSTOMER_DESCRIPTION), customerDescription);
        updateMap.put(EAHCONST.generatePath(EAHCONST.CUSTOMERS_SUB_TREE, userId, EAHCONST.CUSTOMER_EMAIL), customerEmail);
        updateMap.put(EAHCONST.generatePath(EAHCONST.CUSTOMERS_SUB_TREE, userId, EAHCONST.CUSTOMER_PHONE), customerPhone);
        updateMap.put(EAHCONST.generatePath(EAHCONST.CUSTOMERS_SUB_TREE, userId, EAHCONST.CUSTOMER_ADDRESS_NOTES), customerAddressNotes);

        dbRef.updateChildren(updateMap).addOnSuccessListener(aVoid -> {

            Log.d(TAG, "Success registering user info");
            setActivityLoading(false);
            DatabaseReference dbRef1 = dbRef.child(EAHCONST.CUSTOMERS_SUB_TREE).child(currentUser.getUid());
            GeoFire geoFire = new GeoFire(dbRef1);
            geoFire.setLocation(EAHCONST.CUSTOMER_POSITION, new GeoLocation(address.getLatitude(), address.getLongitude()), (key, error) -> {
                if (error != null) {
                    Log.d("Location GeoFire", "There was an error saving the location to GeoFire: " + error);
                } else {
                    Log.d("Location GeoFire", "Location saved on server successfully!");
                }
            });
            Utility.showAlertToUser(this,R.string.notify_save_ok);

        }).addOnFailureListener(e -> {

            Log.e(TAG, "Database error while registering user info: " + e.getMessage());
            setActivityLoading(false);
            Utility.showAlertToUser(this, R.string.notify_save_ko);

        });
        return true;
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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(possiblePosition != null)
            possiblePosition.dismiss();

        outState.putString(NAME_KEY, etName.getText().toString());
        outState.putString(DESCRIPTION_KEY, etDescription.getText().toString());
        outState.putString(PHONE_KEY, etPhone.getText().toString());
        outState.putString(EMAIL_KEY, etMail.getText().toString());
        outState.putString(ADDRESS_KEY, etAddress.getText().toString());
        outState.putSerializable(ADDRESSES_LIST_KEY, (Serializable) addressList);
        outState.putSerializable(POSITIONS_KEY, (Serializable) multiChoiceItems);
        outState.putInt(CHOICE_KEY, choice);

        outState.putBoolean(MANDATORY_INFO_KEY, mandatoryAccountInfo);
        outState.putBoolean(EDIT_MODE_KEY, editMode);

        outState.putBoolean(PHOTO_PRESENT_KEY, photoPresent);
        outState.putBoolean(PHOTO_CHANGED_KEY, photoChanged);

        outState.putBoolean(POSITION_DIALOG_KEY, positionDialogOpen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        etName.setText(savedInstanceState.getString(NAME_KEY, ""));
        etDescription.setText(savedInstanceState.getString(DESCRIPTION_KEY, ""));
        etAddress.setText(savedInstanceState.getString(ADDRESS_KEY, ""));
        etPhone.setText(savedInstanceState.getString(PHONE_KEY, ""));
        etMail.setText(savedInstanceState.getString(EMAIL_KEY, ""));
        choice = savedInstanceState.getInt(CHOICE_KEY);
        addressList = (List<Address>) savedInstanceState.getSerializable(ADDRESSES_LIST_KEY);
        multiChoiceItems = (String[]) savedInstanceState.getSerializable(POSITIONS_KEY);

        editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
        mandatoryAccountInfo = savedInstanceState.getBoolean(MANDATORY_INFO_KEY);

        photoPresent = savedInstanceState.getBoolean(PHOTO_PRESENT_KEY);
        photoChanged = savedInstanceState.getBoolean(PHOTO_CHANGED_KEY);
        positionDialogOpen = savedInstanceState.getBoolean(POSITION_DIALOG_KEY, false);

        if (photoPresent)
            updateAvatarImage();
        if  (positionDialogOpen) {
            showPositionDialog(multiChoiceItems, choice);
        }

        setEditEnabled(editMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(possiblePosition != null) {
            possiblePosition.dismiss();
        }
    }

    public class GeocoderHandler extends Handler {
        String locationAddress = new String();

        @Override
        public void handleMessage(Message message) {
            setActivityLoading(false);

            switch (message.what) {
                case 0:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    etAddress.setText("");
                    etAddress.setHint(R.string.address_not_found);
                    Utility.showAlertToUser(AccountInfoActivity.this, R.string.address_not_found );
                    launchConfirm();
                    break;
                case 1:
                    bundle = message.getData();
                    addressList = (List<Address>) bundle.getSerializable("address");
                    Log.d("accountInfo latlong", addressList.get(0).getLatitude() + " " + addressList.get(0).getLongitude());
                    address = addressList.get(0);
                    etAddress.setText("" + address.getThoroughfare()+" "+ address.getSubThoroughfare()+ ", " + address.getLocality() );
                    launchConfirm();
                    break;
                case 2:
                    bundle = message.getData();
                    addressList = (List<Address>) bundle.getSerializable("address");
                    multiChoiceItems = new String[addressList.size()];
                    for (int i = 0; i < addressList.size(); ++i) {
                        multiChoiceItems[i] = "" + addressList.get(i).getThoroughfare();
                        multiChoiceItems[i] = multiChoiceItems[i] + " " + addressList.get(i).getSubThoroughfare();
                        multiChoiceItems[i] = multiChoiceItems[i] + ", " + addressList.get(i).getLocality();

                    }
                    showPositionDialog(multiChoiceItems, 0);
                    break;
                default:
                    locationAddress = null;
                    break;
            }


        }
    }

    private void showPositionDialog(String[] multiChoiceItems, int checkedItems) {
        positionDialogOpen = true;

        possiblePosition = new AlertDialog.Builder(this)
                .setTitle("Select Your Address")
                .setSingleChoiceItems(multiChoiceItems, checkedItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choice = which;
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        etAddress.setText(multiChoiceItems[choice]);
                        Log.d("accountInfo latlong", addressList.get(choice).getLatitude() + " " + addressList.get(choice).getLongitude());
                        address = addressList.get(choice);
                        positionDialogOpen = false;
                        launchConfirm();
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        etAddress.setText("");
                        launchConfirm();
                        positionDialogOpen = false;
                    }
                }).create();


        possiblePosition.show();

    }


    private void launchConfirm(){
        if (manageUserConfirm()) {
            editMode = false;
            setEditEnabled(false);
        }

    }

}
