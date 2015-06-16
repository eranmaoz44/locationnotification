package ssdl.technion.ac.il.locationnotification;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;

import com.facebook.AccessToken;
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

    Reminder r;

    ColorDrawable cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        Reminder reminder = getIntent().getParcelableExtra(Constants.REMINDER_TAG);
        r = reminder;


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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
                sqlUtils.deleteData(r.getId());
                finish();
                break;
            case R.id.action_share:
                final Dialog shareDialog = new Dialog(this);
                shareDialog.setContentView(R.layout.popup_share);
                shareDialog.setTitle("Share to:");
                shareDialog.show();
                GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                        shareDialog.findViewById(R.id.pb_share_wait).setVisibility(View.GONE);
                        ((ListView)shareDialog.findViewById(R.id.lv_share_friends)).setAdapter(new ShareListAdapter(getApplicationContext(), jsonArray));
                    }
                });
                request.executeAsync();
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void setToolBarAlpha(int alpha) {
        cd.setAlpha(alpha);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBus.getInstance().postQueue(
                new ActivityResultEvent(requestCode, resultCode, data));
    }
}
