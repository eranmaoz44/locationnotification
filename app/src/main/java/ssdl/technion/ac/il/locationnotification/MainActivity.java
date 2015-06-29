package ssdl.technion.ac.il.locationnotification;


import android.app.Activity;
import android.app.ActivityManager;
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
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

import static junit.framework.Assert.assertTrue;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,MainFragment.OnDataPass,UserDetailsFragment.OnDataReceive {
    private static final String PREV_ORIENT_TAG ="prev.orient.tag" ;
    private static final String STARTED_ACTIVITIY_ON_CREATE_TAG = "started_activity_on_create_tag";
    private static final java.lang.String IN_EDIT_MODE = "in_edit_mode" ;
    public static final int CREATE_REMINDER_TAG = 32013 ;
    private static final int EXTRA_ACTIVITY_TAG = 36669;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerFragment drawerFragment;
//    private SelectReminderFragment selectReminderFragment;
    private UserDetailsFragment userDetailsFragment;
    static GoogleApiClient mGoogleApiClient;
    private boolean isEmpty;
    Bundle savedInstanceState;
    private Reminder currReminder;
    private Boolean prevOrientIsLand;
    private Boolean startedActivityOnCreateFlag;
    private boolean inEditMode;
//    private Fragment reminderFragment;
//    private Fragment currReminderFragment;
    UserDetailsFragment fUserDetails;
    MainFragment fMain;

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
                if(!inEditMode ||!getResources().getBoolean(R.bool.is_tablet_landscape)) {
                    startAddReminder();
                } else {
                    saveReminder();
                }
            }
        });

        setSupportActionBar(toolbar);

        if(null!=savedInstanceState){
            startedActivityOnCreateFlag=savedInstanceState.getBoolean(STARTED_ACTIVITIY_ON_CREATE_TAG);
            prevOrientIsLand=savedInstanceState.getBoolean(PREV_ORIENT_TAG);
            currReminder=savedInstanceState.getParcelable(Constants.REMINDER_TAG);
            inEditMode=savedInstanceState.getBoolean(IN_EDIT_MODE);

        } else {
            prevOrientIsLand=false;
            startedActivityOnCreateFlag=false;
            currReminder=null;
            inEditMode=false;

        }

        Log.v("MainActivity","initial valus: "+"prevOrientIsLand="+prevOrientIsLand+" startedActivityOnCreateFlag="+startedActivityOnCreateFlag+" currReminder="+(currReminder!=null ? currReminder.getTitle() : "null"));

        if(getResources().getBoolean(R.bool.is_tablet_potrait)){
            if(prevOrientIsLand&&null!=currReminder){
                startedActivityOnCreateFlag=true;
                Intent intent = new Intent(this, UserDetailsActivity.class);
                intent.putExtra(Constants.REMINDER_TAG, currReminder);
                intent.putExtra(Constants.STARTED_FROM_MAIN_ACTIVITY,true);
                startActivityForResult(intent,CREATE_REMINDER_TAG);
            }
            prevOrientIsLand=false;
        }

        if (getResources().getBoolean(R.bool.is_tablet_landscape)  ) {
            startedActivityOnCreateFlag=false;
            prevOrientIsLand=true;
        }

        buildGoogleApiClient();
            setupDrawer();
        fUserDetails = (UserDetailsFragment) getFragmentManager().findFragmentById(R.id.f_usersDetails);
        fMain=(MainFragment)getFragmentManager().findFragmentById(R.id.f_usersList);
        if(null==savedInstanceState) {

            if(ParseUser.getCurrentUser() == null)
                connectToFacebook(this);


            Log.v("MyTest","Checking whether service is running or not");
            if(!isMyServiceRunning(GeofencingService.class)){
                Log.v("MyTest","Geofencing service not running, starting it");
                Intent intent = new Intent(this, GeofencingService.class);
                startService(intent);
            }
        } else {
            if(inEditMode && getResources().getBoolean(R.bool.is_tablet_landscape)){
                editOn(currReminder);
            } else {
                editOff(null);
            }
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void saveReminder() {
        Reminder r=fUserDetails.saveReminder();
        Log.v("NewRotateSaveBug","r= "+r);
        if(null==r)
            return;
        fMain.updateRecyclerView();
        editOff(null);
        EditText etTitle = (EditText)fUserDetails.getView().findViewById(R.id.et_edit_title);
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
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
                                Log.v("Facebook","login facebookid = "+jsonObject.get("id"));
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
        showViews();
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
                startActivityForResult(new Intent(this, SearchActivity.class),EXTRA_ACTIVITY_TAG);
                break;
            case R.id.action_show_on_map:
                Location location = null;
                if (mGoogleApiClient.isConnected()) {
                    location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                Intent intent = new Intent(this, ShowOnMapActivity.class);
                intent.putExtra(Constants.LOCATION_TAG, location);
                startActivityForResult(intent, EXTRA_ACTIVITY_TAG);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void startAddReminder() {
        Reminder r = createBlankReminder();
        if(getResources().getBoolean(R.bool.is_tablet_landscape)){
            editOn(r);
        } else {
            startedActivityOnCreateFlag = false;
            Intent intent = new Intent(this, UserDetailsActivity.class);
            intent.putExtra(Constants.REMINDER_TAG, r);
            intent.putExtra(Constants.STARTED_FROM_MAIN_ACTIVITY, true);
            startActivityForResult(intent,CREATE_REMINDER_TAG);
        }


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

        return new Reminder(true, "", imageUri, false, date, date, Constants.NEW_REMINDER_ID, myLocation, "","null");
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
        if(isFabEnabled()) {

            Log.v("fuck", "shit show");
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fab.getLayoutParams();
            hideFab(lp);
        }
    }

    private void hideFab(FrameLayout.LayoutParams lp) {
        int fabBottomMargin = lp.bottomMargin;
        fab.animate().translationY(fab.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    public void showViews() {
        if(isFabEnabled()) {
        Log.v("fuck", "shit show");
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));

            showFab();
        }
    }

    private boolean isFabEnabled() {
        return !getResources().getBoolean(R.bool.is_tablet_landscape);
    }

    private void showFab() {
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
                    Log.v("ReturnFromReminder"," requestCode= " +requestCode+" currReminder ="+ (null==currReminder ? "null" : currReminder.getTitle() ));
        switch(requestCode) {
            case CREATE_REMINDER_TAG:

                if (resultCode==RESULT_OK&&getResources().getBoolean(R.bool.is_tablet_landscape)) {
                    boolean backPressed=data.getBooleanExtra(Constants.BACK_PRESSED_TAG,false);
                    if(backPressed){
                        editOff(null);
                        break;
                    }
                    boolean saved=data.getBooleanExtra(Constants.REMINDER_SAVED_TAG,false);
                    if(saved){
                        editOff(null);
                        break;
                    }
                    Reminder r=data.getParcelableExtra(Constants.REMINDER_TAG);
                    if(null!=r) {
                        fMain.setCurrReminder(r);
                        editOn(r);
                    }
                    return;
                }
                break;
            case EXTRA_ACTIVITY_TAG:
                if(currReminder!=null && getResources().getBoolean(R.bool.is_tablet_landscape)){
                    List<Reminder> reminders=getList();
                    for(Reminder r : reminders){
                        if(r.getId().equals(currReminder.getId())){
                            currReminder=r;
                            fUserDetails.setReminder(currReminder);
                            break;
                        }
                    }
                }
                break;
            default:

                break;
        }
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

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
        if(getResources().getBoolean(R.bool.is_tablet_landscape)) {

            showFab();
            if(null!=r && r.getSenderId().equals("null")) {
                editOn(r);
            } else {
                editOff(r);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.REMINDER_TAG,currReminder);
        outState.putBoolean(PREV_ORIENT_TAG,prevOrientIsLand);
        outState.putBoolean(STARTED_ACTIVITIY_ON_CREATE_TAG,startedActivityOnCreateFlag);
        outState.putBoolean(IN_EDIT_MODE,inEditMode);
    }

    @Override
    public Reminder onReminderReceive() {
        return currReminder;
    }

    private void editOn(Reminder r){
        assertTrue(null!=r);
        currReminder=r;
        if(getResources().getBoolean(R.bool.is_tablet_landscape)) {
            fUserDetails.setReminder(currReminder);
        }
        fab.setImageResource(R.drawable.ic_check_circle_white_24dp);
        inEditMode = true;
    }

    private void editOff(Reminder r){
        currReminder=r;
        Log.v("NewRotateSaveBug","currReminder= "+currReminder);
        Log.v("ReminderEdit","fUserDetails==null ? "+ fUserDetails==null ? "yes"  : "no");
        if(getResources().getBoolean(R.bool.is_tablet_landscape)) {
            Log.v("NewRotateSaveBug","is tablet landspace= ");
            fUserDetails.setReminder(currReminder);
        }

        fab.setImageResource(R.drawable.ic_add_white_24dp);
        inEditMode = false;
    }

    @Override
    public void turnEditingOff() {

    }

    @Override
    public void turnEditingOn() {

    }

}
