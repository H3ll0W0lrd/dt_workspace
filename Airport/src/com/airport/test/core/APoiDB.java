package com.airport.test.core;

import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTFileUtil;
import com.rtm.common.model.POI;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class APoiDB extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "poi";
	private static final int DATABASE_VERSION = 1;
	
	private static APoiDB instance;

	public APoiDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static APoiDB getInstance() {
		if(instance==null){
			instance =new APoiDB(DTApplication.getInstance());
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		SQLiteDatabase.openOrCreateDatabase(DTFileUtil.getDataDir() + "poi.sqlite", null);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 1) {
			db.needUpgrade(1);
		}
	}

	public void selectPoi(POI mPoi) {
		SQLiteDatabase data = getWritableDatabase();
		Cursor c = data.rawQuery("select * from poi where floor=F2", null);
		mPoi.setAddress(c.getString(c.getColumnIndex("address")));
		mPoi.setHours(c.getString(c.getColumnIndex("time")));
		mPoi.setLogoImage(c.getString(c.getColumnIndex("logo")));
		mPoi.setPoiImage(c.getString(c.getColumnIndex("image")));
		mPoi.setDesc(c.getString(c.getColumnIndex("descipt")));
		mPoi.setClassname(c.getString(c.getColumnIndex("type")));
		mPoi.setCurrecy(c.getString(c.getColumnIndex("currecy")));
		c.close();
	}
}