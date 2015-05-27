//package ssdl.technion.ac.il.locationnotification.services;
//
//import android.app.Application;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationServices;
//
//import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;
//
///**
// * Created by Eran on 4/12/2015.
// */
//public class MyApplication extends Application {
//
////    private GoogleApiClient mGoogleApiClient;
//
//    private SQLUtils sqlUtils;
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
////        buildGoogleApiClient();
//
//        sqlUtils=new SQLUtils(getApplicationContext());
//    }
//
//    public SQLUtils getSQL(){
//        return sqlUtils;
//    }
////
////    public GoogleApiClient getGoogleApiClient() { return mGoogleApiClient;}
//
//
////
////    protected synchronized void buildGoogleApiClient() {
////            mGoogleApiClient = new GoogleApiClient.Builder(this)
////                    .addConnectionCallbacks(this)
////                    .addOnConnectionFailedListener(this)
////                    .addApi(LocationServices.API)
////                    .build();
////
////    }
////
////    @Override
////    public void onConnected(Bundle bundle) {
////
////        Intent intent = new Intent(this, GeofencingService.class);
////        startService(intent);
////    }
////
////    @Override
////    public void onConnectionSuspended(int i) {
////
////    }
////
////    @Override
////    public void onConnectionFailed(ConnectionResult connectionResult) {
////
////    }
//
//}
