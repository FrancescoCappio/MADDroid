package it.polito.maddroid.lab3.common;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class Utility {
    private static final String TAG = "Utility";
    private static final ArrayList<String> days = new ArrayList<>(Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"));

    public static void showAlertToUser(Activity activity, int stringResId) {
        if (activity == null) {
            Log.e(TAG, "Cannot show alert because the activity is null");
            return;
        }
        String alert = activity.getResources().getString(stringResId);
        Snackbar.make(activity.findViewById(android.R.id.content),alert,Snackbar.LENGTH_SHORT).show();
    }
    
    public static void showAlertToUser(Activity activity, String string) {
        if (activity == null) {
            Log.e(TAG, "Cannot show alert because the activity is null");
            return;
        }
        Snackbar.make(activity.findViewById(android.R.id.content),string,Snackbar.LENGTH_SHORT).show();
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
    
    public static String getCategoriesNamesMatchingIds(String categoriesIds, List<RestaurantCategory> categories) {
        StringBuilder sb = new StringBuilder();
        
        for (String catId : categoriesIds.split(";"))
            for (RestaurantCategory rc : categories)
                if (rc.getId().equals(catId))
                    sb.append(rc.getName()).append(", ");
                
        // delete last comma
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }
    
    public static int getPixelsFromDP(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    
    
    public static String extractTimeTable(String s){
    
        Map<String,String> map = new HashMap<>();
        String[] splitted = s.split(";");
        
        for (String dayTimeTable : splitted) {
            String day = dayTimeTable.split(",")[0];
    
            String timetable;
            if (day.equals(dayTimeTable)) {
                day = dayTimeTable.split(" ")[0];
                timetable = "Closed";
            } else
                timetable = dayTimeTable.substring(day.length() + 1);
            
            if (map.containsKey(timetable)) {
                map.put(timetable, map.get(timetable) + " " + day);
            } else {
                map.put(timetable, day);
            }
        }
        
        Map<String, String> inverted = new HashMap<>();
        for (Map.Entry<String,String> entry : map.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
    
        StringBuilder sb = new StringBuilder();
        List<String> dayslist = new ArrayList<>(inverted.keySet());
        Collections.sort(dayslist);
        for (String str : dayslist) {
            sb.append(str).append(" ").append(inverted.get(str)).append("\n");
        }
        
        s = sb.toString();
        for ( int i = 0; i < 7 ; ++i)
            s = s.replace("Day"+i, days.get(i));
        s = s.replace("_", "-");
        s = s.replace(",", "\t");
        s = s.replace(";", "\n");
        //remove trailing new line
        s = s.trim();
        
        return s;
    }
    
    public static boolean checkRestaurantOpen(String timeTable, String dateS, String timeS)
    {
        int timeHourP;
        int timeMinutesP;
        int timeHourA;
        int timeMinutesA;
        boolean [] restaurantWeeklyOpen = new boolean[10080];
        
        //first of all we convert chosen date and time in a Date object
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date orderDate;
        try {
            orderDate = df.parse(dateS);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception while parsing date");
            return false;
        }
        
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(orderDate);
        int day = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
        if (day < 0)
            day = 7;
        
        String splitted[] = timeS.split(":");
        
        int hour = Integer.parseInt(splitted[0]);
        int minutes = Integer.parseInt(splitted[1]);
        
        if ((hour < 0 || hour > 23 ) || (minutes < 0 || minutes > 59) || (day < 0 || day > 6) || timeTable.isEmpty())
            return false;
        
        String [] allWeek = timeTable.split(";");
        
        if (allWeek[day].contains("closed"))
            return false;
        
        for (int indexDay = 0; indexDay < 7 ; ++indexDay)
        {
            if (allWeek[indexDay].contains("closed"))
                continue;
            String [] allDay = allWeek[indexDay].split(",");
            for (int j = 1; j < allDay.length; ++j)
            {
                String [] timeFirst = allDay[j].split("_");
                timeFirst[0] = timeFirst[0].replace(":", " ");
                timeFirst[1] = timeFirst[1].replace(":", " ");
                timeHourP = Integer.parseInt(timeFirst[0].substring(0, 2));
                timeMinutesP = Integer.parseInt(timeFirst[0].substring(3));
                timeMinutesP = (timeHourP*60)+ timeMinutesP + (1440* indexDay);
                
                timeHourA = Integer.parseInt(timeFirst[1].substring(0, 2));
                timeMinutesA = Integer.parseInt(timeFirst[1].substring(3));
                if(timeHourP > timeHourA)
                    timeMinutesA = (timeHourA*60)+ timeMinutesA + (1440* (indexDay+1));
                else
                    timeMinutesA = (timeHourA*60)+ timeMinutesA + (1440* indexDay);
                
                
                for (int count = timeMinutesP ; count < timeMinutesA ; ++count)
                    restaurantWeeklyOpen[count] = true;
                
            }
        }
        
        return (restaurantWeeklyOpen[minutes + (hour*60) + (1440 *day)]);
        
    }
    
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    public interface RandomRiderCaller {
        void generatedRiderId(String riderId);
    }
    
    public static void generateRandomRiderId(DatabaseReference dbRef, RandomRiderCaller randomRiderCaller) {
        Query queryRef = dbRef
                .child(EAHCONST.USERS_SUB_TREE)
                .orderByChild(EAHCONST.USERS_TYPE)
                .startAt("RIDER").endAt("RIDER\uf8ff");
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                List<String> ridersIds = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ridersIds.add(ds.getKey());
                }
                
                int index = getRandomNumberInRange(0, ridersIds.size()-1);
                randomRiderCaller.generatedRiderId(ridersIds.get(index));
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                randomRiderCaller.generatedRiderId(null);
            }
        });
    }
    
    public static int getRandomNumberInRange(int min, int max) {
        
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        
        if (min == max)
            return min;
        
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
    
    public static void storeToken(DatabaseReference dbRef, String userId, String token) {
        dbRef.child(EAHCONST.USERS_SUB_TREE).child(userId).child(EAHCONST.USERS_TOKEN).setValue(token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token saved"))
                .addOnFailureListener(e -> Log.d(TAG, "Cannot store token"));
    }
    
    public static Intent generateIntentPhoneNumber(String phoneNumber) {
        return new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
    }
    
    public static Intent generateIntentEmail(String emailAddress) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
    
        String[] addresses = new String[1];
        addresses[0] = emailAddress;
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        return intent;
    }
    
    public static String minutesToPrettyDuration(Context context, int minutes, boolean longString) {
        
        if (minutes < 60) {
            if (longString) {
                if (minutes == 1)
                    return minutes + " " + context.getString(R.string.minute_long);
                else
                    return minutes + " " + context.getString(R.string.minutes_long);
            } else {
                return minutes + context.getString(R.string.minutes_short);
            }
        }
        
        int hours = minutes / 60;
        
        if (hours < 24) {
            int remain = minutes - hours * 60;
    
            if (longString) {
                String hoursS;
                if (hours == 1)
                    hoursS = hours + " " + context.getString(R.string.hour_long);
                else
                    hoursS = hours + " " + context.getString(R.string.hours_long);
    
                String minutesS;
                if (remain == 1)
                    minutesS = remain + " " + context.getString(R.string.minute_long);
                else
                    minutesS = remain + " " + context.getString(R.string.minutes_long);
    
                return hoursS + " " + minutesS;
            }
            else {
                return hours + context.getString(R.string.hours_short) + " " + remain + context.getString(R.string.minutes_short);
            }
        }
        
        int days = hours / 24;
        int remainMinutes = minutes - hours * 60;
        int remainHours = hours - days * 24;

        if (longString) {
            String daysS;
            if (days == 1)
                daysS = days + " " + context.getString(R.string.day_long);
            else
                daysS = days + " " + context.getString(R.string.days_long);
    
            String hoursS;
            if (remainHours == 1)
                hoursS = remainHours + " " + context.getString(R.string.hour_long);
            else
                hoursS = remainHours + " " + context.getString(R.string.hours_long);
    
            String minutesS;
            if (remainMinutes == 1)
                minutesS = remainMinutes + " " + context.getString(R.string.minute_long);
            else
                minutesS = remainMinutes + " " + context.getString(R.string.minutes_long);
    
            return daysS + " " + hoursS + " " + minutesS;
        }
        
        return days + context.getString(R.string.days_short) + " " + remainHours + context.getString(R.string.hours_short) + " " + remainMinutes + context.getString(R.string.minutes_short);
    }
    
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    public static boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
}



