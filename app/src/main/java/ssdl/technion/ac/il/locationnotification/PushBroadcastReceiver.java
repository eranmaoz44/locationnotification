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
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;

public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    Reminder r;

    protected void onPushReceive(final Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);
        try {
            final JSONObject j = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            r = new Reminder(j);
            final String jSender = j.getString("sender FbId");
            (new AsyncTask<Void, Void, Void>() {
                NotificationCompat.Builder b;
                final NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setSmallIcon(R.mipmap.ic_launcher)
                                .setColor(Color.RED)
                                .setContentTitle(j.get(Constants.SENDER_NAME_STRING) + " wants to remind you!")
                                .setContentText("Click to view!")
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).setAutoCancel(true);
                        b = builder;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        URL url = new URL("https://graph.facebook.com/" + jSender + "/picture?type=small");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.connect();
                        b.setLargeIcon(BitmapFactory.decodeStream(con.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (j.has("file lnk") && j.has("file extention")) {
                        try {
                            String args[] = {j.getString("file lnk"), j.getString("file extention")};
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            String[] fileSplit = args[0].split("/");
                            String fileName = fileSplit[fileSplit.length - 1];
                            File photo = new File(path, fileName + "." + args[1]);
                            int i = 0;
                            while (photo.exists())
                                photo = new File(path, fileName + (i++) + "." + args[1]);
                            try {
                                path.mkdirs();
                                photo.createNewFile();
                                URL url = new URL(args[0]);
                                InputStream is = url.openStream();
                                OutputStream os = new FileOutputStream(photo);
                                byte[] b = new byte[2048];
                                int length;
                                while ((length = is.read(b)) != -1) {
                                    os.write(b, 0, length);
                                }
                                is.close();
                                os.close();
                                r.setImgPath(photo.getAbsolutePath());
                            } catch (java.io.IOException e) {
                                Log.e("PictureDemo", "Exception in photoCallback", e);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent notificationIntent = new Intent(context, UserDetailsActivity.class);
                    notificationIntent.putExtra(Constants.REMINDER_TAG, r);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(notificationIntent);
                    PendingIntent notificationPendingIntent =
                            stackBuilder.getPendingIntent(Integer.valueOf(r.getId()), PendingIntent.FLAG_UPDATE_CURRENT);

                    (new SQLUtils(context.getApplicationContext())).insertData(r);
                    b.setContentIntent(notificationPendingIntent);
                    mNotificationManager.notify(Integer.valueOf(r.getId()), b.build());
                }
            }).execute();
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }
}
