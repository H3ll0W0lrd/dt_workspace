package com.rtmap.ambassador.core;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rtmap.ambassador.model.Request;
import com.rtmap.ambassador.model.User;
import com.rtmap.ambassador.util.DTFileUtil;
import com.rtmap.ambassador.util.DTLog;

public class DTSqlite {

	private static DTSqlite instance;
	public static final int STATELOGIN = 1;// 登录状态为1
	public static final int STATELOGOUT = 0;// 退出登录状态为0
	private static final String USER_TABLE = "user";
	private static final String REQUEST_TABLE = "request";

	public static DTSqlite getInstance() {
		if (instance == null) {
			instance = new DTSqlite();
		}
		return instance;
	}

	private SQLiteDatabase database;

	public DTSqlite() {
		database = getWritableDatabase();
	}

	// 创建sd卡缓存数据库
	private SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase database = null;
		String path = DTFileUtil.getDataDir() + "info.d";
		File file = new File(path);
		Log.i("rtmap", file.exists() + "   " + path);
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

	public void createDatabase(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS user(_id INTEGER PRIMARY KEY autoincrement,user_id,qrCode,staffCode,staffName,login)");
		db.execSQL("CREATE TABLE IF NOT EXISTS request(_id INTEGER PRIMARY KEY autoincrement,url,params)");
	}

	public void insertUser(User user) {
		SQLiteDatabase db = database;
		Cursor c = db.query(USER_TABLE, null, "user_id=?",
				new String[] { "1" }, null, null, null);
		if (c.moveToNext()) {// 有该用户
			updateUser(user, STATELOGIN);
		} else {
			db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("user_id", user.getId() + "");
			values.put("qrCode", user.getQrCode());
			values.put("staffCode", user.getStaffCode());
			values.put("staffName", user.getStaffName());
			values.put("login", STATELOGIN + "");
			DTLog.e(db.insert(USER_TABLE, null, values) + "");
		}
		c.close();
	}

	/**
	 * 缓存请求
	 */
	public void insertRequest(String url, String params) {
		SQLiteDatabase db = database;
		db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("url", url);
		values.put("params", params);
		DTLog.e(db.insert(REQUEST_TABLE, null, values) + "");
	}
	
	/**
	 * 得到缓存请求
	 */
	public ArrayList<Request> getRequestList() {
		SQLiteDatabase db = database;
		Cursor c = db.query(REQUEST_TABLE, null, null,
				null, null, null, null);
		ArrayList<Request> list = new ArrayList<Request>();
		while(c.moveToNext()) {
			Request request = new Request();
			request.setId(c.getInt(c.getColumnIndex("_id")));
			request.setUrl(c.getString(c.getColumnIndex("url")));
			request.setParams(c.getString(c.getColumnIndex("params")));
			list.add(request);
		}
		c.close();
		return list;
	}
	
	public void deleteRequest(int id) {
		String sql = "delete from "+REQUEST_TABLE+" where _id="+id;
		SQLiteDatabase db = database;
		db.execSQL(sql);
	}

	public void updateUser(User user, int isLogin) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("user_id", user.getId() + "");
		values.put("qrCode", user.getQrCode());
		values.put("staffCode", user.getStaffCode());
		values.put("staffName", user.getStaffName());
		values.put("login", isLogin + "");
		db.update(USER_TABLE, values, "user_id=?", new String[] { user.getId()
				+ "" });
		db.close();
	}

	/**
	 * 得到登录用户
	 */
	public User getUser() {
		SQLiteDatabase db = database;
		Cursor c = db.query(USER_TABLE, null, "login=?",
				new String[] { STATELOGIN + "" }, null, null, null);
		if (c.moveToNext()) {
			User mUser = new User();
			mUser.setId(c.getInt(c.getColumnIndex("user_id")));
			mUser.setQrCode(c.getString(c.getColumnIndex("qrCode")));
			mUser.setStaffCode(c.getString(c.getColumnIndex("staffCode")));
			mUser.setStaffName(c.getString(c.getColumnIndex("staffName")));
			mUser.setLogin(STATELOGIN);
			c.close();
			return mUser;
		}
		c.close();
		return null;
	}

	/**
	 * 退出登录
	 * 
	 * @param tb_user_id
	 */
	public void exitLogin() {
		String sql = "update " + USER_TABLE + " set login='0' where login='1'";
		SQLiteDatabase db = database;
		db.execSQL(sql);
	}
}
