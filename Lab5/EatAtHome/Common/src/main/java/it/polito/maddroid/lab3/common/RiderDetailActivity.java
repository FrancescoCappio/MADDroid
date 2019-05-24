package it.polito.maddroid.lab3.common;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class RiderDetailActivity extends AppCompatActivity {

    public static final String TAG = "RiderDetailActivity";
    public static final String RIDER_KEY = "RIDER_KEY";

    private Rider currentRider;

    private int waitingCount = 0;

    private TextView tvName;
    private TextView tvPhoneNumber;
    private TextView tvEmail;
    private TextView tvDescription;
    private ImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_detail);


        Intent intent = getIntent();

        if (intent.getSerializableExtra(RIDER_KEY) == null) {
            Log.e(TAG, "Cannot open rider detail activity for null rider");
            finish();
            return;
        }

        currentRider = (Rider) intent.getSerializableExtra(RIDER_KEY);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.rider_info);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getReferencesToViews();

        setDataToViews();

        setOnClickListeners();
    }

    private void getReferencesToViews() {
        tvName = findViewById(R.id.tv_name);
        tvDescription = findViewById(R.id.tv_description);
        tvEmail = findViewById(R.id.tv_email);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);

        ivAvatar = findViewById(R.id.iv_avatar);
    }

    private void setDataToViews() {
        tvName.setText(currentRider.getName());
        tvPhoneNumber.setText(currentRider.getPhoneNumber());
        tvEmail.setText(currentRider.getEmail());
        tvDescription.setText(currentRider.getDescription());

        downloadAvatar(currentRider.getId());
    }

    private void setOnClickListeners() {
        tvPhoneNumber.setOnClickListener(v -> {
            Intent intent = Utility.generateIntentPhoneNumber(currentRider.getPhoneNumber());
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        tvEmail.setOnClickListener(v -> {
            Intent intent = Utility.generateIntentEmail(currentRider.getEmail());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    private File getAvatarTmpFile() {
        // Determine Uri of camera image to save.
        final File root = new File(getApplicationContext().getFilesDir() + File.separator + "images" + File.separator);
        root.mkdirs();
        final String fname = "Rider_avatar_tmp_" + currentRider.getId() + ".jpg";
        return new File(root, fname);
    }

    private void downloadAvatar(String UID) {
        File localFile = getAvatarTmpFile();

        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("avatar_" + UID +".jpg");

        setActivityLoading(true);

        riversRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Avatar downloaded successfully");
                    updateAvatarImage();
                    setActivityLoading(false);
                }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error while downloading avatar image: " + exception.getMessage());
            Utility.showAlertToUser(this, R.string.notify_avatar_download_ko);
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
                .into(ivAvatar);

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


}
