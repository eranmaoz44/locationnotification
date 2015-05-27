//package ssdl.technion.ac.il.locationnotification.activities;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.support.v7.app.ActionBarActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//import ssdl.technion.ac.il.locationnotification.Constants.Constants;
//import ssdl.technion.ac.il.locationnotification.R;
//import ssdl.technion.ac.il.locationnotification.services.GeofencingService;
//import ssdl.technion.ac.il.locationnotification.services.MyApplication;
//import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
//
//public class ViewRemindersActivity extends ActionBarActivity {
//
//    List<Reminder> reminders;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_view_reminders);
//
//
//        reminders=((MyApplication)getApplication()).getSQL().getReminderList();
//        for(Reminder r : reminders){
//            Log.v("SQL", r.toString());
//        }
//
//        final ListView remindersListView=(ListView)findViewById(R.id.lv_reminders);
//        remindersListView.setAdapter(new ReminderListAdapter(this,0,reminders));
//
//        remindersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                final String reminderId=reminders.get(position).getId();
//                final int reminderPos=position;
//                AlertDialog.Builder builder = new AlertDialog.Builder(ViewRemindersActivity.this);
//                builder.setTitle("Choose what to do with reminder");
//                builder.setItems(new CharSequence[] {"Delete"},
//                        new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                switch (which) {
//                                    case 0:
//                                        ((MyApplication)getApplication()).getSQL().deleteData(reminderId);
//                                        reminders.remove(position);
//                                        remindersListView.invalidateViews();
//                                        Intent intent = new Intent(getApplicationContext(), GeofencingService.class);
//                                        startService(intent);
//                                        break;
//                                    default:
//                                        break;
//                                }
//                            }
//                        });
//
//                builder.show();
//                return true;
//            }
//        });
//
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_view_reminders, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//    private class ReminderListAdapter extends ArrayAdapter<Reminder> {
//
//        private class ViewHolder {
//            private TextView itemView;
//        }
//
//        public ReminderListAdapter(Context context, int textViewResourceId, List<Reminder> items) {
//            super(context, textViewResourceId, items);
//        }
//
//        public View getView(int position, View convertView, ViewGroup parent) {
//            convertView = LayoutInflater.from(this.getContext())
//                    .inflate(R.layout.reminder_row_layout, parent, false);
//            TextView tvOnOff= (TextView)convertView. findViewById(R.id.tv_onOff);
//            TextView tvName= (TextView) convertView.findViewById(R.id.tv_name);
//            ImageView ivInitiator = (ImageView)convertView.findViewById(R.id.iv_initiator);
//            TextView tvDate= (TextView)convertView.findViewById(R.id.tv_date);
//            Reminder reminder=reminders.get(position);
//            tvOnOff.setText(reminder.getOnOff()? "On" : "Off");
//            tvName.setText(reminder.getTitle());
//            File image = new File(reminder.getImgPath());
//
//            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//
//            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
//
//            bitmap = Bitmap.createScaledBitmap(bitmap,256,256,true);
//
//            ivInitiator.setImageBitmap(bitmap);
//
//            Date dateFrom=reminder.getDateFrom();
//            Date dateTo=reminder.getDateTo();
//            final SimpleDateFormat sdf = Constants.dateFormat;
//            String dateFromString = sdf.format(dateFrom);
//            String dateToString = sdf.format(dateTo);
//            tvDate.setText(dateFromString+"-"+dateToString);
//            return convertView;
//        }
//    }
//}
