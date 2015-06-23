// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package ssdl.technion.ac.il.locationnotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;

public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    Reminder r;

    protected void onPushReceive(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);
        try {
            final JSONObject j = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            r = new Reminder(j);
            (new SQLUtils(context.getApplicationContext())).insertData(r);
            Intent notificationIntent = new Intent(context, UserDetailsActivity.class);
            notificationIntent.putExtra(Constants.REMINDER_TAG, r);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent notificationPendingIntent =
                    stackBuilder.getPendingIntent(Integer.valueOf(r.getId()), PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(Color.RED)
                    .setContentTitle(j.get(Constants.SENDER_NAME_STRING) + " wants to remind you!")
                    .setContentText("Click to view!")
                    .setContentIntent(notificationPendingIntent)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).setAutoCancel(true);
            final NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final String jSender = j.getString("sender FbId");
            (new AsyncTask<NotificationCompat.Builder,Void,Void>(){
                NotificationCompat.Builder b;
                @Override
                protected Void doInBackground(NotificationCompat.Builder... params) {
                    b = params[0];
                    try {
                        URL url = new URL("https://graph.facebook.com/" + jSender + "/picture?type=small");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.connect();
                        b.setLargeIcon(BitmapFactory.decodeStream(con.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mNotificationManager.notify(Integer.valueOf(r.getId()), b.build());
                }
            }).execute(builder);
        } catch (JSONException|ParseException e) {
            e.printStackTrace();
        }
    }
}
