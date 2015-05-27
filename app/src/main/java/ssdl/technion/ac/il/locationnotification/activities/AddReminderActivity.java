//package ssdl.technion.ac.il.locationnotification.activities;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.support.v7.app.ActionBarActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.DatePicker;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Switch;
//import android.widget.Toast;
//
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
//import com.google.android.gms.common.GooglePlayServicesRepairableException;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlacePicker;
//
//import java.io.DataInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.channels.FileChannel;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//import ssdl.technion.ac.il.locationnotification.Constants.Constants;
//import ssdl.technion.ac.il.locationnotification.R;
//import ssdl.technion.ac.il.locationnotification.services.GeofencingService;
//import ssdl.technion.ac.il.locationnotification.services.MyApplication;
//import ssdl.technion.ac.il.locationnotification.utilities.GeofencingUtils;
//import ssdl.technion.ac.il.locationnotification.utilities.Location;
//import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
//
//import static junit.framework.Assert.*;
////TODO: handle empty fields
////TODO: handle errors
//public class AddReminderActivity extends ActionBarActivity {
//
//    final int ACTION_REQUEST_GALLERY=21235;
//    final int PLACE_PICKER_REQUEST=21236;
//    Switch shOnOff;
//    EditText etName;
//    ImageView ivInitiator;
//    String iconPath;
//    DatePicker dpDate;
//    Place place;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_reminder);
//        shOnOff= (Switch) findViewById(R.id.sh_onOff);
//        etName= (EditText) findViewById(R.id.et_name);
//        ivInitiator= (ImageView) findViewById(R.id.iv_initiator);
//        iconPath="";
//        dpDate= (DatePicker) findViewById(R.id.dp_date);
//
//        Reminder reminder=getIntent().getParcelableExtra(Constants.REMINDER_TAG);
//        if(null != reminder){
//            etName.setText(reminder.getTitle());
//        }
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
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
//    public void onClickChooseInitiator(View v){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Choose Image Source");
//        builder.setItems(new CharSequence[] {"Gallery"},
//                new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
//                            case 0:
//
//                                // GET IMAGE FROM THE GALLERY
//                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                                intent.setType("image/*");
//
//                                Intent chooser = Intent.createChooser(intent, "Choose a Picture");
//                                startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
//
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                });
//
//        builder.show();
//    }
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//
//            switch (requestCode) {
//                case ACTION_REQUEST_GALLERY:
//                    (new SaveImageTask()).execute(data);
//                    break;
//                case PLACE_PICKER_REQUEST:
//                    place = PlacePicker.getPlace(data, this);
//                    String toastMsg = String.format("Place: %s", place.getName());
//                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
//                    break;
//            }
//
//        }
//    }
//
//
//    private class SaveImageTask extends AsyncTask<Intent,Void,Bitmap> {
//        @Override
//        protected Bitmap doInBackground(Intent... params) {
//            Log.v("Images", "Starting saving");
//            String root = getFilesDir() + File.separator;
//            String dir = root + "initiatorImages" + File.separator;
//            File createDir = new File(dir);
//            if (!createDir.exists()) {
//                createDir.mkdir();
//            }
//            assertTrue(createDir.exists());
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
//            String timeStamp = dateFormat.format(new Date());
//            iconPath=dir + "picture_" + timeStamp + ".jpg";
//            File f = new File(dir + "picture_" + timeStamp + ".jpg");
//            try {
//                f.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            assertTrue(f.exists());
//            InputStream stream = null;
//            try {
//                stream = getContentResolver().openInputStream(
//                        params[0].getData());
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            Bitmap bitmap = BitmapFactory.decodeStream(stream);
//            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap, 256, 256, false);
//            try {
//                stream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            FileOutputStream fOut = null;
//            try {
//                fOut = new FileOutputStream(f);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG,22, fOut);
//            try {
//                fOut.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                fOut.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Log.v("Images", "Finished saving");
//            return scaledBitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            ivInitiator.setImageBitmap(bitmap);
//            Log.v("Images","Finished displaying");
//        }
//    }
//    public void onClickSubmit(View v){
//        Boolean onOff=shOnOff.isChecked();
//        String name=etName.getText().toString();
//        Date date=getDateFromDatePicker(dpDate);
//        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
//        //TODO: make string for the id key
//        int id = sharedPref.getInt("id_key", 0);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putInt("id_key", id + 1);
//        editor.commit();
//        //TODO: put in constants somewhere or add user input
//        Location location=new Location(place.getLatLng().latitude,place.getLatLng().longitude,Constants.RADIUS);
//                    ((MyApplication) getApplication()).getSQL().insertData(onOff, name, iconPath,false,date,date,Integer.toString(id),location,"coolstuff");
//        Intent intent = new Intent(getApplicationContext(), GeofencingService.class);
//        startService(intent);
//        finish();
//    }
//    private Date getDateFromDatePicker(DatePicker datePicker){
//        int day = datePicker.getDayOfMonth();
//        int month = datePicker.getMonth();
//        int year =  datePicker.getYear();
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month, day);
//
//        Date date=calendar.getTime();
//        return date;
//    }
//
//    public void onClickPickPlace(View v){
//        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//        Context context = getApplicationContext();
//        try {
//            startActivityForResult(builder.build(context), PLACE_PICKER_REQUEST);
//        } catch (GooglePlayServicesRepairableException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        }
//    }
//}
