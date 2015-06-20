package ssdl.technion.ac.il.locationnotification;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.MyLocation;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;
import ssdl.technion.ac.il.locationnotification.utils_ui.ScrollViewHelper;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.inthecheesefactory.thecheeselibrary.fragment.StatedFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertTrue;


public class UserDetailsFragment extends StatedFragment implements CompoundButton.OnCheckedChangeListener {

    ImageView imageOfReminder;
    EditText editTextTitle;
    EditText editDescription;
    DateDialogHelper dateDialogHelper1;
    DateDialogHelper dateDialogHelper2;
    RadioGroup radioGroupRepeate;
    TextView textViewDate1;
    TextView textViewDate2;
    ImageButton buttonDate1;
    ImageButton buttonDate2;
    TextView tvDash;
    TextView tvDateStart;
    TextView tvDateEnd;
    TextView textViewPlacePicker;
    Switch onOffSwitch;
    final int ACTION_REQUEST_GALLERY = 21235;    Reminder reminder;
    String iconPath;

    private final int PLACE_PICKER_REQUEST=20000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);
        setupUI(view);

       // setupFadingToolbar(view);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reminder=((UserDetailsActivity)getActivity()).getReminder();
        assertTrue(null != reminder);

        editTextTitle.setText(reminder.getTitle());
        putCursorOnEnd(editTextTitle);
        editDescription.setText(reminder.getMemo());
        putCursorOnEnd(editDescription);
        editTextTitle.addTextChangedListener(new UpdateListener(R.id.et_edit_title));
        editDescription.addTextChangedListener(new UpdateListener(R.id.et_description));

        File image = new File(reminder.getImgPath());

        //TODO: add support to default picture, when creating new reminder.
        if(image.exists()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();

            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

            bitmap = Bitmap.createScaledBitmap(bitmap,256,256,true);

            imageOfReminder.setImageBitmap(bitmap);
        } else {
            imageOfReminder.setImageResource(R.drawable.image_3);
        }

        setTextViewDates();

        MyLocation loc=reminder.getLocation();
        if(-1!=loc.getRadius()){
            (new NameFetcher()).execute(loc);
        } else {
             textViewPlacePicker.setText(getString(R.string.edit_user_pick_location));
        }
        if(!reminder.getAlwaysOn()) {
            ((RadioButton)radioGroupRepeate.findViewById(R.id.radio_dates)).setChecked(true);
            radioCheckChanged(R.id.radio_dates);

        }else{
            ((RadioButton)radioGroupRepeate.findViewById(R.id.radio_always)).setChecked(true);
            radioCheckChanged(R.id.radio_always);
        }

        onOffSwitch.setChecked(reminder.getOnOff());
    }

    private void putCursorOnEnd(EditText et) {
        et.setSelection(et.getText().length());

    }

    private void setupUI(View view) {
        editTextTitle = (EditText) view.findViewById(R.id.et_edit_title);
        editDescription = (EditText) view.findViewById(R.id.et_description);
        imageOfReminder = (ImageView) view.findViewById(R.id.img_edit_image);
        setImageUpload();
        textViewDate1 = (TextView) view.findViewById(R.id.date1);
        //textViewDate1.setOnClickListener(dateDialogHelper1);
        textViewDate2 = (TextView) view.findViewById(R.id.date2);
        //textViewDate2.setOnClickListener(dateDialogHelper2);
        textViewPlacePicker = (TextView) view.findViewById(R.id.tv_location);
        textViewPlacePicker.setOnClickListener(new PlacePickerListener());

        setDateButtons(view);

        setupRadioGroup(view);


        onOffSwitch = (Switch)view.findViewById(R.id.s_on_off);
        onOffSwitch.setOnCheckedChangeListener(this);
    }

    private void setImageUpload() {
        imageOfReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose Image Source");
                builder.setItems(new CharSequence[]{"Gallery", "Remove image"},
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:

                                        // GET IMAGE FROM THE GALLERY
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setType("image/*");

                                        Intent chooser = Intent.createChooser(intent, "Choose a Picture");
                                        getActivity().startActivityForResult(chooser, ACTION_REQUEST_GALLERY);

                                        break;
                                    case 1:
                                        reminder.setImgPath("Drawable/");
                                        imageOfReminder.setImageResource(R.drawable.image_3);
                                    default:
                                        break;
                                }
                            }
                        });
                builder.show();
            }
        });

    }

    private void setDateButtons(View view) {
        buttonDate1 = (ImageButton) view.findViewById(R.id.btn_date1);
        buttonDate1.setEnabled(false);
        dateDialogHelper1 = new DateDialogHelper(textViewDate1);
        buttonDate1.setOnClickListener(dateDialogHelper1);
        buttonDate2 = (ImageButton) view.findViewById(R.id.btn_date2);
        dateDialogHelper2 = new DateDialogHelper(textViewDate2);
        buttonDate2.setEnabled(false);
        buttonDate2.setOnClickListener(dateDialogHelper2);
        tvDateStart=(TextView)view.findViewById(R.id.start);
        tvDateEnd=(TextView)view.findViewById(R.id.end);
        tvDash=(TextView)view.findViewById(R.id.dash);
    }

    private void setTextViewDates() {
        SimpleDateFormat sdf=Constants.dateFormat;
        String date1=sdf.format(reminder.getDateFrom());
        String date2=sdf.format(reminder.getDateTo());
        textViewDate1.setText(date1);
        dateDialogHelper1.setDate(reminder.getDateFrom());
        textViewDate2.setText(date2);
        dateDialogHelper2.setDate(reminder.getDateTo());
    }

    private String dateFormat(int day, int month, int year) {
        String date = day + "/" + ((month + 1) % 12) + "/" + year;
        return date;
    }

    private void radioCheckChanged(int checkedId){
        if (checkedId == R.id.radio_dates) {
            reminder.setAlwaysOn(false);
            buttonDate1.setEnabled(true);
            buttonDate2.setEnabled(true);
            setDatesVisibility(View.VISIBLE);
        } else {
            reminder.setAlwaysOn(true);
            buttonDate1.setEnabled(false);
            buttonDate2.setEnabled(false);
            setDatesVisibility(View.GONE);
        }
    }

    private void setDatesVisibility(int visibility){
        ViewGroup v=(ViewGroup)getView();
        buttonDate1.setVisibility(visibility);
        buttonDate2.setVisibility(visibility);
        textViewDate1.setVisibility(visibility);
        textViewDate2.setVisibility(visibility);
        tvDateStart.setVisibility(visibility);
        tvDateEnd.setVisibility(visibility);
        tvDash.setVisibility(visibility);
    }

    private void setupRadioGroup(View view) {
        radioGroupRepeate = (RadioGroup) view.findViewById(R.id.radio_repeat);
        radioGroupRepeate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioCheckChanged(checkedId);
            }
        });
    }

    private void setupFadingToolbar(View view) {
        ScrollViewHelper scrollViewHelper = (ScrollViewHelper) view.findViewById(R.id.scrollViewHelper);
        scrollViewHelper.setOnScrollViewListener(new ScrollViewHelper.OnScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewHelper v, int l, int t, int oldl, int oldt) {
                ((UserDetailsActivity) getActivity()).setToolBarAlpha(getAlphaforActionBar(v.getScrollY()));
            }

            private int getAlphaforActionBar(int scrollY) {
                int minDist = 0, maxDist = 550;
                if (scrollY > maxDist) {
                    return 255;
                } else {
                    if (scrollY < minDist) {
                        return 0;
                    } else {
                        return (int) ((255.0 / maxDist) * scrollY);
                    }
                }
            }


        });

