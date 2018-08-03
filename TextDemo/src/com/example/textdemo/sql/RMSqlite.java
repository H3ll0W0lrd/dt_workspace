package com.example.textdemo.sql;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class RMSqlite {
	private static RMSqlite instance;
	private SQLiteDatabase database;

	public static RMSqlite getInstance() {
		if (instance == null) {
			instance = new RMSqlite();
		}
		return instance;
	}

	public RMSqlite() {
		database = getWritableDatabase();
	}

	// 创建sd卡缓存数据库
	private SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase database = null;
		String path = Environment.getExternalStorageDirectory()
				+ File.separator+"poi.sqlite";
		File file = new File(path);
		Log.i("rtmap", file.exists()+"   "+path);
		database = SQLiteDatabase.openOrCreateDatabase(path, null);

		if (database != null) {
			if (file.exists()) {
				int version = database.getVersion();
				if (version != 0) {
					database.setVersion(0);
				}
			} else {
				database.setVersion(0);
			}
			createDatabase(database);
		}
		return database;
	}

	// 创建数据库表
	private void createDatabase(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE IF NOT EXISTS mapAngleInfo(_id INTEGER PRIMARY KEY autoincrement,buildId,mapAngle,timestamp)");
		database.execSQL("CREATE TABLE IF NOT EXISTS phone(_id INTEGER PRIMARY KEY autoincrement,lbsid,delaylocate_time,isbadlog_return,isphone_whitelist,expiration_time)");
	}


	public void getPoi() {
		Cursor c = database.query("poi", null, null,
				null, null, null, null);
		while (c.moveToNext()) {
			Log.i("rtmap", "名称："+c.getString(c.getColumnIndex("name")));
		}
	}
}