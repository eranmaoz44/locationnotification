package ssdl.technion.ac.il.locationnotification;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.melnykov.fab.FloatingActionButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.activities.SearchActivity;
import ssdl.technion.ac.il.locationnotification.activities.ShowOnMapActivity;
import ssdl.technion.ac.il.locationnotification.services.GeofencingService;
import ssdl.technion.ac.il.locationnotification.utilities.MyLocation;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Toolbar toolBar;
    private FloatingActionButton fab;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolBar = (Toolbar) findViewById(R.id.tool_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddReminder();
            }
        });

        setSupportActionBar(toolBar);

        Intent intent = new Intent(this, GeofencingService.class);
        startService(intent);

        buildGoogleApiClient();

        //facebook
        final List<String> permissions = Arrays.asList("user_friends");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (user != null)
                    GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                            try {
                                ParseInstallation.getCurrentInstallation().put("FacebookId", jsonObject.get("id"));
                                ParseInstallation.getCurrentInstallation().saveInBackground();

                                user.put("FacebookId", jsonObject.get("id"));
                                user.put("name", jsonObject.get("name"));
                                user.saveInBackground();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).executeAsync();
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void buildGoogleApiClient() {
        if (null == mGoogleApiClient)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

    }

    public void attachList(RecyclerView recyclerView) {
        fab.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.addReminder:
                startAddReminder();
                break;
            case R.id.search_button:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.action_show_on_map:
                startActivity(new Intent(this, ShowOnMapActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void startAddReminder() {
        String imageUri = "drawable://";
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        Location location = null;
        if (mGoogleApiClient.isConnected()) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        MyLocation myLocation = null;
        if (null != location) {
            myLocation = new MyLocation(location.getLatitude(), location.getLongitude(), Constants.RADIUS);

        } else {
            myLocation = new MyLocation(-1.0, -1.0, -1);
        }

        Reminder r = new Reminder(true, "", imageUri, false, date, date, Constants.NEW_REMINDER_ID, myLocation, "");
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(Constants.REMINDER_TAG, r);
        startActivity(intent);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        postponeEnterTransition();
        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        postponeEnterTransition();
//        final View decor = getWindow().getDecorView();
//        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                decor.getViewTreeObserver().removeOnPreDrawListener(this);
//                startPostponedEnterTransition();
//                return true;
//            }
//        });
//    }
}
