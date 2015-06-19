package ssdl.technion.ac.il.locationnotification.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Eran on 5/23/2015.
 */
public class Constants {
    static public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    static public final String REMINDER_TAG = "locationnotification.reminder";
    static public final int RADIUS=28;
    static public final float MIN_DISTANCE_BETWEEN_UPDATES=20;
    static public final float ACCEPTABLE_ACCURAY=30;
    static public final String NEW_REMINDER_ID="-1";
    static public final String ID_KEY="id_key";
    static public final String SENDER_NAME_STRING = "sender name";
    public static final  String LOCATION_TAG="ssdl.technion.ac.il.locationnotification.location_tag";
    public static final String REMINDER_DELETED_TAG ="ssdl.technion.ac.il.locationnotification.reminder_deleted_tag" ;
}
