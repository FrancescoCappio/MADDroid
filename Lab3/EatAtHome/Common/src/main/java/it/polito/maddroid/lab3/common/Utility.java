package it.polito.maddroid.lab3.common;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Utility {
    private static final String TAG = "Utility";
    
    public static void showAlertToUser(Activity activity, int stringResId) {
        String alert = activity.getResources().getString(stringResId);
        Snackbar.make(activity.findViewById(android.R.id.content),alert,Snackbar.LENGTH_SHORT).show();
    }
    
    public static void startActivityToGetImage(Activity context, String AUTHORITY, File destFile, int requestCode) {
        File myImageFile = destFile;
        
        final Uri outputFileUri = FileProvider.getUriForFile(context.getApplicationContext(),
                AUTHORITY,
                myImageFile);
        
        
        Log.d(TAG, "Uri: " + outputFileUri.toString());
        
        // Camera
        final List<Intent> totIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
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
        
        context.startActivityForResult(chooserIntent, requestCode);
    }
}
