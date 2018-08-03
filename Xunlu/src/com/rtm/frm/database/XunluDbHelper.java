package com.rtm.frm.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@SuppressLint("SdCardPath")
public class XunluDbHelper extends SQLiteOpenHelper {
	
	private static String DATABASE_PATH = "/data/data/";

	private static final String DATABASE_NAME = "xunlu.db";

	private static final int DATABASE_VERSION = 3;
	
	private Context mContext;
	
	 private static XunluDbHelper mInstance;  
	
	public synchronized static XunluDbHelper getInstance(Context context) {  
        if (mInstance == null) {  
            mInstance = new XunluDbHelper(context);  
        }  
        return mInstance;  
    };  
	
	public XunluDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db,Builds.TABLE_NAME,Builds.TABLE_COLUMNS,Builds.TABLE_INDEXES);
		createTable(db,Floors.TABLE_NAME,Floors.TABLE_COLUMNS,Floors.TABLE_INDEXES);
		//创建优惠表
		createTable(db, FavorablePois.TABLE_NAME, FavorablePois.TABLE_COLUMNS, FavorablePois.TABLE_INDEXES);
		
		DATABASE_PATH = DATABASE_PATH + mContext.getPackageName() + "/databases/"+DATABASE_NAME;
		copyDBCities(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 升级数据库
		Log.e("XunluDbHelper", "onUpgrade");
		dropTable(db, Builds.TABLE_NAME);
		dropTable(db, Floors.TABLE_NAME);
		dropTable(db, FavorablePois.TABLE_NAME);
		
		createTable(db,Builds.TABLE_NAME,Builds.TABLE_COLUMNS,Builds.TABLE_INDEXES);
		createTable(db,Floors.TABLE_NAME,Floors.TABLE_COLUMNS,Floors.TABLE_INDEXES);
		createTable(db, FavorablePois.TABLE_NAME, FavorablePois.TABLE_COLUMNS, FavorablePois.TABLE_INDEXES);
		DATABASE_PATH = DATABASE_PATH + mContext.getPackageName() + "/databases/"+DATABASE_NAME;
		copyDBCities(db);
	}
	
	private void dropTable(SQLiteDatabase db,String tableName) {
		String sql = "DROP TABLE ";
		db.execSQL(sql + tableName);
	}
	
	private static void createTable(SQLiteDatabase db, String tableName,
			String[] columns, String[] indexes) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE ").append(tableName).append(" (");
		for (int i = 0, c = columns.length; i < c; ++i) {
			if (i != 0)
				sqlBuilder.append(",");
			sqlBuilder.append(columns[i]);
		}
		sqlBuilder.append(");");
		for (int i = 0, c = indexes.length; i < c; ++i) {
			sqlBuilder.append("CREATE INDEX ").append(tableName).append("_")
					.append(indexes[i]).append(" ON ").append(tableName).append("(")
					.append(indexes[i]).append(");");
		}
		db.execSQL(sqlBuilder.toString());
	}
	
    /**
     * @explain 复制数据库内容
     * @param db
     */
    public void copyDBCities(SQLiteDatabase db) {
		File cachefile = mContext.getCacheDir();
		File dbFile = new File(cachefile, DATABASE_NAME);
		try {
			InputStream is = mContext.getAssets().open(DATABASE_NAME); // 获取数据库库文件输入流
			FileOutputStream fos = new FileOutputStream(dbFile); // 定义输出流
			byte[] bt = new byte[8192];
			int len = -1;
			while ((len = is.read(bt)) != -1) {
				fos.write(bt, 0, len);
			}
			is.close();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		SQLiteDatabase assetdb = SQLiteDatabase.openDatabase(dbFile.getPath(),
				null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		if (assetdb != null) {
			Cursor assetDbBuildsCursor = assetdb.query(
					Builds.TABLE_NAME, null, null, null, null,
					null, null);

			Log.e("开始插入", "开始插入");
			db.beginTransaction(); // 手动设置开始事务
			while (assetDbBuildsCursor.moveToNext()) {
				ContentValues value = new ContentValues();
				value.put(Builds.CITY_NAME, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.CITY_NAME)));
				value.put(Builds.BUILD_ID, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.BUILD_ID)));
				value.put(Builds.NAME, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.NAME)));
				value.put(Builds.SIZE, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.SIZE)));
				value.put(Builds.NAME_JP2, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.NAME_JP2)));
				value.put(Builds.LAT, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.LAT)));
				value.put(Builds.LNG, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.LNG)));
				value.put(Builds.FLOORS, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.FLOORS)));
				value.put(Builds.VERSION_DATA, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.VERSION_DATA)));
				value.put(Builds.VERSION_MAP, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.VERSION_MAP)));
				value.put(Builds.IS_PRIVATE, assetDbBuildsCursor
						.getInt(assetDbBuildsCursor
								.getColumnIndex(Builds.IS_PRIVATE)));
				value.put(Builds.GOOGLE_LAT, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.GOOGLE_LAT)));
				value.put(Builds.GOOGLE_LNG, assetDbBuildsCursor
						.getString(assetDbBuildsCursor
								.getColumnIndex(Builds.GOOGLE_LNG)));
				db.insert(Builds.TABLE_NAME, null, value);
			}
			
			
			Cursor assetDbFloorsCursor = assetdb.query(
					Floors.TABLE_NAME, null, null, null, null,
					null, null);
			while (assetDbFloorsCursor.moveToNext()) {
				ContentValues value = new ContentValues();
				value.put(Floors.BUILD_ID, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.BUILD_ID)));
				value.put(Floors.BUILD_NAME, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.BUILD_NAME)));
				value.put(Floors.FLOOR, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.FLOOR)));
				value.put(Floors.DESCRIPTION, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.DESCRIPTION)));
				value.put(Floors.DESCRIPTION_1, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.DESCRIPTION_1)));
				value.put(Floors.DESCRIPTION_, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.DESCRIPTION_)));
				value.put(Floors.WIDTH, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.WIDTH)));
				value.put(Floors.HEIGHT, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.HEIGHT)));
				value.put(Floors.LEVEL_TITLE, assetDbFloorsCursor
						.getString(assetDbFloorsCursor
								.getColumnIndex(Floors.LEVEL_TITLE)));
				value.put(Floors.IS_PRIVATE, assetDbFloorsCursor
						.getInt(assetDbFloorsCursor
								.getColumnIndex(Floors.IS_PRIVATE)));
				
				db.insert(Floors.TABLE_NAME, null, value);
			}
			
			Cursor assetDbFavorableCursor = assetdb.query(
					FavorablePois.TABLE_NAME, null, null, null, null,
					null, null);
			while (assetDbFavorableCursor.moveToNext()) {
				ContentValues value = new ContentValues();
				value.put(FavorablePois.CITY_NAME, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.CITY_NAME)));
				value.put(FavorablePois.BUILD_ID, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.BUILD_ID)));
				value.put(FavorablePois.FLOOR, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.FLOOR)));
				value.put(FavorablePois.POI_X, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.POI_X)));
				value.put(FavorablePois.POI_Y, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.POI_Y)));
				value.put(FavorablePois.POI_ID, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.POI_ID)));
				value.put(FavorablePois.CATEGORY_CODE, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.CATEGORY_CODE)));
				value.put(FavorablePois.POI_NAME, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.POI_NAME)));
				value.put(FavorablePois.AD_LEVEL, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.AD_LEVEL)));
				value.put(FavorablePois.DESCRIPTION, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.DESCRIPTION)));
				value.put(FavorablePois.START_TIME, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.START_TIME)));
				value.put(FavorablePois.END_TIME, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.END_TIME)));
				value.put(FavorablePois.AD_URL, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.AD_URL)));
				value.put(FavorablePois.AD_BIG_URL, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.AD_BIG_URL)));
				value.put(FavorablePois.POI_NO, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.POI_NO)));
				value.put(FavorablePois.POI_NO_CARD_PAY, assetDbFavorableCursor
						.getString(assetDbFavorableCursor
								.getColumnIndex(FavorablePois.POI_NO_CARD_PAY)));
				
				db.insert(FavorablePois.TABLE_NAME, null, value);
			}
			db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
			db.endTransaction(); // 处理完成
			
			assetDbBuildsCursor.close();
			assetDbFloorsCursor.close();
			assetDbFavorableCursor.close();

			Log.e("插入结束", "插入结束");
			assetdb.close();
		} else {
			Log.i("DBHelper", "copy db is null");
		}

	}
}
