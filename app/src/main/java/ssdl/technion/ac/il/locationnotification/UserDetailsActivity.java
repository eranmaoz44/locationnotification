package ssdl.technion.ac.il.locationnotification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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


public class UserDetailsActivity extends ActionBarActivity {
    private Toolbar toolBar;
    private ImageView iv;

    private MenuItem menuSave;

    Reminder r;

    ColorDrawable cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);


        Reminder reminder = getIntent().getParcelableExtra(Constants.REMINDER_TAG);
        r = reminder;



        UserDetailsFragment userDetailsFragment = new UserDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.REMINDER_TAG, r);
        userDetailsFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.user_detatils_container, userDetailsFragment).commit();


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

    }

    public Reminder getReminder() {
        return r;
    }

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
                sqlUtils.deleteData(r.getId());
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.REMINDER_DELETED_TAG,true);
            resultIntent.putExtra(Constants.REMINDER_TAG,r);
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
                            ((ListView) shareDialog.findViewById(R.id.lv_share_friends)).setAdapter(new ShareListAdapter(getApplicationContext(), jsonArray, getReminder(), shareDialog));
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
        boolean validated = validateInput();
        if(!validated){
            return;
        }
        SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
        if(0==r.getId().compareTo(Constants.NEW_REMINDER_ID)){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int rId = sharedPref.getInt(Constants.ID_KEY, 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(Constants.ID_KEY, rId + 1);
            editor.commit();
            r.setId(String.valueOf(rId));
            sqlUtils.insertData(r);
            Log.v("SQL", "insertData");
            Toast.makeText(this,getString(R.string.added_successfully), Toast.LENGTH_SHORT).show();
        } else {
            sqlUtils.updateData(r);
            Log.v("SQL", "updateData");
            Toast.makeText(this,getString(R.string.updated_successfully),Toast.LENGTH_SHORT).show();
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.REMINDER_DELETED_TAG,false);
        resultIntent.putExtra(Constants.REMINDER_ADDED_TAG,true);
        resultIntent.putExtra(Constants.REMINDER_TAG,r);
        setResult(Activity.RESULT_OK, resultIntent);
        onBackPressed();
    }

    private boolean validateInput() {
        boolean validated=true;
        EditText etTitle=(EditText)findViewById(R.id.et_edit_title);
        String title=etTitle.getText().toString();
        if(0==title.compareTo("")){
            validated=false;
            etTitle.setError(getString(R.string.title_error_message),getResources().getDrawable(R.drawable.ic_error_white_24dp));
        }
        TextView tvLocation=(TextView)findViewById(R.id.tv_location);
        String location=tvLocation.getText().toString();
        if(0==location.compareTo(getString(R.string.edit_user_pick_location))){
            if(validated)
                tvLocation.requestFocus();
            validated=false;
            tvLocation.setError(getString(R.string.location_error_message),getResources().getDrawable(R.drawable.ic_error_white_24dp));
        }
        if(!validated){
            Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
        }
        return validated;
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
}
