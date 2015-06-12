package ssdl.technion.ac.il.locationnotification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.fragment.bus.ActivityResultBus;
import com.inthecheesefactory.thecheeselibrary.fragment.bus.ActivityResultEvent;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;


public class UserDetailsActivity extends ActionBarActivity {
    private Toolbar toolBar;
    private ImageView iv;

    Reminder r;

    ColorDrawable cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        Reminder reminder = getIntent().getParcelableExtra(Constants.REMINDER_TAG);
        r = reminder;


        toolBar = (Toolbar) findViewById(R.id.details_toolbar);
        cd = new ColorDrawable(getResources().getColor(R.color.primary));
        // cd.setAlpha(0);
        toolBar.setBackground(cd);

        //toolBar.setBackgroundColor(getResources().getColor(R.color.background_floating_material_dark));
        setSupportActionBar(toolBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setStatusBarColor(getResources().getColor(R.color.background_floating_material_dark));
            postponeEnterTransition();
            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }

    }

    public Reminder getReminder() {
        return r;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_remove) {
            SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
            sqlUtils.deleteData(r.getId());
            finish();
            return true;
        } else if (id==R.id.action_save){
            saveReminder();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveReminder() {
        boolean validated = validateInput();
        if(!validated){
            return;
        }
        SQLUtils sqlUtils = new SQLUtils(getApplicationContext());
        if(0==r.getId().compareTo(Constants.NEW_REMINDER_ID)){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int rId = sharedPref.getInt(Constants.ID_KEY, 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(Constants.ID_KEY, rId + 1);
            editor.commit();
            r.setId(String.valueOf(rId));
            sqlUtils.insertData(r);
            Log.v("SQL", "insertData");
            Toast.makeText(this,getString(R.string.added_successfully),Toast.LENGTH_SHORT).show();
        } else {
            sqlUtils.updateData(r);
            Log.v("SQL", "updateData");
            Toast.makeText(this,getString(R.string.updated_successfully),Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private boolean validateInput() {
        boolean validated=true;
        EditText etTitle=(EditText)findViewById(R.id.et_edit_title);
        String title=etTitle.getText().toString();
        if(0==title.compareTo("")){
            validated=false;
            etTitle.setError(getString(R.string.title_error_message),getResources().getDrawable(R.drawable.ic_error_white_24dp));
        }
        TextView tvLocation=(TextView)findViewById(R.id.tv_location);
        String location=tvLocation.getText().toString();
        if(0==location.compareTo(getString(R.string.edit_user_pick_location))){
            if(validated)
                tvLocation.requestFocus();
            validated=false;
            tvLocation.setError(getString(R.string.location_error_message),getResources().getDrawable(R.drawable.ic_error_white_24dp));
        }
        if(!validated){
            Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
        }
        return validated;
    }


    public void setToolBarAlpha(int alpha) {
        cd.setAlpha(alpha);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBus.getInstance().postQueue(
                new ActivityResultEvent(requestCode, resultCode, data));
    }
}
