package it.polito.maddroid.lab3.common;


import android.app.Activity;
import android.support.design.widget.Snackbar;


public class Utility {
    
    public static void showAlertToUser(Activity activity, int stringResId) {
        String alert = activity.getResources().getString(stringResId);
        Snackbar.make(activity.findViewById(android.R.id.content),alert,Snackbar.LENGTH_SHORT).show();
    }
}
