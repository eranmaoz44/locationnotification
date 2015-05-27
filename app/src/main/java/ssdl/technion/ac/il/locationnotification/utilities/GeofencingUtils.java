package ssdl.technion.ac.il.locationnotification.utilities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;

import ssdl.technion.ac.il.locationnotification.services.GeofenceTransitionsIntentService;

import static junit.framework.Assert.assertTrue;

/**
* Created by Eran on 5/20/2015.
*/
public class GeofencingUtils implements ResultCallback<Status> {
    PendingIntent mGeofencePendingIntent;
    SQLUtils sqlUtils;
    Context context;
    private GoogleApiClient mGoogleApiClient;

    public GeofencingUtils(Context context,GoogleApiClient googleApiClient){
        this.context=context;
        sqlUtils=new SQLUtils(context);
        mGoogleApiClient=googleApiClient;
    }
    private GeofencingRequest getGeofencingRequest() {
        List<Geofence> geofences=getGeofences();
        if(geofences.isEmpty()){
            return null;
        }
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(geofences);

        // Return a GeofencingRequest.
        return builder.build();
    }
    private List<Geofence> getGeofences(){
        List<Reminder> reminders=sqlUtils.getReminderList();
        List<Geofence> geofences=new ArrayList<>();
        for(Reminder r :reminders){
            if(r.isActive()) {
                Log.v("GeofencingUtils", r.toString());
                geofences.add(createGeofence(r));
            }

        }
        return geofences;
    }

    private Geofence createGeofence(Reminder reminder){
        return new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(reminder.getId())

                        // Set the circular region of this geofence.
                .setCircularRegion(
                        reminder.getLocation().getLatitude(),
                        reminder.getLocation().getLongitude(),
                        reminder.getLocation().getRadius()
                )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                        // Create the geofence.
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent=PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    public void updateGeofences() {
        if (!mGoogleApiClient.isConnected()) {
//            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
//            return;
            //TODO: deal with error
        }

        try {
            Log.v("Geofencing", "goefencesUpdated");
            GeofencingRequest geofencingRequest=getGeofencingRequest();
            if(geofencingRequest==null){
                return;
            }

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    geofencingRequest,
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//            logSecurityException(securityException);
            //TODO: deal with error
            assertTrue(0==1);
        }
    }

    public void removeGeofences(){
        if(null != mGeofencePendingIntent) {
            Log.v("Geofencing", "geofences removed");
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofencePendingIntent);
        }
    }


    @Override
    public void onResult(Status status) {
        Log.v("GeofencingUtils",status.toString());
    }
}
