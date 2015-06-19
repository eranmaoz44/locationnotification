package ssdl.technion.ac.il.locationnotification.utilities;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Eran on 5/18/2015.
 */
public class Reminder implements Parcelable {

    private final int NUM_OF_FIELDS = 11;

    private Boolean onOff;
    private String title;
    private String imgPath;
    private Boolean alwaysOn;
    private Date dateFrom;
    private Date dateTo;


    private String id;
    private MyLocation location;
    private String memo;

    public Reminder(Boolean onOff, String title, String imgPath, Boolean alwaysOn, Date dateFrom, Date dateTo, String id, MyLocation location, String memo) {
        this.onOff = onOff;
        this.title = title;
        this.imgPath = imgPath;
        this.alwaysOn = alwaysOn;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.id = id;
        this.location = location;
        this.memo = memo;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject $ = new JSONObject();
        $.put("onOff", onOff);
        $.put("title", title);
        $.put("imgPath", imgPath);
        $.put("alwaysOn", alwaysOn);
        $.put("dateFrom", dateFrom.toString());
        $.put("dateTo", dateTo.toString());
        $.put("id", id);
        $.put("location", location.toJson());
        $.put("memo", memo);
        return $;
    }

    public Reminder(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getBoolean("onOff"), jsonObject.getString("title"), jsonObject.getString("imgPath"), jsonObject.getBoolean("alwaysOn"), new Date(jsonObject.getString("dateFrom")),
                new Date(jsonObject.getString("dateTo")), jsonObject.getString("id"), new MyLocation(jsonObject.getJSONObject("location")), jsonObject.getString("memo"));
    }

    //parcel part
    public Reminder(Parcel in) {
        String[] data = new String[NUM_OF_FIELDS];

        in.readStringArray(data);
        this.onOff = Boolean.parseBoolean(data[0]);
        this.title = data[1];
        this.imgPath = data[2];
        this.alwaysOn = Boolean.parseBoolean(data[3]);
        this.dateFrom = stringToDate(data[4]);
        this.dateTo = stringToDate(data[5]);
        this.id = data[6];
        this.location = new MyLocation(Double.parseDouble(data[7]), Double.parseDouble(data[8]), Integer.parseInt(data[9]));
        this.memo = data[10];
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "onOff=" + onOff +
                ", title='" + title + '\'' +
                ", imgPath='" + imgPath + '\'' +
                ", alwaysOn=" + alwaysOn +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", id='" + id + '\'' +
                ", location=" + location +
                ", memo='" + memo + '\'' +
                '}';
    }


    public Boolean getOnOff() {
        return onOff;
    }

    public String getTitle() {
        return title;
    }

    public String getImgPath() {
        return imgPath;
    }

    public Boolean getAlwaysOn() {
        return alwaysOn;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public String getId() {
        return id;
    }

    public MyLocation getLocation() {
        return location;
    }

    public String getMemo() {
        return memo;
    }


    public void setOnOff(Boolean onOff) {
        this.onOff = onOff;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setAlwaysOn(Boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(MyLocation location) {
        this.location = location;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isActive() {
        Date today = new Date();
        Boolean result = onOff && (alwaysOn || (isBeforeDate(dateFrom, today) && isBeforeDate(today, dateTo)));
        Log.v("Services", toString() + " isActive=" + result);
        return result;
    }

    private boolean isBeforeDate(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR) ||
                (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                        c1.get(Calendar.DAY_OF_YEAR) <=
                                c2.get(Calendar.DAY_OF_YEAR));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{onOff.toString(), title, imgPath, alwaysOn.toString(), dateToString(dateFrom), dateToString(dateTo),
                id, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(location.getRadius()), memo});
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {

        @Override
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    private String dateToString(Date d) {
        final SimpleDateFormat sdf = Constants.dateFormat;
        String dateString = sdf.format(d);
        return dateString;
    }

    private Date stringToDate(String s) {
        final SimpleDateFormat sdf = Constants.dateFormat;
        Date date = null;
        try {
            date = sdf.parse(s);
        } catch (ParseException e) {
            assertTrue(0 == 1);
        }
        return date;
    }
}
