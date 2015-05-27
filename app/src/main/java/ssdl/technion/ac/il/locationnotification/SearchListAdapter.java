package ssdl.technion.ac.il.locationnotification;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.MyLocation;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;

/**
 * Created by nir on 26/05/2015.
 */
public class SearchListAdapter extends BaseAdapter implements View.OnClickListener {
    Activity activity;
    List<Reminder> dataSet, showing;
    public String query = "";
    public Boolean containingPic = false, containingLoc = false, sortOnAbc = true;
    private Location location;


    public SearchListAdapter(Activity activity, List<Reminder> dataSet) {
        this.dataSet = dataSet;
        this.activity = activity;
        showing = new ArrayList<>(dataSet);
    }

    @Override
    public int getCount() {
        return showing.size();
    }

    @Override
    public Object getItem(int position) {
        return showing.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View $;
        ViewTag vt;
        if (convertView == null) {
            $ = activity.getLayoutInflater().inflate(R.layout.search_item, null);
            $.setOnClickListener(this);

            vt = new ViewTag();
            vt.text = (TextView) $.findViewById(R.id.tv_si_title);
            vt.dist = (TextView) $.findViewById(R.id.tv_si_dist);
            vt.pic = (ImageView) $.findViewById(R.id.iv_si_image);

            $.setTag(vt);
        } else {
            $ = convertView;
            vt = (ViewTag) $.getTag();
        }
        vt.r = showing.get(position);
        vt.text.setText(vt.r.getTitle());
        //picture
        File image = new File(vt.r.getImgPath());
        if(image.exists()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            vt.pic.setImageBitmap(bitmap);
        } else {
            vt.pic.setImageResource(R.drawable.image_3);
        }

        //location
        if(location != null) {
            MyLocation l = vt.r.getLocation();
            int dist = (int) getDistance(l, location);
            vt.dist.setText( dist < 1000 ? (dist + "m") : ((dist/1000)+ "km"));
            if(l.getLatitude() == 0 && l.getLongitude() == 0)
                vt.dist.setText("Not set");
        }
        return $;
    }

    private float getDistance(MyLocation location, Location curr) {
        Location l = new Location(curr);
        l.setLatitude(location.getLatitude());
        l.setLongitude(location.getLongitude());
        return l.distanceTo(curr);
    }


    public void sortOnDist() {
        sortOnAbc = false;
    }

    public void sortOnAbc() {
        sortOnAbc = true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(activity, UserDetailsActivity.class);
        intent.putExtra(Constants.REMINDER_TAG, ((ViewTag)v.getTag()).r);
        activity.startActivity(intent);
    }

    public void setLocation(Location location) {
        this.location = location;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        showing.clear();
        for (Reminder r : dataSet) {
            if (/*!s.toString().equals("") &&*/ r.getTitle().contains(query) || r.getMemo().contains(query)) {
                if (containingLoc && r.getLocation().getLongitude() == 0 && r.getLocation().getLatitude() == 0)
                    continue;
                File image = new File(r.getImgPath());
                if (containingPic && !image.exists())
                    continue;
                showing.add(r);
            }
        }
        if(sortOnAbc){
            Collections.sort(showing, new Comparator<Reminder>() {
                @Override
                public int compare(Reminder lhs, Reminder rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });
        }else{//sort on distance!!
            Collections.sort(showing, new Comparator<Reminder>() {
                @Override
                public int compare(Reminder lhs, Reminder rhs) {
                    double ldis = getDistance(lhs.getLocation(), location);
                    double rdis = getDistance(rhs.getLocation(), location);
                    return ldis == rdis ? 0 : (ldis > rdis ? 1 : -1);

                }
            });
        }
        super.notifyDataSetChanged();
    }

    private class ViewTag {
        TextView text, dist;
        ImageView pic;
        Reminder r;
    }
}