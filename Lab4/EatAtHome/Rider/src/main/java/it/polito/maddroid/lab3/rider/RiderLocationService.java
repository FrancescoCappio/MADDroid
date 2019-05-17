package it.polito.maddroid.lab3.rider;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import it.polito.maddroid.lab3.common.EAHCONST;


public class RiderLocationService extends Service {
    
    private static RiderLocationService instance;
    
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String TAG = "RiderLocationService";
    
    private final static String CHANNEL_ID = "Rider_LOCATION_CHANNEL";
    private final static String CHANNEL_NAME = "Rider on duty";
    private final static String CHANNEL_DESC = "The app is running";
    private final static int RIDER_NOTIFICATION_ID = 158;
    
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    
    private Location lastLocation;
    
    public RiderLocationService() {
    
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot start a service for a not logged user");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        createNotificationChannel();
    
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(CHANNEL_NAME)
                .setContentText(CHANNEL_DESC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    
        // Create an explicit intent for an Activity in your app
        Intent newIntent = new Intent(this, MainActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        newIntent.putExtra(EAHCONST.NOTIFICATION_KEY, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newIntent, 0);
        builder.setContentIntent(pendingIntent);
    
        startForeground(RIDER_NOTIFICATION_ID, builder.build());
    
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        startLocation();
        
        Log.d(TAG, "Service started");
        
        instance = this;
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    
        if (currentUser != null) {
            String riderOrderPath = EAHCONST.generatePath(EAHCONST.RIDERS_POSITIONS_SUBTREE, currentUser.getUid());
    
            DatabaseReference dbRef1 = dbRef.child(riderOrderPath);
    
            dbRef1.removeValue((databaseError, databaseReference) -> {
                if (databaseError == null)
                    Log.d(TAG, "Successfully removed rider's position from the db");
                else
                    Log.e(TAG, "Error removing rider's position from the db: " + databaseError.getMessage());
            });
    
        }
        
        Log.d(TAG, "Service stop");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    
    private void startLocation() {
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        locationListener = new LocationListener() {
            
            @Override
            public void onLocationChanged(Location location) {
                uploadLocation(location);
            }
            
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            
            }
            
            @Override
            public void onProviderEnabled(String s) {
            
            }
            
            @Override
            public void onProviderDisabled(String s) {
            
            }
            
        };
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            // we have permission!
            
            // ///Criteria //////////
            Criteria crta = new Criteria();
            crta.setAccuracy(Criteria.ACCURACY_MEDIUM);
            crta.setPowerRequirement(Criteria.POWER_LOW);
            
            String provider = locationManager.getBestProvider(crta, true);
            locationManager.requestLocationUpdates(provider, 30000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            uploadLocation(location);
            
        }
        
    }
    
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void uploadLocation(Location location) {
        String riderOrderPath = EAHCONST.generatePath(
                EAHCONST.RIDERS_POSITIONS_SUBTREE);
        
        lastLocation = location;
    
        DatabaseReference dbRef1 = dbRef.child(riderOrderPath);
        GeoFire geoFire = new GeoFire(dbRef1);
        geoFire.setLocation(currentUser.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
            if (error != null) {
                Log.e(TAG,"There was an error saving the location to GeoFire: " + error);
            } else {
                Log.d(TAG,"Location saved on server successfully!");
            }
        });
    }
    
    public static RiderLocationService getInstance() {
        return instance;
    }
    
    public Location getLastLocation() {
        return lastLocation;
    }
}
