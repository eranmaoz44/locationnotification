package ssdl.technion.ac.il.locationnotification.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import ssdl.technion.ac.il.locationnotification.R;
import ssdl.technion.ac.il.locationnotification.SearchListAdapter;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class SearchActivity extends ActionBarActivity implements TextWatcher, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    ImageButton sortDist, sortABC, contPic, contLoc;
    ListView searchResults;
    EditText searchQuery;
    SearchListAdapter adapter;
    private GoogleApiClient mGoogleApiClient;

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
        contLoc = (ImageButton) findViewById(R.id.ib_contains_loc);
        contPic = (ImageButton) findViewById(R.id.ib_contains_pic);
        searchResults = (ListView) findViewById(R.id.lv_search_results);
        searchQuery = (EditText) findViewById(R.id.et_phone_home);

        searchQuery.addTextChangedListener(this);
        adapter = new SearchListAdapter(this, getList());
        searchResults.setAdapter(adapter);

        sortABC.setOnClickListener(this);
        sortDist.setOnClickListener(this);
        contPic.setOnClickListener(this);
        contLoc.setOnClickListener(this);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_contains_loc:
                adapter.containingLoc = !adapter.containingLoc;
                break;
            case R.id.ib_contains_pic:
                adapter.containingPic = !adapter.containingPic;
                break;
            case R.id.ib_sort_abc:
                adapter.sortOnAbc();
                break;
            case R.id.ib_sort_dist:
                adapter.sortOnDist();
                break;
        }

        adapter.notifyDataSetChanged();
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
}
