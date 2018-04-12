package dederides.firebaseapp.com.dederides;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class DedeMessagingService extends FirebaseMessagingService {

    private static final String TAG = "DedeMessagingService";

    public DedeMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        JSONObject data;
        String eventID = null;
        String notificationType = null;
        String title = null ;
        String message = null;

        /* Get Notification Data */
        if ( remoteMessage.getData().size() > 0 ) {
            Log.d( TAG, "Message Data: " + remoteMessage.getData() );

            try {

                data = new JSONObject( remoteMessage.getData() );

                /* Get notification type */
                notificationType = data.getString( "notificationType" );
                Log.d( TAG, "onMessageReceived: \n"
                        + "notificationType: " + notificationType );

                /* Get possible event ID */
                eventID = data.getString( "eventID" );
                Log.d( TAG, "onMessageReceived: \n"
                        + "eventID: " + eventID );


            } catch ( JSONException e ) {
                e.printStackTrace();
            }
        }

        /* Get Notification Fields */
        if ( remoteMessage.getNotification() != null ) {

            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();

            Log.d( TAG, "Notification Title: " + title );
            Log.d( TAG, "Notification Message: " + message );

        }

        if( notificationType == null ) {
            sendNotification( title, message );
        } else if ( notificationType.equals( "queueUpdatedNotification" ) && eventID != null ) {
            displayQueueUpdatedNotification( eventID );
        } else {
            sendNotification( title, message );
        }


    }

    @Override
    public void onDeletedMessages() {



    }

    private void sendNotification(String title,  String messageBody ) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString( R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId )
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void displayQueueUpdatedNotification( final String eventID ) {

        FirebaseDatabase.getInstance().getReference().child( "events" ).child( eventID )
                .child( "name" ).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String eventName = dataSnapshot.getValue( String.class );

                Intent intent = new Intent(DedeMessagingService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        DedeMessagingService.this,
                        0 /* Request code */,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT
                );

                String channelId = getString( R.string.default_notification_channel_id);
                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(DedeMessagingService.this, channelId )
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle( eventName + " Queue Updated" )
                                .setContentText( "There is a new rider in the " + eventName + " queue." )
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager == null ) {

                    Log.e( TAG, "Error with Notification Manager" );
                    return;
                }

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    NotificationChannel channel = new NotificationChannel(channelId,
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
