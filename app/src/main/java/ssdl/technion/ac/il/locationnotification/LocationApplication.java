// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package ssdl.technion.ac.il.locationnotification;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

public class LocationApplication extends Application
{
    public void onCreate()
    {
        super.onCreate();
        Parse.initialize(this, "b87hU60VwwtYST0AzHDsUA0dH19PSbPkAokz8BSC", "uy1JUTnG6EMnAiXbwfJoQZ5uqmhovsVwE6PWJGNd");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(getApplicationContext(), 100);
        ParsePush.subscribeInBackground("");
    }
}
