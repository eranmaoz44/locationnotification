package ssdl.technion.ac.il.locationnotification;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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
import ssdl.technion.ac.il.locationnotification.fragments.ZeroRemindersFragment;
import ssdl.technion.ac.il.locationnotification.services.GeofencingService;
import ssdl.technion.ac.il.locationnotification.utilities.MyLocation;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,MainFragment.OnDataPass,UserDetailsFragment.OnDataReceive {
    private static final String PREV_ORIENT_TAG ="prev.orient.tag" ;
    private static final String STARTED_ACTIVITIY_ON_CREATE_TAG = "started_activity_on_create_tag";
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerFragment drawerFragment;
//    private SelectReminderFragment selectReminderFragment;
    private UserDetailsFragment userDetailsFragment;
    private GoogleApiClient mGoogleApiClient;
    private boolean isEmpty;
    Bundle savedInstanceState;
    private Reminder currReminder;
    private Boolean prevOrientIsLand;
    private Boolean startedActivityOnCreateFlag;
//    private Fragment reminderFragment;
//    private Fragment currReminderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState=savedInstanceState;
        Log.v("ChangeFragments", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);
        toolbar= (Toolbar)findViewById(R.id.tool_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddReminder();
            }
        });

        setSupportActionBar(toolbar);

        if(null!=savedInstanceState){
            startedActivityOnCreateFlag=savedInstanceState.getBoolean(STARTED_ACTIVITIY_ON_CREATE_TAG);
            prevOrientIsLand=savedInstanceState.getBoolean(PREV_ORIENT_TAG);
            currReminder=savedInstanceState.getParcelable(Constants.REMINDER_TAG);
        } else {
            prevOrientIsLand=false;
            startedActivityOnCreateFlag=false;
            currReminder=null;
        }

        Log.v("MainActivity","initial valus: "+"prevOrientIsLand="+prevOrientIsLand+" startedActivityOnCreateFlag="+startedActivityOnCreateFlag+" currReminder="+(currReminder!=null ? currReminder.getTitle() : "null"));

        if(getResources().getBoolean(R.bool.is_tablet_potrait)){
            if(prevOrientIsLand&&null!=currReminder){
                startedActivityOnCreateFlag=true;
                Intent intent = new Intent(this, UserDetailsActivity.class);
                intent.putExtra(Constants.REMINDER_TAG, currReminder);
                intent.putExtra(Constants.STARTED_FROM_TABLET_LAND_TAG,true);
                startActivity(intent);
            }
            prevOrientIsLand=false;
        }

        if (getResources().getBoolean(R.bool.is_tablet_landscape)  ) {
            startedActivityOnCreateFlag=false;
            prevOrientIsLand=true;
        }

        buildGoogleApiClient();
            setupDrawer();
        if(null==savedInstanceState) {
            Intent intent = new Intent(this, GeofencingService.class);
            startService(intent);
            if(ParseUser.getCurrentUser() == null)
                connectToFacebook(this);
        }
    }

    private List<Reminder> getList(){
        SQLUtils sqlUtils=new SQLUtils(this);
        return sqlUtils.getReminderList();
    }

    public void changeToZeroRemindersFragment(){
//        if(currFragment!=zeroRemindersFragment) {
//            Log.v("ChangeFragments","Changing to zero fragment");
//            changeFragment(zeroRemindersFragment);
//            currFragment = zeroRemindersFragment;
//        }
    }

    public void changeToMainFragment(){
//        if(currFragment!=mainFragment) {
//            Log.v("ChangeFragments","Changing to main fragment");
//            changeFragment(mainFragment);
//            currFragment=mainFragment;
//        }
    }

    private void changeFragment(Fragment f) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
//        transaction.replace(R.id.main_fragment_container, f);

        // Commit the transaction
        transaction.commit();
    }

    public static void connectToFacebook(Activity c) {
        //facebook
        final List<String> permissions = Arrays.asList("user_friends");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(c, permissions, new LogInCallback() {
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
                    .addApi(Places.GEO_DATA_API)
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
                Location location = null;
                if (mGoogleApiClient.isConnected()) {
                    location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                Intent intent = new Intent(this, ShowOnMapActivity.class);
                intent.putExtra(Constants.LOCATION_TAG, location);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void startAddReminder() {
        Reminder r = createBlankReminder();
        startedActivityOnCreateFlag=false;
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(Constants.REMINDER_TAG, r);
        intent.putExtra(Constants.STARTED_FROM_TABLET_LAND_TAG,false);
        startActivity(intent);
    }

    public Reminder createBlankReminder() {
        String imageUri = "drawable://";
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

        return new Reminder(true, "", imageUri, false, date, date, Constants.NEW_REMINDER_ID, myLocation, "");
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

    public void hideViews() {
        Log.v("fuck", "shit show");
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fab.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        fab.animate().translationY(fab.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    public void showViews() {
        Log.v("fuck", "shit show");
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    private void toggleTranslateFAB(float slideOffset) {
        fab.setTranslationX(slideOffset * 200);
    }

    public void onDrawerSlide(float slideOffset) {
        toggleTranslateFAB(slideOffset);
    }

    private void setupDrawer() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //setup the NavigationDrawer
        drawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
//        updateFragment();
}

//    public void updateFragment() {
//        if(getList().size()>0)
//            changeToMainFragment();
//        else
//            changeToZeroRemindersFragment();
//    }

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

    @Override
    public void onReminderPass(Reminder r) {
            currReminder = r;
        if(getResources().getBoolean(R.bool.is_tablet_landscape)) {
            UserDetailsFragment fUserDetails = (UserDetailsFragment) getFragmentManager().findFragmentById(R.id.f_usersDetails);
            fUserDetails.setReminder(currReminder);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.REMINDER_TAG,currReminder);
        outState.putBoolean(PREV_ORIENT_TAG,prevOrientIsLand);
        outState.putBoolean(STARTED_ACTIVITIY_ON_CREATE_TAG,startedActivityOnCreateFlag);
    }

    @Override
    public Reminder onReminderReceive() {
        return currReminder;
    }
}
