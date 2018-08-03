package com.rtmap.wifipicker.util;

import android.database.sqlite.SQLiteDatabase;
/**
 * 缓存数据库类
 * @author Xiao Yi
 *
 */
public class DatabaseHelper {
	//执行sql语句
	private void execSQL(SQLiteDatabase db, String sql){
		try {
			db.execSQL(sql);
		} catch(Exception e) {
			e.printStackTrace();
		};
	}
	
	//创建sd卡缓存数据库
	public SQLiteDatabase getWritableDatabase(){
		SQLiteDatabase database = null;
		if(FileUtil.checkDir(Constants.WIFI_PICKER_PATH)) {
			String path = String.format("%s%s", Constants.WIFI_PICKER_PATH, Constants.DATABASE_NAME);
			boolean isExist = FileUtil.checkFile(path);
			database = SQLiteDatabase.openOrCreateDatabase(path, null);
			
			if(database != null) {
				if(isExist) {
					int version = database.getVersion();
					if(version != Constants.DATABASE_VERSION){
						onUpdateDatabase(database, version, Constants.DATABASE_VERSION);
						database.setVersion(Constants.DATABASE_VERSION);
					}
				} else {
					createDatabase(database);
					database.setVersion(Constants.DATABASE_VERSION);
				}
			}
		}
		return database;
	}
	
	//创建数据库表
	private void createDatabase(SQLiteDatabase database){
		if(database != null) {
			execSQL(database, "CREATE TABLE IF NOT EXISTS pois(_id INTEGER PRIMARY KEY autoincrement,buildId, floor, x, y, name, time)");
			execSQL(database, "CREATE TABLE IF NOT EXISTS points(_id INTEGER PRIMARY KEY autoincrement,buildId, floor, x, y, mapPath, wifi, type, time)");
		}
	}
	
	private void onUpdateDatabase(SQLiteDatabase database, int oldVersion, int newVersion) {
		if(database != null) {
			execSQL(database, "CREATE TABLE IF NOT EXISTS pois(_id INTEGER PRIMARY KEY autoincrement,buildId, floor, x, y, name, time)");
			execSQL(database, "CREATE TABLE IF NOT EXISTS points(_id INTEGER PRIMARY KEY autoincrement,buildId, floor, x, y, mapPath, wifi, type, time)");
		}
	}
	
	
}
