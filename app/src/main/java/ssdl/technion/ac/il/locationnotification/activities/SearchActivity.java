package ssdl.technion.ac.il.locationnotification.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.internal.TrashResourceRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.R;
import ssdl.technion.ac.il.locationnotification.SearchListAdapter;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class SearchActivity extends ActionBarActivity implements TextWatcher, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    ImageButton sortDist, sortABC, contPic;
//    ImageButton contLoc;
    ListView searchResults;
    EditText searchQuery;
    SearchListAdapter adapter;
    private GoogleApiClient mGoogleApiClient;
    public static final int REMINDER_REQUEST_CODE = 32032;

    float OPAQUE=1.0f;
    float TRANSPARENT=0.4f;

    private List<Reminder> getList() {
        SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
        return sqlUtils.getReminderList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        sortABC = (ImageButton) findViewById(R.id.ib_sort_abc);
        sortDist = (ImageButton) findViewById(R.id.ib_sort_dist);
//        contLoc = (ImageButton) findViewById(R.id.ib_contains_loc);
        contPic = (ImageButton) findViewById(R.id.ib_contains_pic);
        searchResults = (ListView) findViewById(R.id.lv_search_results);
        searchQuery = (EditText) findViewById(R.id.et_phone_home);

        searchQuery.addTextChangedListener(this);
        adapter = new SearchListAdapter(this, getList());
        searchResults.setAdapter(adapter);

        sortABC.setOnClickListener(this);
        sortDist.setOnClickListener(this);
        contPic.setOnClickListener(this);
        sortABC.setAlpha(OPAQUE);
        sortDist.setAlpha(TRANSPARENT);
        contPic.setAlpha(TRANSPARENT);
//        sortABC.addOnTouchListener(new ButtonHighlighterOnTouchListener(sortABC));
//        sortDist.setOnTouchListener(new ButtonHighlighterOnTouchListener(sortDist));
//        contPic.setOnTouchListener(new ButtonHighlighterOnTouchListener(contPic));
//        contLoc.setOnClickListener(this);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.ib_contains_loc:
//                adapter.containingLoc = !adapter.containingLoc;
//                break;
            case R.id.ib_contains_pic:
                toggleAlpha(contPic);
                v.invalidate();
                adapter.containingPic = !adapter.containingPic;
                break;
            case R.id.ib_sort_abc:
                sortABC.setAlpha(OPAQUE);
                sortDist.setAlpha(TRANSPARENT);
                v.invalidate();
                adapter.sortOnAbc();
                break;
            case R.id.ib_sort_dist:
                final Location currLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if(null!=currLocation){
                    sortDist.setAlpha(OPAQUE);
                    sortABC.setAlpha(TRANSPARENT);
                    v.invalidate();
                    adapter.sortOnDist(currLocation);
                }else {
                    Toast.makeText(this,getString(R.string.SORT_BY_DISTANCE_ERROR),Toast.LENGTH_SHORT).show();
                }
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private void toggleAlpha(ImageButton ib) {
        if (ib.getAlpha()==TRANSPARENT)
            ib.setAlpha(OPAQUE);
        else
            ib.setAlpha(TRANSPARENT);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        adapter.query = s.toString();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

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
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation == null)
                return;
            adapter.setLocation(lastLocation);
        }
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
        switch(requestCode){
            case REMINDER_REQUEST_CODE:
                if(Activity.RESULT_OK==resultCode){
                    List<Reminder> l=getList();
                    Log.v("SearchRefresh", l.toString());
                    adapter.setList(l);
                }
                break;
        }
    }

    public class ButtonHighlighterOnTouchListener implements View.OnTouchListener {

        final ImageButton imageButton;

        public ButtonHighlighterOnTouchListener(final ImageButton imageButton) {
            super();
            this.imageButton = imageButton;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    // Your action here on button click

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.getBackground().clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        }

    }
}
