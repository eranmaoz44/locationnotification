package ssdl.technion.ac.il.locationnotification.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.MainActivity;
import ssdl.technion.ac.il.locationnotification.R;
import ssdl.technion.ac.il.locationnotification.UserDetailsActivity;
import ssdl.technion.ac.il.locationnotification.utilities.GeofencingUtils;
import ssdl.technion.ac.il.locationnotification.utilities.MyLocation;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class GeofencingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private SQLUtils sqlUtils;

    private long lastUpdateTime;
    private int lastInterval;
    private Location lastLocation;
    private GeofencingUtils geofencingUtils;

    private boolean startedGpsTracking;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("MyTest", "onCreate service");
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        sqlUtils = new SQLUtils(this);
    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected void buildGoogleApiClient() {
        if (null == mGoogleApiClient)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.v("MyTest", "service onConnected");

        geofencingUtils = new GeofencingUtils(this, mGoogleApiClient);
        startGeofencing();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("MyTest", "onStartCommand");

        if (mGoogleApiClient.isConnected()) {
            Log.v("MyTest", "onStartCommand and google is connected");
            startGeofencing();
        }

        return START_REDELIVER_INTENT;
    }

    protected LocationRequest createLocationRequest(Location currLocation) {
        Log.v("MyTest", "createLocationRequest lastLocationAccuracy=" + (lastLocation != null ? lastLocation.getAccuracy() : "nul") + "currLocationAccuracy=" + (currLocation != null ? currLocation.getAccuracy() : "nul"));

        if (null == lastLocation || null == currLocation || lastLocation.getAccuracy() > Constants.ACCEPTABLE_ACCURAY || currLocation.getAccuracy() > Constants.ACCEPTABLE_ACCURAY) { // system not stable yet,
            return null;
        }

        float distance = lastLocation.distanceTo(currLocation);
        Log.v("MyTest", "distance=" + distance);
        if (distance < Constants.MIN_DISTANCE_BETWEEN_UPDATES) {
            return null;
        }

        lastLocation = currLocation;

        long currTime = System.currentTimeMillis();
        long difference = currTime - lastUpdateTime;
        Log.v("MyTest", "difference=" + difference);
        if (difference < lastInterval) {
            return null;
        }
        lastUpdateTime = currTime;


        float closestDistance = getClosestDistance(currLocation);
        if (-1.0 == closestDistance) {
            return null;
        }

        int waitingInterval = distanceToWaitingInterval(closestDistance);
        lastInterval = waitingInterval;

        Log.v("MyTest", "closestDistance=" + closestDistance + " waitingInterval=" + waitingInterval);

        Log.v("LocationUpdates", "waitingInterval=" + waitingInterval);

        LocationRequest request = new LocationRequest();
        request.setInterval(lastInterval);
        request.setSmallestDisplacement(Constants.MIN_DISTANCE_BETWEEN_UPDATES);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return request;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.v("MyTest", "Service Destroyed");
        super.onDestroy();
    }

    protected void updateLocationUpdates(LocationRequest request) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

//    @Override
//    public void onGpsStatusChanged(int event)
//    {
//
//        switch(event)
//        {
//            case GpsStatus.GPS_EVENT_STARTED:
//
////                geofencingUtils.updateGeofences();
//
//                Log.v("GPS","GPS_EVENT_STARTED");
//
//
//                break;
//            case GpsStatus.GPS_EVENT_STOPPED:
//
//                Log.v("GPS","GPS_EVENT_ENDED");
//
//                break;
//        }
//    }

    private void updateGpsSamplingRate(Location location) {

        LocationRequest request = createLocationRequest(location);

        Log.v("MyTest", "changedNeeded=" + (request != null ? "yes" : "no"));

        if (null != request) {
            updateLocationUpdates(request);
        }
    }

    private int distanceToWaitingInterval(float closestDistance) {
        if (closestDistance < 0) {
            return 10 * 60;
        }
        float distanceInKm = closestDistance / 1000;
        int waitingInterval;
        if (distanceInKm > 100) {
            waitingInterval = 10 * 60;
        } else if (distanceInKm > 10) {
            waitingInterval = 2 * 60;
        } else if (distanceInKm > 5) {
            waitingInterval = 1 * 60;
        } else if (distanceInKm > 1) {
            waitingInterval = 30;
        } else if (distanceInKm > 0.5) {
            waitingInterval = 10;
        } else {
            waitingInterval = 2;
        }
        return waitingInterval * 1000;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (null == lastLocation && null != location && location.getAccuracy() <= Constants.ACCEPTABLE_ACCURAY) {
            lastLocation = location;
        }
        Log.v("MyTest", "locationChanged");
        if (location != null && location.getAccuracy() <= Constants.ACCEPTABLE_ACCURAY && lastLocation != null) {
            Log.v("MyTest", "entered condition checking whether to send notificatios");
//            geofencingUtils.updateGeofences(location);
            for (Reminder r : sqlUtils.getReminderList()) {
                Log.v("MyTest", "trying to send not to" + r.getTitle());
                MyLocation rLoc = r.getLocation();
                int rRadius = rLoc.getRadius();
                Log.v("MyTest", "distance between curr loc=" + getDistance(location, rLoc));
                Log.v("MyTest", "distance between last loc=" + getDistance(lastLocation, rLoc));
                if (r.isActive() && getDistance(location, rLoc) <= rRadius && getDistance(lastLocation, rLoc) >= rRadius) {
                    Log.v("MyTest", "sending not to" + r.getTitle());
                    sendNotification(r);
                }
            }
        }
        updateGpsSamplingRate(location);
    }

    private float getDistance(Location l, MyLocation m) {
        Location l2 = new Location(l);
        l2.setLatitude(m.getLatitude());
        l2.setLongitude(m.getLongitude());
        return l.distanceTo(l2);
    }

    private void sendNotification(Reminder r) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), UserDetailsActivity.class);

        notificationIntent.putExtra(Constants.REMINDER_TAG, r);

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

        Bitmap icon = null;
        //get picture
        File image = new File(r.getImgPath());
        if(image.exists()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            icon = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        } else {
            icon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);
        }
        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setContentTitle(r.getTitle())
                .setContentText(r.getMemo())
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

    float getClosestDistance(Location currLocation) {
        if (null == currLocation) {
            return 0;
        }
        float minDistance = -1;
        for (Reminder r : sqlUtils.getReminderList()) {
            if (!r.isActive()) {
                Log.v("MyTest", r.toString());
                continue;
            }
            Location loc = new Location("");
            loc.setLatitude(r.getLocation().getLatitude());
            loc.setLongitude(r.getLocation().getLongitude());
            float dist = loc.distanceTo(currLocation);
            if (minDistance == -1 || dist < minDistance) {
                minDistance = dist;
            }
        }
        return minDistance;
    }

    void startGeofencing() {
        Log.v("MyTest", "startGeofencing");
        lastInterval = 0;
        lastUpdateTime = System.currentTimeMillis();
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startGpsSampling();
//        geofencingUtils.updateGeofences(lastLocation);
//        startingLocation
    }

    private void startGpsSampling() {
        Log.v("MyTest", "startGpsSamling()");
        LocationRequest request = new LocationRequest();
        request.setInterval(2000);
        request.setSmallestDisplacement(Constants.MIN_DISTANCE_BETWEEN_UPDATES);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
    }
}
