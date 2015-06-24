package ssdl.technion.ac.il.locationnotification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.inthecheesefactory.thecheeselibrary.fragment.bus.ActivityResultBus;
import com.inthecheesefactory.thecheeselibrary.fragment.bus.ActivityResultEvent;
import com.parse.ParseFacebookUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class UserDetailsActivity extends ActionBarActivity implements UserDetailsFragment.OnDataReceive {
    private Toolbar toolBar;
    private ImageView iv;

    private MenuItem menuSave;


    ColorDrawable cd;
    private Reminder reminder;
    private boolean startedFromTabletLandTag;
    private UserDetailsFragment fUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);



        if(null!=savedInstanceState){
            startedFromTabletLandTag=savedInstanceState.getBoolean(Constants.STARTED_FROM_TABLET_LAND_TAG);
        } else {
            startedFromTabletLandTag=getIntent().getBooleanExtra(Constants.STARTED_FROM_TABLET_LAND_TAG,false);
        }

        Log.v("UserDetailsOnCreate","startedFromTabletLandTag="+startedFromTabletLandTag);



        Reminder reminder = getIntent().getParcelableExtra(Constants.REMINDER_TAG);
        this.reminder = reminder;



//        UserDetailsFragment userDetailsFragment = new UserDetailsFragment();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(Constants.REMINDER_TAG, r);
//        userDetailsFragment.setArguments(bundle);
//        getFragmentManager().beginTransaction().replace(R.id.user_detatils_container, userDetailsFragment).commit();


        toolBar = (Toolbar) findViewById(R.id.details_toolbar);
        cd = new ColorDrawable(getResources().getColor(R.color.primary));
        // cd.setAlpha(0);
        toolBar.setBackground(cd);

        //toolBar.setBackgroundColor(getResources().getColor(R.color.background_floating_material_dark));
        setSupportActionBar(toolBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setStatusBarColor(getResources().getColor(R.color.background_floating_material_dark));
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
        fUserDetails= (UserDetailsFragment) getFragmentManager().findFragmentById(R.id.f_usersDetails);

    }

//    public Reminder getReminder() {
//        return r;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuSave=menu.findItem(R.id.action_save);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
                sqlUtils.deleteData(reminder.getId());
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.REMINDER_DELETED_TAG,true);
            resultIntent.putExtra(Constants.REMINDER_TAG,reminder);
            setResult(Activity.RESULT_OK, resultIntent);
            onBackPressed();
            return true;
            case R.id.action_save:
                saveReminder();

                return true;
            case R.id.action_share:
                final Dialog shareDialog = new Dialog(this);
                shareDialog.setContentView(R.layout.popup_share);
                shareDialog.setTitle("Share to:");
                shareDialog.show();
                GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                        shareDialog.findViewById(R.id.pb_share_wait).setVisibility(View.GONE);
                        try {
                            ((ListView) shareDialog.findViewById(R.id.lv_share_friends)).setAdapter(new ShareListAdapter(getApplicationContext(), jsonArray, reminder, shareDialog));
                        }catch (FacebookException e){
                            shareDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(UserDetailsActivity.this);
                            builder.setMessage("Please connect to facebook first!");
                            builder.setCancelable(true);
                            builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.connectToFacebook(UserDetailsActivity.this);
                                }
                            });
                            builder.create().show();
                        }
                    }
                });
                request.executeAsync();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
private void saveReminder() {
        Reminder r=fUserDetails.saveReminder();
        if(null==r)
            return;
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.REMINDER_DELETED_TAG,false);
        resultIntent.putExtra(Constants.REMINDER_ADDED_TAG,true);
        resultIntent.putExtra(Constants.REMINDER_TAG,reminder);
        setResult(Activity.RESULT_OK, resultIntent);
        onBackPressed();
    }

    public void setToolBarAlpha(int alpha) {
        cd.setAlpha(alpha);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBus.getInstance().postQueue(
                new ActivityResultEvent(requestCode, resultCode, data));
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public void setSaveButtonVisibility(boolean visibility){
        menuSave.setVisible(visibility);
    }

    @Override
    public Reminder onReminderReceive() {
        return reminder;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Constants.STARTED_FROM_TABLET_LAND_TAG,startedFromTabletLandTag);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(getResources().getBoolean(R.bool.is_tablet_landscape) && startedFromTabletLandTag ){
            onBackPressed();
        }
    }
}