//        mSpannableString = new SpannableString(title);
//       mAlphaForegroundColorSpan = new AlphaForeGroundColorSpan(0xFFFFFF);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        reminder.setOnOff(isChecked);
    }

    private class DateDialogHelper implements DatePickerDialog.OnDateSetListener, View.OnClickListener {
        TextView textView;
        DatePickerDialog dpd;
        Calendar calendar;

        public DateDialogHelper(TextView textView) {
            this.textView = textView;

            calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            dpd = DatePickerDialog.newInstance(this, year, month, day);

        }

        @Override
        public void onClick(View view) {

            dpd.show(getFragmentManager(), "Datepickerdialog");
        }

        public Calendar getDate(){
            return calendar;
        }

        public void setDate(Date date){
            calendar.setTime(date);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            dpd = DatePickerDialog.newInstance(this, year, month, day);
        }

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c=Calendar.getInstance();
            c.set(year,monthOfYear,dayOfMonth);
            if(!isLegalDate(textView,c)){
                Toast.makeText(getActivity(), getActivity().getString(R.string.illegal_date), Toast.LENGTH_SHORT).show();
                return;
            }

            calendar=c;
            SimpleDateFormat sdf = Constants.dateFormat;
            String dateString = sdf.format(c.getTime());

            textView.setText(dateString);



            dpd = DatePickerDialog.newInstance(this, year, monthOfYear, dayOfMonth);
            Date rDate = calendar.getTime();
            if (textView.getId() == R.id.date1) {
                reminder.setDateFrom(rDate);
            } else {
                reminder.setDateTo(rDate);
            }
        }
    }
    private boolean isLegalDate(TextView textView,Calendar calendar){
        int res;
        if(textView.getId()==R.id.date1){
           res=dateDialogHelper2.getDate().compareTo(calendar);
            return  dateDialogHelper2.getDate().after(calendar) ;
        }else{
            res=dateDialogHelper1.getDate().compareTo(calendar);
            return  dateDialogHelper1.getDate().before(calendar);
        }

    }

    class UpdateListener implements TextWatcher{

        private int id;
        public UpdateListener(int id){
            this.id=id;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String txt=s.toString();
            switch(id){
                case R.id.et_edit_title:
                    reminder.setTitle(txt);
                    break;
                case R.id.et_description:
                    reminder.setMemo(txt);
                    break;
                default:
                    assertTrue(0==1);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {
                case ACTION_REQUEST_GALLERY:

                    (new SaveImageTask()).execute(data);
                    break;

                case PLACE_PICKER_REQUEST:
                    Place place = PlacePicker.getPlace(data, getActivity());
                    MyLocation myLocation=new MyLocation(place.getLatLng().latitude,place.getLatLng().longitude,Constants.RADIUS);
                    reminder.setLocation(myLocation);
                    (new NameFetcher()).execute(myLocation);

//                    String toastMsg = String.format("Place: %s", place.getName());
//                    Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }

    private class NameFetcher extends AsyncTask<MyLocation,Void,String>{

        private MyLocation loc;
        @Override
        protected String doInBackground(MyLocation... params) {
            loc=params[0];
            final String latLngStr="("+loc.getLatitude()+", "+loc.getLongitude()+")";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(UserDetailsFragment.this.getActivity().getApplicationContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("NameFetcher","IOException");
                return latLngStr;
            }

            if(addresses.size()<1){
                Log.v("NameFetcher","addresses.size()<1)");
                return latLngStr;
            }
            Address addr=addresses.get(0);
            //TODO: get location name some how, like "Nola pub" instead of "Hankin 32" etc..
            String name=null;
            String address=addr.getAddressLine(0);
            if(null!=name){
                return name;
            } else if(null!=address){
                String city=addr.getAddressLine(1);
                return address+(null != city ? ", "+city : "");
            } else {
                return latLngStr;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v("NameFetcher","onPostExecutre "+s);
            super.onPostExecute(s);
            textViewPlacePicker.setText(s);
        }
    }



    private class PlacePickerListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            focusOnPrevLoc(builder);

            Context context =getActivity();
            try {
                startActivityForResult(builder.build(context), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }

        private void focusOnPrevLoc(PlacePicker.IntentBuilder builder) {
            if(-1!=reminder.getLocation().getRadius()){
                MyLocation loc=reminder.getLocation();
                LatLng latLng=new LatLng(loc.getLatitude(),loc.getLongitude());
                final double HEADING_NORTH_EAST = 45;
                final double HEADING_SOUTH_WEST = 215;
                LatLng northEast = SphericalUtil.computeOffset(latLng, 709, HEADING_NORTH_EAST);
                LatLng southWest = SphericalUtil.computeOffset(latLng, 709,HEADING_SOUTH_WEST );
                builder.setLatLngBounds(new LatLngBounds(southWest,northEast));
            }
        }


    }


    private class SaveImageTask extends AsyncTask<Intent, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Intent... params) {
            Log.v("Images", "Starting saving");
            String root = getActivity().getFilesDir() + File.separator;
            String dir = root + "initiatorImages" + File.separator;
            File createDir = new File(dir);
            if (!createDir.exists()) {
                createDir.mkdir();
            }
            assertTrue(createDir.exists());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            String timeStamp = dateFormat.format(new Date());
            iconPath = dir + "picture_" + timeStamp + ".jpg";
            File f = new File(dir + "picture_" + timeStamp + ".jpg");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(f.exists());
            InputStream stream = null;
            try {
                stream = getActivity().getContentResolver().openInputStream(
                        params[0].getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 22, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v("Images", "Finished saving");
            return scaledBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageOfReminder.setImageBitmap(bitmap);
            reminder.setImgPath(iconPath);
            Toast.makeText(getActivity(), getActivity().getString(R.string.image_uploaded_successfuly), Toast.LENGTH_SHORT).show();
            Log.v("Images", "Finished displaying");
        }
    }

    private void transformIntoViewMode(){
        imageOfReminder.setOnClickListener(null);
        editTextTitle.setEnabled(false);
        for (int i = 0; i < radioGroupRepeate.getChildCount(); i++) {
            radioGroupRepeate.getChildAt(i).setEnabled(false);
        }
        buttonDate1.setEnabled(false);
        buttonDate2.setEnabled(false);
        textViewDate1.setOnClickListener(null);
        textViewDate2.setOnClickListener(null);
        textViewPlacePicker.setOnClickListener(null);
        editDescription.setEnabled(false);
        ((UserDetailsActivity)getActivity()).setSaveButtonVisibility(false);
    }
}
