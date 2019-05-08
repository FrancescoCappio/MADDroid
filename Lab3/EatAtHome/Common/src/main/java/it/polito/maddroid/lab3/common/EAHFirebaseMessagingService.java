package it.polito.maddroid.lab3.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class EAHFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FirebaseMessageService";
    
    private static final String CHANNEL_ID = "MY_PUSH_NOTIFICATION_CHANNEL";
    
    private static final String CHANNEL_NAME = "Eat@Home notifications";
    private static final String CHANNEL_DESC = "Notification channel used for events";
    
    public static void setActivityToLaunch(Class<?> activityToLaunch) {
        EAHFirebaseMessagingService.activityToLaunch = activityToLaunch;
    }
    
    private static Class<?> activityToLaunch;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;
    
    public EAHFirebaseMessagingService() {
        mAuth = FirebaseAuth.getInstance();
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        createNotificationChannel();
    
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
    
            sendNotification(remoteMessage.getNotification());
        }
    }
    
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            // user not authenticated
            return;
        }
        
        dbRef = FirebaseDatabase.getInstance().getReference();
        
        Utility.storeToken(dbRef, currentUser.getUid(), token);
    }
    
    
    private void sendNotification(RemoteMessage.Notification remoteNotification) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(remoteNotification.getTitle())
                .setContentText(remoteNotification.getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    
        // Create an explicit intent for an Activity in your app
        
        if (activityToLaunch == null) {
            Log.e(TAG, "Cannot start null activity");
        } else {
            Intent intent = new Intent(this, activityToLaunch);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentIntent(pendingIntent);
        }
    
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(Utility.getRandomNumberInRange(1, 1000), builder.build());
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
    
}
