package ssdl.technion.ac.il.locationnotification.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.R;
import ssdl.technion.ac.il.locationnotification.UserDetailsActivity;
import ssdl.technion.ac.il.locationnotification.utilities.MyLocation;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class ShowOnMapActivity extends Activity implements GoogleMap.OnMapLoadedCallback, GoogleMap.OnInfoWindowClickListener {
    GoogleMap map;
    List<Reminder> dataSet;
    Map<Marker, Reminder> markerToReminder;
    Location currLoc;
    final String CURRENT_LOCATION_MARKER="CurrentLocationMarker";

    private List<Reminder> getList() {
        SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
        return sqlUtils.getReminderList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_on_map);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMapLoadedCallback(this);
        map.setOnInfoWindowClickListener(this);
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                // Can't make marker unclickable, so this is a test whether its the curr loc marker
                if(!markerToReminder.containsKey(marker)){
                    return null;
                }
                View $ = getLayoutInflater().inflate(R.layout.map_info_window, null);
                Reminder r = markerToReminder.get(marker);
                ((TextView) $.findViewById(R.id.tv_miw_text)).setText(r.getTitle());
                //image
                ImageView imageView = (ImageView)$.findViewById(R.id.iv_miw_image);
                File image = new File(r.getImgPath());
                if(image.exists()){
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.image_3);
                }

                return $;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        dataSet = getList();
        currLoc=getIntent().getParcelableExtra(Constants.LOCATION_TAG);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_on_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapLoaded() {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        markerToReminder = new HashMap<>();
        for (Reminder r : dataSet) {
            MyLocation l = r.getLocation();
            if (l.getLatitude() == 0 && l.getLongitude() == 0)
                continue;
            LatLng loc = new LatLng(l.getLatitude(), l.getLongitude());
            Marker m = map.addMarker(new MarkerOptions()
                    .position(loc)
                            //.snippet(r.getMemo()) TODO
                            //.icon(BitmapDescriptorFactory.fromFile()) TODO
                    .title(r.getId()));
            markerToReminder.put(m, r);
            map.addCircle(new CircleOptions().center(loc)
                    .radius(r.getLocation().getRadius())//meters
                    .strokeColor(Color.argb(255, 0, 153, 255))
                    .fillColor(Color.argb(30, 0, 153, 255)).strokeWidth(2));
            bounds.include(loc);
        }
        try {
            addCurrLoc(bounds);
            setMinimalBounds(bounds);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 200));
            focusOnCurrLoc();
        } catch (Exception e) {
        }
    }

    private void focusOnCurrLoc() {
        if(null!=currLoc){
            LatLng latLng=new LatLng(currLoc.getLatitude(),currLoc.getLongitude());
            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(latLng);
            map.moveCamera(center);
        }
    }

    private void addCurrLoc(LatLngBounds.Builder bounds) {
        if(null!=currLoc){
            LatLng latLng=new LatLng(currLoc.getLatitude(),currLoc.getLongitude());
            map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.current_location))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
            int radius= Constants.RADIUS;
            map.addCircle(new CircleOptions().center(latLng)
                    .radius(radius)//meters
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.GREEN).strokeWidth(2));
            bounds.include(latLng);
        }
    }

    private void setMinimalBounds(LatLngBounds.Builder bounds) {
        final double HEADING_NORTH_EAST = 45;
        final double HEADING_SOUTH_WEST = 215;
        LatLng center = bounds.build().getCenter();
        LatLng northEast = SphericalUtil.computeOffset(center, 709, HEADING_NORTH_EAST);
        LatLng southWest = SphericalUtil.computeOffset(center, 709,HEADING_SOUTH_WEST );
        bounds.include(northEast);
        bounds.include(southWest);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Reminder r = markerToReminder.get(marker);
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(Constants.REMINDER_TAG, r);
        startActivity(intent);
    }
}
