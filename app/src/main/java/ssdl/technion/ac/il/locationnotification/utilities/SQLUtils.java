package ssdl.technion.ac.il.locationnotification.utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.location.places.Place;

import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ssdl.technion.ac.il.locationnotification.Constants.Constants;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SQLUtils extends SQLiteOpenHelper {

	private static final String DB_NAME = "data.db";
	private static final int DB_VERSION = 1;
	private static final String TABLE = "reminders_table";
	
	public SQLUtils(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("DB", "onCreate");
		
		String sql = "create table " + TABLE + "(onOff INTEGER, title VARCHAR( 64 ), imgPath VARCHAR( 128 ), alwaysOn INTEGER, dateFrom VARCHAR( 64 ), dateTo VARCHAR( 64 ), id VARCHAR ( 64 ), latitude DOUBLE, longitude DOUBLE, radius INTEGER, memo VARCHAR( 128 ), senderId VARCHAR( 128 ));";
		
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

//    //TODO: handle errors somehow
//    public static byte[] serialize(final Object obj) {
//        final ByteArrayOutputStream b = new ByteArrayOutputStream();
//        ObjectOutputStream o=null;
//        try {
//            o = new ObjectOutputStream(b);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assertTrue(0==1);
//        }
//        try {
//            o.writeObject(obj);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assertTrue(0==1);
//        }
//        return b.toByteArray();
//    }
//
//    public static Object deserialize(final byte[] bytes) {
//        final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
//        ObjectInputStream o=null;
//        try {
//            o = new ObjectInputStream(b);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assertTrue(0==1);
//        }
//        Object object=null;
//        try {
//            object=o.readObject();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            assertTrue(0==1);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assertTrue(0==1);
//        }
//        return object;
//    }

	public void insertData(Reminder r) {
		Log.d("DB", "onInsert");
		
		SQLiteDatabase db = this.getWritableDatabase();

        Double latitude=r.getLocation().getLatitude();
        Double longitude=r.getLocation().getLongitude();
        int radius=r.getLocation().getRadius();



		String sql = "INSERT into " + TABLE +
			" (onOff, title, imgPath, alwaysOn, dateFrom, dateTo, id, latitude, longitude, radius, memo, senderId) VALUES ('" + boolToInt(r.getOnOff()) + "','" +escapeString(r.getTitle()) +"','"+r.getImgPath()+"','"+boolToInt(r.getAlwaysOn())+"','"+dateToString(r.getDateFrom())+"','"+dateToString(r.getDateTo())+"','"+r.getId()+"','"+latitude+"','"+longitude+"','"+radius+"','"+escapeString(r.getMemo())+"','"+r.getSenderId()+ "')";

		db.execSQL(sql);
		
		db.close();
	}

    private String dateToString(Date d){
        final SimpleDateFormat sdf = Constants.dateFormat;
        String dateString = sdf.format(d);
        return dateString;
    }

    private Date stringToDate(String s){
        final SimpleDateFormat sdf = Constants.dateFormat;
        Date date = null;
        try {
            date = sdf.parse(s);
        } catch (ParseException e) {
            assertTrue(0==1);
        }
        return date;
    }

    private String escapeString(String s){
        return s.replaceAll("'","''");
    }

    private int boolToInt(Boolean b){
        return (b ? 1: 0);
    }

    private Boolean intToBoolean(int n){
        return n>0;
    }

	public List<Reminder> getReminderList() {
		List<Reminder> reminderList = new ArrayList<Reminder>();
		
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("select * from " + TABLE, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
            Boolean onOff=intToBoolean(cursor.getInt(0));
            Boolean alwaysOn=intToBoolean(cursor.getInt(3));
            Date dateFrom=stringToDate(cursor.getString(4));
            Date dateTo=stringToDate(cursor.getString(5));
            MyLocation location= new MyLocation(cursor.getDouble(7),cursor.getDouble(8),cursor.getInt(9));
            reminderList.add(new Reminder(onOff,
                    cursor.getString(1),cursor.getString(2),alwaysOn,dateFrom,dateTo,cursor.getString(6),location,cursor.getString(10),cursor.getString(11)));
		     
		    cursor.moveToNext();
		}
		
		cursor.close();
		db.close();	
		
		return reminderList;
	}
    public List<Reminder> getRemindersByIds(List<String> ids){
        List<Reminder> remindersFiltered=new ArrayList<>();
        for(Reminder r : getReminderList()){
            if(ids.contains(r.getId())){
                remindersFiltered.add(r);
            }
        }
        return remindersFiltered;
    }

    public void deleteData(String id){
        Log.d("DB", "onDelete");

        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "DELETE FROM " + TABLE +
                " WHERE id="+id;

        db.execSQL(sql);

        db.close();

    }

    public void updateData(Reminder r) {
        Log.d("DB", "onInsert");

        SQLiteDatabase db = this.getWritableDatabase();

        Double latitude=r.getLocation().getLatitude();
        Double longitude=r.getLocation().getLongitude();
        int radius=r.getLocation().getRadius();



        String sql = "UPDATE " + TABLE  +" SET "+
                "onOff='"+boolToInt(r.getOnOff())+"', title='"+escapeString(r.getTitle())+"', imgPath='"+r.getImgPath()+
        "', alwaysOn='"+boolToInt(r.getAlwaysOn())+"', dateFrom='"+dateToString(r.getDateFrom())+"', dateTo='"+dateToString(r.getDateTo())+
        "', latitude='"+latitude+"', longitude='"+longitude+"', radius='"+radius+"', memo='"+escapeString(r.getMemo())+"', senderId='"+r.getSenderId()+"' WHERE id='"+r.getId()+"'";

        db.execSQL(sql);

        db.close();
    }

}
