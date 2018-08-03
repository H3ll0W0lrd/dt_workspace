package com.rtm.location.utils;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rtm.common.model.BuildInfo;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.location.entity.RMUser;

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
		if (RMFileUtil.createPath(RMFileUtil.getFingerDir())) {
			String path = RMFileUtil.getFingerDir() + RMFileUtil.SQLITE;
			File file = new File(path);
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
		}
		return database;
	}

	// 创建数据库表
	private void createDatabase(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE IF NOT EXISTS mapAngleInfo(_id INTEGER PRIMARY KEY autoincrement,buildId,mapAngle,timestamp)");
		database.execSQL("CREATE TABLE IF NOT EXISTS phoneA(_id INTEGER PRIMARY KEY autoincrement,lbsid,log_record_time,isbadlog_return,isphone_whitelist,expiration_time,bei_md5,bei_url)");
		database.execSQL("DROP TABLE IF EXISTS phone");
	}
	
	public void addUser(RMUser user) {
		RMUser i = getUser(user.getId());
		ContentValues values = new ContentValues();
		values.put("lbsid", user.getLbsid());
		values.put("log_record_time", user.getLog_record_time());
		values.put("isbadlog_return", user.getIsbadlog_return());
		values.put("isphone_whitelist", user.getIsphone_whitelist());
		values.put("expiration_time", user.getExpiration_time());
		values.put("bei_md5", user.getBei_md5());
		values.put("bei_url", user.getBei_url());
		if (i == null) {
			database.insert("phoneA", null, values);
		} else {
			database.update("phoneA", values, "lbsid=?",
					new String[] { user.getLbsid() });
		}
	}

	public void addInfo(BuildInfo info) {
		BuildInfo i = getBuildInfo(info.getBuildId());
		ContentValues values = new ContentValues();
		values.put("buildId", info.getBuildId());
		values.put("mapAngle", info.getMapAngle());
		values.put("timestamp", System.currentTimeMillis());
		if (i == null) {
			database.insert("mapAngleInfo", null, values);
		} else {
			database.update("mapAngleInfo", values, "buildId=?",
					new String[] { info.getBuildId() });
		}
	}

	public BuildInfo getBuildInfo(String buildId) {
		Cursor c = database.query("mapAngleInfo", null, "buildId=?",
				new String[] { buildId }, null, null, null);
		if (c.moveToNext()) {
			BuildInfo info = new BuildInfo();
			info.setBuildId(buildId);
			info.setMapAngle(c.getFloat(c.getColumnIndex("mapAngle")));
			return info;
		}
		return null;
	}
	public RMUser getUser(int _id) {
		Cursor c = database.query("phoneA", null, "_id=?",
				new String[] { _id+"" }, null, null, null);
		if (c.moveToNext()) {
			RMUser info = new RMUser();
			info.setId(_id);
			info.setLbsid(c.getString(c.getColumnIndex("lbsid")));
			info.setLog_record_time(c.getInt(c.getColumnIndex("log_record_time")));
			info.setBei_md5(c.getString(c.getColumnIndex("bei_md5")));
			info.setBei_url(c.getString(c.getColumnIndex("bei_url")));
			info.setExpiration_time(c.getString(c.getColumnIndex("expiration_time")));
			info.setId(c.getInt(c.getColumnIndex("_id")));
			info.setIsbadlog_return(c.getInt(c.getColumnIndex("isbadlog_return")));
			info.setIsphone_whitelist(c.getInt(c.getColumnIndex("isphone_whitelist")));
			return info;
		}
		return null;
	}
	
	public RMUser getUser() {
		Cursor c = database.query("phoneA", null, null,
				null, null, null, null);
		if (c.moveToNext()) {
			RMUser info = new RMUser();
			info.setLbsid(c.getString(c.getColumnIndex("lbsid")));
			info.setLog_record_time(c.getInt(c.getColumnIndex("log_record_time")));
			info.setExpiration_time(c.getString(c.getColumnIndex("expiration_time")));
			info.setId(c.getInt(c.getColumnIndex("_id")));
			info.setIsbadlog_return(c.getInt(c.getColumnIndex("isbadlog_return")));
			info.setIsphone_whitelist(c.getInt(c.getColumnIndex("isphone_whitelist")));
			info.setBei_md5(c.getString(c.getColumnIndex("bei_md5")));
			info.setBei_url(c.getString(c.getColumnIndex("bei_url")));
			return info;
		}
		return null;
	}
	
}
