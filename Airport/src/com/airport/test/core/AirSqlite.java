package com.airport.test.core;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.airport.test.model.CateData;
import com.airport.test.model.MsgData;
import com.dingtao.libs.DTApplication;
import com.rtm.common.model.POI;

public class AirSqlite extends SQLiteOpenHelper {
	private static AirSqlite instance;

	public static AirSqlite getInstance() {
		if (instance == null) {
			instance = new AirSqlite(DTApplication.getInstance());
		}
		return instance;
	}

	public AirSqlite(Context context) {
		super(context, "msg", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDatabase(db);
	}

	// 创建数据库表
	private void createDatabase(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE IF NOT EXISTS msg(_id INTEGER PRIMARY KEY autoincrement,avatar,text,poiNo,time,gone)");
		database.execSQL("CREATE TABLE IF NOT EXISTS search(_id INTEGER PRIMARY KEY autoincrement,text)");
		database.execSQL("CREATE TABLE IF NOT EXISTS catetb(_id INTEGER PRIMARY KEY autoincrement,name,drawid,gone)");
		database.execSQL("insert into msg(_id,text,poiNo,gone) values(1,'当前安检人数较多，请合理安排您的时间，以免耽误乘机',131,0)");
		database.execSQL("insert into msg(_id,text,poiNo,gone) values(2,'午餐时间到啦！已为您推荐有优惠的餐厅，是否查看',131,0)");
		database.execSQL("insert into msg(_id,text,poiNo,gone) values(3,'还有30分钟就要开始登机了，请前往您登机口乘机',131,0)");
		database.execSQL("insert into msg(_id,text,poiNo,gone) values(4,'附近的中信书店上架了您感兴趣的新书籍',131,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(0,'机场出行',2130837561,1)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(1,'机场到达',2130837561,1)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(2,'机场中转',2130837561,1)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(3,'购物',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(4,'餐饮',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(9,'休闲娱乐',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(5,'机场设备',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(6,'书店',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(7,'中餐',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(8,'西餐',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(10,'快餐',2130837561,0)");
		database.execSQL("insert into catetb(_id,name,drawid,gone) values(11,'交通',2130837561,0)");
	}

	public void insertSearchHistory(String name) {
		SQLiteDatabase database = getWritableDatabase();
		database.execSQL("insert into search(text) values('" + name + "')");
	}

	public void deleteSearch() {
		SQLiteDatabase database = getWritableDatabase();
		database.execSQL("DROP TABLE IF EXISTS search");
		database.execSQL("CREATE TABLE IF NOT EXISTS search(_id INTEGER PRIMARY KEY autoincrement,text)");
	}

	public void delete() {
		SQLiteDatabase database = getWritableDatabase();
		ArrayList<MsgData> list = getMsgInfoList();
		for (MsgData data : list) {
			ContentValues values = new ContentValues();
			values.put("gone", 0);
			database.update("msg", values, "_id=?",
					new String[] { data.getMid() + "" });
		}
	}

	public ArrayList<POI> getSearchList() {
		SQLiteDatabase database = getReadableDatabase();
		Cursor c = database.query("search", null, null, null, null, null, null);
		ArrayList<POI> list = new ArrayList<POI>();
		while (c.moveToNext()) {
			POI info = new POI();
			info.setName(c.getString(c.getColumnIndex("text")));
			list.add(info);
		}
		return list;
	}

	public void update(int mid) {
		SQLiteDatabase database = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("gone", 1);
		database.update("msg", values, "_id=?", new String[] { mid + "" });
	}

	public void updatecatetb(ArrayList<CateData> list) {
		SQLiteDatabase database = getWritableDatabase();
		ContentValues values = new ContentValues();
		for (int i = 0; i < list.size(); i++) {
			CateData data = list.get(i);
			values.put("name", data.getName());
			values.put("drawid", data.getIconid());
			values.put("gone", data.isCheck()?1:0);
			database.update("catetb", values, "_id=?", new String[] { i + "" });
		}
	}
	public ArrayList<CateData> getcatetbList() {
		SQLiteDatabase database = getReadableDatabase();
		Cursor c = database.query("catetb", null, null, null, null, null, null);
		ArrayList<CateData> list = new ArrayList<CateData>();
		while (c.moveToNext()) {
			CateData info = new CateData();
			info.setName(c.getString(c.getColumnIndex("name")));
			info.setIconid(c.getInt(c.getColumnIndex("drawid")));
			int gone = c.getInt(c.getColumnIndex("gone"));
			info.setCheck(gone==0?false:true);
			list.add(info);
		}
		c.close();
		return list;
	}
	

	public ArrayList<MsgData> getMsgInfoList() {
		SQLiteDatabase database = getReadableDatabase();
		Cursor c = database.query("msg", null, null, null, null, null, null);
		ArrayList<MsgData> list = new ArrayList<MsgData>();
		while (c.moveToNext()) {
			MsgData info = new MsgData();
			info.setMid(c.getInt(c.getColumnIndex("_id")));
			info.setAvatar(c.getString(c.getColumnIndex("avatar")));
			info.setPoiNo(c.getInt(c.getColumnIndex("poiNo")));
			info.setText(c.getString(c.getColumnIndex("text")));
			info.setGone(c.getInt(c.getColumnIndex("gone")));
			list.add(info);
		}
		return list;
	}

	public MsgData getMsgInfo(int mid) {
		SQLiteDatabase database = getReadableDatabase();
		Cursor c = database.query("msg", null, "_id=?",
				new String[] { mid + "" }, null, null, null);
		while (c.moveToNext()) {
			MsgData info = new MsgData();
			info.setMid(c.getInt(c.getColumnIndex("_id")));
			info.setAvatar(c.getString(c.getColumnIndex("avatar")));
			info.setPoiNo(c.getInt(c.getColumnIndex("poiNo")));
			info.setText(c.getString(c.getColumnIndex("text")));
			info.setGone(c.getInt(c.getColumnIndex("gone")));
			return info;
		}
		return null;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS msg");
		db.execSQL("DROP TABLE IF EXISTS search");
		db.execSQL("DROP TABLE IF EXISTS catetb");
		onCreate(db);
	}
}
