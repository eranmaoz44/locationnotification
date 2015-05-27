//package ssdl.technion.ac.il.locationnotification.activities;
//
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.support.v7.app.ActionBarActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
//import com.google.android.gms.common.GooglePlayServicesRepairableException;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.Geofence;
//import com.google.android.gms.location.GeofencingRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlacePicker;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import ssdl.technion.ac.il.locationnotification.R;
//import ssdl.technion.ac.il.locationnotification.services.GeofenceTransitionsIntentService;
//import ssdl.technion.ac.il.locationnotification.services.GeofencingService;
//import ssdl.technion.ac.il.locationnotification.services.MyApplication;
//import ssdl.technion.ac.il.locationnotification.utilities.GeofencingUtils;
//
//
//public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
//
//    public static GoogleApiClient mGoogleApiClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Intent intent = new Intent(this, GeofencingService.class);
//        startService(intent);
//
//
////        buildGoogleApiClient();
//
//    }
//
//    /**
//     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
//     */
//    protected synchronized void buildGoogleApiClient() {
//        if(null == mGoogleApiClient)
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
////        if(mGoogleApiClient.isConnected()==false)
////            mGoogleApiClient.connect();
//
//
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    public void onClickAddReminder(View v){
//        Intent intent=new Intent(this,AddReminderActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickViewReminders(View v) {
//        Intent intent = new Intent(this, ViewRemindersActivity.class);
//        startActivity(intent);
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        Log.v("GoogleApi", "Connected");
//        //TODO: fix
//        Intent intent = new Intent(this, GeofencingService.class);
//        startService(intent);
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.v("GoogleApi", "Connection suspended");
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.v("GoogleApi", "Connection failed +" +connectionResult);
//    }
//
//    public void onClickPickPlace(View v){
//        Intent intent = new Intent(getApplicationContext(), GeofencingService.class);
//        startService(intent);
//    }
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
////        int PLACE_PICKER_REQUEST = 1;
////        if (requestCode == PLACE_PICKER_REQUEST) {
////            if (resultCode == RESULT_OK) {
////                Place place = PlacePicker.getPlace(data, this);
////                String toastMsg = String.format("Place: %s", place.getName());
////                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
////                List<Geofence> mGeofenceList=new ArrayList<Geofence>();
////                Geofence geofence;
////                geofence=new Geofence.Builder()
////                        // Set the request ID of the geofence. This is a string to identify this
////                        // geofence.
////                        .setRequestId(place.getId())
////
////                        .setCircularRegion(
////                                place.getLatLng().latitude,
////                                place.getLatLng().longitude,
////                                256
////                        )
////                        .setExpirationDuration(10800000)
////                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
////                        .build();
////                mGeofenceList.add(geofence);
////                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
////                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
////                builder.addGeofences(mGeofenceList);
////                GeofencingRequest geofencingRequest=builder.build();
////                Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
////                // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
////                // calling addGeofences() and removeGeofences().
////                PendingIntent pendingIntent=PendingIntent.getService(this, 0, intent, PendingIntent.
////                        FLAG_UPDATE_CURRENT);
////                Log.v("GoogleApi","mGoogleApiClient.isConnected()="+mGoogleApiClient.isConnected());
////                LocationServices.GeofencingApi.addGeofences(
////                        mGoogleApiClient,
////                        geofencingRequest,
////                        pendingIntent
////                ).setResultCallback(this);
////            }
////        }
//    }
//
//    @Override
//    public void onResult(Status status) {
//
//    }
//}
