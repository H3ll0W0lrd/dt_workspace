package com.rtmap.wifipicker.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rtm.frm.model.NavigatePoint;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.model.RMPoint;

public class WPDBService {
	private DatabaseHelper databaseHelper = null;
	private static WPDBService instance;

	private WPDBService() {
		databaseHelper = new DatabaseHelper();
	}

	public static WPDBService getInstance() {
		if (instance == null) {
			instance = new WPDBService();
		}
		return instance;
	}

	// 获取对应地图的所有数据库
	public HashMap<String, SQLiteDatabase> getBackupDataBases(String root,
			final String mapName) {
		// mapName: 860100010040500002-F1.5-0.jpg
		// database name: 860100010040500002-F2-0_518_470_1410428628600.db
		HashMap<String, SQLiteDatabase> dbs = new HashMap<String, SQLiteDatabase>();
		String[] files = FileHelper.listFiles(root, new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.startsWith(mapName) && filename.endsWith(".db")) {
					return true;
				}
				return false;
			}
		});
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				String[] strs = files[i].split("_");
				String key = strs[1] + "_" + strs[2]; // 起点坐标作为key
				String path = String.format("%s%s", root, files[i]);
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path,
						null);
				dbs.put(key, db);
			}
		}
		return dbs;
	}

	/**
	 * 刷选用户文件夹下文件名是否含有contaninStr
	 * 
	 * @param containStr
	 * @return
	 */
	public HashMap<String, SQLiteDatabase> getDataBases4ShowView(
			final String mMapName, String buildId) {
		// mapName: 860100010040500002-F1
		// database name: 860100010040500002-F2-0_518_470_1410428628600.db
		HashMap<String, SQLiteDatabase> dbs = new HashMap<String, SQLiteDatabase>();
		if (!StringUtil.isEmpty(mMapName)) {
			String root = Constants.WIFI_PICKER_PATH
					+ WPApplication.getInstance().getShare()
							.getString(DTFileUtils.PREFS_USERNAME, "") + "/"
					+ buildId + "/";
			String[] files = FileHelper.listFiles(root, new FilenameFilter() {// 筛选文件名含有mapname的文件

						@Override
						public boolean accept(File dir, String filename) {
							if (filename.contains(mMapName)
									&& (filename.endsWith(".db") || filename
											.endsWith(".db.export"))) {
								return true;
							}
							return false;
						}
					});
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					String path = String.format("%s%s", root, files[i]);
					DTLog.e("DB:path: " + path);
					SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
							path, null);
					dbs.put(path, db);
				}
			}
		}
		return dbs;
	}

	// 获取数据库中的wifi采集信息
	public ArrayList<String> getAllWifis(SQLiteDatabase database) {
		ArrayList<String> ret = new ArrayList<String>();
		if (database != null) {
			Cursor cursor = database.rawQuery(
					"SELECT wifi FROM points ORDER BY time ASC", null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					String wifi = cursor.getString(cursor
							.getColumnIndex("wifi"));
					ret.add(wifi);
				}
				cursor.close();
			}
		}
		return ret;
	}

	// 获取数据库中的点坐标信息
	public ArrayList<NavigatePoint> getAllPoints(SQLiteDatabase database,
			String floor) {
		ArrayList<NavigatePoint> ret = new ArrayList<NavigatePoint>();
		if (database != null) {
			Cursor cursor = database.rawQuery(
					"SELECT * FROM points ORDER BY time ASC", null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					float x = cursor.getFloat(cursor.getColumnIndex("x"));
					float y = cursor.getFloat(cursor.getColumnIndex("y"));
					NavigatePoint point = new NavigatePoint(_id + "", x, y, "",
							floor, "", 0);
					ret.add(point);
				}
				cursor.close();
			}
		}
		return ret;
	}

	/**
	 * 获取数据库中的点坐标信息
	 * 
	 * @param database
	 * @return
	 */
	public ArrayList<RMPoint> getAllPoints(SQLiteDatabase database) {
		ArrayList<RMPoint> ret = new ArrayList<RMPoint>();
		if (database != null) {
			Cursor cursor = database.rawQuery("SELECT * FROM points", null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					String buildId = cursor.getString(cursor
							.getColumnIndex("buildId"));
					String floor = cursor.getString(cursor
							.getColumnIndex("floor"));
					float x = cursor.getFloat(cursor.getColumnIndex("x"));
					float y = cursor.getFloat(cursor.getColumnIndex("y"));
					String type = cursor.getString(cursor
							.getColumnIndex("type"));
					String mapPath = cursor.getString(cursor
							.getColumnIndex("mapPath"));
					String wifi = cursor.getString(cursor
							.getColumnIndex("wifi"));
					long time = cursor.getLong(cursor.getColumnIndex("time"));
					RMPoint point = new RMPoint();
					point.set_id(_id);
					point.setFloor(floor);
					point.setX(x);
					point.setY(y);
					point.setBuildId(buildId);
					point.setTime(time);
					point.setType(type);
					point.setWifi(wifi);
					point.setMapPath(mapPath);
					ret.add(point);
				}
				cursor.close();
			}
		}
		return ret;
	}

	/**
	 * 删除此ID后的所有点
	 * 
	 * @param database
	 * @param id
	 */
	public void deletePointsById(SQLiteDatabase database, int id) {
		if (database == null) {
			return;
		}
		DTLog.e("id : " + id);
		database.execSQL("DELETE FROM points WHERE _id = ?",
				new Object[] { id });
	}

	public void insertPoint(String buildId, String floor, float x, float y,
			String mapPath, String wifi, String type) {
		SQLiteDatabase sDatabase = databaseHelper.getWritableDatabase();
		sDatabase
				.execSQL(
						"INSERT INTO points(buildId, floor, x, y, mapPath, wifi, type, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						new Object[] { buildId, floor, x, y, mapPath, wifi,
								type, new Date().getTime() });
	}

	public void deleteAllWifis() {
		SQLiteDatabase sDatabase = databaseHelper.getWritableDatabase();
		sDatabase.execSQL("DELETE FROM points");
	}

	public void closeDatabase(SQLiteDatabase sdb) {
		if (sdb != null) {
		}
	}

}
