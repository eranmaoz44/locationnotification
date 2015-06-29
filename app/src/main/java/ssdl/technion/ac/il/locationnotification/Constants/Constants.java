package ssdl.technion.ac.il.locationnotification.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Eran on 5/23/2015.
 */
public class Constants {
    static public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    static public final String REMINDER_TAG = "locationnotification.reminder";
    static public final int RADIUS=128;
    static public final float MIN_DISTANCE_BETWEEN_UPDATES=64;
    static public final float ACCEPTABLE_ACCURAY=1024;
//    static public final float STILL_DISTANCE=32;
    static public final int NOTIFIABLE_STILL_COUNT=5;
    static public final String NEW_REMINDER_ID="-1";
    static public final String ID_KEY="id_key";
    static public final String SENDER_NAME_STRING = "sender name";
    public static final  String LOCATION_TAG="ssdl.technion.ac.il.locationnotification.location_tag";
    public static final String REMINDER_DELETED_TAG ="ssdl.technion.ac.il.locationnotification.reminder_deleted_tag" ;
    public static final String REMINDER_ADDED_TAG ="ssdl.technion.ac.il.locationnotification.reminder_added_tag" ;
    public static final String STARTED_FROM_MAIN_ACTIVITY = "ssdl.technion.ac.il.locationnotification.started_from_main_activity";
    public static final String FACEBOOK_ID ="FacebookId" ;
    public static final int FASTEST_INTERVAL=60*1000;
    public static final int DEFAULT_INTERVAL=60*1000;
    public static final float WAITING_PER_DISTANCE =25 ;
    public static final long MINIMUM_NOTIFICATION_INTERVAL = 10*60*1000;
    public static final String REMINDER_SAVED_TAG = "ssdl.technion.ac.il.locationnotification.reminder_saved_tag";
    public static final String BACK_PRESSED_TAG ="ssdl.technion.ac.il.locationnotification.back_pressed_tag" ;
}
