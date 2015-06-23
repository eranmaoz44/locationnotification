// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package ssdl.technion.ac.il.locationnotification;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ssdl.technion.ac.il.locationnotification.utilities.Reminder;

public class ShareListAdapter extends BaseAdapter {
    private class MyTag {
        ImageView imageView;
        TextView textView;
    }


    Dialog container;
    private Context context;
    private List ids;
    private List images;
    private List names;
    JSONObject reminder;

    public ShareListAdapter(Context context1, JSONArray jsonarray, Reminder reminder1, Dialog dialog) {
        context = context1;
        container = dialog;
        int i;
        try {
            reminder = reminder1.toJson();
            reminder.put("sender name", ParseUser.getCurrentUser().get("name"));
            reminder.put("sender FbId", ParseUser.getCurrentUser().get("FacebookId"));
            Log.d("MyApp", reminder.toString());
        } catch (JSONException e) {
            Toast.makeText(context, "Could not parse reminder. please report this problem to developers!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        } catch (NullPointerException e) {
            throw new FacebookException();
        }
        names = new LinkedList();
        ids = new LinkedList();
        images = new LinkedList();
        i = 0;
        while (i < jsonarray.length()) {
            try {
                names.add(((JSONObject) jsonarray.get(i)).get("name").toString());
                ids.add(((JSONObject) jsonarray.get(i)).get("id").toString());
                (new AsyncTask<Integer, Void, Bitmap>() {
                    int i;

                    protected Bitmap doInBackground(Integer ainteger[]) {
                        i = ainteger[0];
                        try {
                            URL url = new URL("https://graph.facebook.com/" + ids.get(i) + "/picture?type=small");
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.connect();
                            return BitmapFactory.decodeStream(con.getInputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    protected void onPostExecute(Bitmap bitmap) {
                        images.add(i, bitmap);
                        notifyDataSetInvalidated();
                    }

                }).execute(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public int getCount() {
        return names.size();
    }

    public Object getItem(int i) {
        return names.get(i);
    }

    public long getItemId(int i) {
        return 0;
    }

    public View getView(final int position, View view, ViewGroup viewgroup) {
        MyTag tag;
        if (view == null) {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.search_item, null);
            view.findViewById(R.id.tv_si_dist_lbl).setVisibility(View.GONE);
            view.findViewById(R.id.tv_si_dist).setVisibility(View.GONE);
             tag  = new MyTag();
            tag.imageView = (ImageView) view.findViewById(R.id.iv_si_image);
            tag.textView = (TextView) view.findViewById(R.id.tv_si_title);
            view.setTag(tag);
            tag.textView.setTextColor(0xff000000);
        } else {
            tag = (MyTag) view.getTag();
        }
        tag.textView.setText((CharSequence) names.get(position));
        if (images.size() > position && images.get(position) != null) {
            tag.imageView.setImageBitmap((Bitmap) images.get(position));
        }
        view.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View view1) {
                ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
                query.whereEqualTo("FacebookId", ids.get(position));
                ParsePush parsepush = new ParsePush();
                parsepush.setData(reminder);
                parsepush.setQuery(query);
                parsepush.sendInBackground();
                Toast.makeText(context, "Notification sent!", Toast.LENGTH_SHORT).show();
                container.dismiss();
            }
        });
        return view;
    }


}
