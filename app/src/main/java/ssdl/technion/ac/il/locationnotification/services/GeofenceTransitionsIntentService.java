package ssdl.technion.ac.il.locationnotification.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.MainActivity;
import ssdl.technion.ac.il.locationnotification.R;
import ssdl.technion.ac.il.locationnotification.UserDetailsActivity;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;

/**
* Created by Eran on 5/20/2015.
*/
public class GeofenceTransitionsIntentService extends IntentService{
    private final String TAG = "GPS";
    private final SQLUtils sqlUtils;

    public GeofenceTransitionsIntentService() {
        super("asdaskjdhaskdhksa");
        sqlUtils=new SQLUtils(this);
    }



    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
            Log.e("GPS", "GeofenceTransiiosintent error");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            List<Reminder> reminders = getGeofencesReminders(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            for(Reminder r : reminders){
              sendNotification(r);

            }
            Log.i(TAG, reminders.toString());
        } else {
            // Log the error.
            Log.e(TAG, "invalid traisiont type type");
        }
    }

    private List<Reminder> getGeofencesReminders(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {
        return sqlUtils.getRemindersByIds(getIdsFromGeofences(triggeringGeofences));


//        String geofenceTransitionString = getTransitionString(geofenceTransition);

//        // Get the Ids of each geofence that was triggered.
//        ArrayList triggeringGeofencesIdsList = new ArrayList();
//        for ( Reminder r: sqlUtils.getRemindersByIds(getIdsFromGeofences(triggeringGeofences))) {
//            triggeringGeofencesIdsList.add(geofence.getRequestId());
//            reminders.add(r.getName());
//        }
//        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

//        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
//        return transitionDetails;
    }

    private List<String> getIdsFromGeofences(List<Geofence> geofences){
        List<String> ids=new ArrayList<>();
        for(Geofence g : geofences){
            ids.add(g.getRequestId());
        }
        return ids;
    }


    private void sendNotification(Reminder r) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), UserDetailsActivity.class);

        notificationIntent.putExtra(Constants.REMINDER_TAG,r);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(Integer.valueOf(r.getId()), PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(r.getTitle())
                .setContentText(getString(R.string.app_name))
                .setContentIntent(notificationPendingIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(Integer.valueOf(r.getId()), builder.build());
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exited";
            default:
                return "Unkoned geofcne";
        }
    }

}
