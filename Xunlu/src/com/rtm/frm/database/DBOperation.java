package com.rtm.frm.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.model.Build;
import com.rtm.frm.model.Floor;
import com.rtm.frm.utils.XunluUtil;

/**
 * @author liyan
 * @explain 数据库操作方法类
 */
public class DBOperation {

	private XunluDbHelper mOpenHelper;

	private static DBOperation mDbOperation;

	public static DBOperation getInstance() {
		if (mDbOperation == null) {
			mDbOperation = new DBOperation();
		}
		return mDbOperation;
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 下午6:24:29
	 * @explain 批量插入楼层数据 ，直接使用代码插入
	 * @param list
	 *            批量插入的list数据
	 * @param buildsTable
	 *            批量插入的建筑物表名
	 */
	public void insertBuildsBatch(List<Build> list, String buildsTable) {
		synchronized (this) {
			mOpenHelper = XunluDbHelper.getInstance(XunluApplication.mApp);

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			synchronized (db) {
				db.beginTransaction();
				try {
					String cityName;
					for (Build buildObj : list) {
						cityName = buildObj.cityName;
						if (XunluUtil.isEmpty(cityName)) {
							cityName = XunluApplication.mApp.getResources()
									.getString(R.string.db_collect_build);
						}

						String sql = "insert into " + buildsTable
								+ " values (null," + "'" + cityName + "',"
								+ buildObj.id + "," + "'" + buildObj.name + "',"
								+ buildObj.size + "," + "'" + buildObj.nameJp2
								+ "'," + buildObj.lat + "," + buildObj.lng + ","
								+ "'" + buildObj.floors + "',"
								+ buildObj.versionData + "," + buildObj.versionMap
								+ "," + buildObj.isPrivate + ",'"+buildObj.googleLat+"','"+buildObj.googleLng+"');";
						db.execSQL(sql);
					}
					// 设置事务标志为成功，当结束事务时就会提交事务
					db.setTransactionSuccessful();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// 结束事务
					db.endTransaction();
					db.close();
				}
			}
		}
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 下午6:24:11
	 * @explain 批量出入楼层数据 ，直接使用代码插入
	 * @param list
	 *            楼层数据
	 * @param floorsTable
	 *            批量插入的楼层表名
	 */
	public void insertFloorsBatch(List<Floor> list, String floorsTable) {
		mOpenHelper = XunluDbHelper.getInstance(XunluApplication.mApp);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		synchronized (db) {
			db.beginTransaction();
			try {
				for (Floor floorObj : list) {
					String sql = "insert into " + floorsTable
							+ " values (null," + floorObj.buildId + "," + "'"
							+ floorObj.buildName + "'," + "'" + floorObj.floor
							+ "'," + floorObj.description + "," + "'"
							+ floorObj.description_1 + "'," + "'"
							+ floorObj.description_ + "'," + floorObj.width
							+ "," + floorObj.height + "," + "'"
							+ floorObj.levelTile + "'," + floorObj.isPrivate
							+ ");";
					db.execSQL(sql);
				}
				// 设置事务标志为成功，当结束事务时就会提交事务
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 结束事务
				db.endTransaction();
				db.close();
			}
		}
	}
	
	public void copyBuildData() {
		clearAllTableData(false);
		mOpenHelper = XunluDbHelper.getInstance(XunluApplication.mApp);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		mOpenHelper.copyDBCities(db);
		db.close();
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 下午1:46:01
	 * @explain 是否有本地建筑物数据，使用contentProvider
	 * @return 有true/无false
	 */
	public boolean isHaveLocalBuildsData() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		Cursor cursor = resolver.query(Builds.CONTENT_URI,
				new String[] { Builds.CITY_NAME }, " where "
						+ Builds.IS_PRIVATE + " == ?", new String[] { "0" },
				null);

		if (cursor == null) {
			return false;
		}
		int count = cursor.getCount();
		cursor.close();

		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 下午1:46:01
	 * @explain 是否有本地楼层数据,使用contentProvider
	 * @return 有true/无false
	 */
	public boolean isHaveLocalFloorsData() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		Cursor cursor = resolver.query(Floors.CONTENT_URI,
				new String[] { Floors.BUILD_ID }, Floors.IS_PRIVATE + " == ?",
				new String[] { "0" }, null);

		if (cursor == null) {
			return false;
		}
		int count = cursor.getCount();
		cursor.close();

		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @explain 清除所有表中的数据,使用contentProvider
	 * @return true 清除完毕 false 清除失败
	 */
	public boolean clearAllTableData(boolean isPrivate) {
		boolean buildsfinish = false;
		boolean floorsfinish = false;
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();

		resolver.delete(Builds.CONTENT_URI, Builds.IS_PRIVATE + " = ?",new String[]{"0"}); 
		resolver.delete(Floors.CONTENT_URI, Floors.IS_PRIVATE + " = ?",new String[]{"0"}); 
		
//		if (resolver.delete(Builds.CONTENT_URI, Builds.IS_PRIVATE + " == ?",
//				new String[] { (isPrivate ? 1 : 0) + "" }) > 0) {
//			buildsfinish = true;
//		}
//
//		if (resolver.delete(Floors.CONTENT_URI, Floors.IS_PRIVATE + " == ?",
//				new String[] { (isPrivate ? 1 : 0) + "" }) > 0) {
//			floorsfinish = true;
//		}
		resolver = null;
//		return buildsfinish && floorsfinish;
		return true;
	}
	
	/**
	 * @explain 清除优惠表中的数据,使用contentProvider
	 */
	public boolean clearFavorableTableData() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		resolver.delete(FavorablePois.CONTENT_URI, null, null);
		resolver = null;
		return true;
	}

	/**
	 * @return 拥有机场的城市列表
	 */
	public List<String> queryAirportCitys() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<String> citys = new ArrayList<String>();
		String[] projection = new String[] { "distinct " + Builds.CITY_NAME };
		String selection = " where " + Builds.NAME + " like ?";
		String[] selectionArgs = new String[] { "%机场%" };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);
		while (c != null && c.moveToNext()) {
			String cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			citys.add(cityName);
		}
		if (c != null) {
			c.close();
		}
		return citys;
	}

	/**
	 * @return 拥有商场的城市列表
	 */
	public List<String> queryMallCitys() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<String> citys = new ArrayList<String>();
		String[] projection = new String[] { "distinct " + Builds.CITY_NAME };
		String selection = " where " + Builds.NAME + " not like ?";
		String[] selectionArgs = new String[] { "%机场%" };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);

		while (c != null && c.moveToNext()) {
			String cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			citys.add(cityName);
		}
		if (c != null) {
			c.close();
		}
		return citys;
	}

	/**
	 * 根据cityName获取本地buildList
	 * 
	 * @param cityName
	 * @return 该城市所有建筑物列表
	 */
	public List<Build> queryBuildByCity(String cityName) {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> builds = new ArrayList<Build>();
		String[] projection = new String[] { "distinct " + Builds.NAME,
				Builds.CITY_NAME, Builds.BUILD_ID, Builds.SIZE,
				Builds.NAME_JP2, Builds.LAT, Builds.LNG, Builds.FLOORS,
				Builds.VERSION_DATA, Builds.VERSION_MAP,Builds.GOOGLE_LAT,Builds.GOOGLE_LNG };
		String selection = " where " + Builds.CITY_NAME + " == ? OR "+ Builds.CITY_NAME+" == ?";
		String[] selectionArgs = new String[] { cityName ,"个人收藏"};
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			build.googleLat = c.getString(c.getColumnIndex(Builds.GOOGLE_LAT));
			build.googleLng = c.getString(c.getColumnIndex(Builds.GOOGLE_LNG));
			if(Float.valueOf(build.lng) > 1 && Float.valueOf(build.lat) > 1) {
				builds.add(build);
			}
		}
		if (c != null) {
			c.close();
		}
		return builds;
	}

	/**
	 * @explain 获取所有私有建筑物
	 * @param id
	 * @return
	 */
	public List<Build> queryPrivateBuildAll() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> builds = new ArrayList<Build>();
		String[] projection = new String[] { Builds.CITY_NAME, Builds.BUILD_ID,
				Builds.NAME, Builds.SIZE, Builds.NAME_JP2, Builds.LAT,
				Builds.LNG, Builds.FLOORS, Builds.VERSION_DATA,
				Builds.VERSION_MAP, Builds.IS_PRIVATE ,Builds.GOOGLE_LAT,Builds.GOOGLE_LNG};
		String selection = " where " + Builds.IS_PRIVATE + " == ?";
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				new String[] { "1" }, null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			build.isPrivate = c.getInt(c.getColumnIndex(Builds.IS_PRIVATE));
			build.googleLat = c.getString(c.getColumnIndex(Builds.GOOGLE_LAT));
			build.googleLng = c.getString(c.getColumnIndex(Builds.GOOGLE_LNG));
			builds.add(build);
		}
		if (c != null) {
			c.close();
		}
		return builds;
	}

	/**
	 * 根据建筑物id，查询楼层信息
	 * 
	 * @param buildId
	 * @return
	 */
	public List<Floor> queryFloorByBuildId(String buildId) {
			List<Floor> floors = new ArrayList<Floor>();
			ContentResolver resolver = XunluApplication.mApp.getContentResolver();

			String[] projection = null;
			String selection = Floors.BUILD_ID + " ==  ?"; // 条件
			String[] selectionArgs = new String[] { buildId };

			Cursor c = null;
		synchronized (this) {

			projection = new String[] { "distinct " + Floors.BUILD_NAME,
					Floors.BUILD_ID, Floors.FLOOR, Floors.DESCRIPTION,
					Floors.DESCRIPTION_, Floors.DESCRIPTION_1, Floors.WIDTH,
					Floors.HEIGHT, Floors.LEVEL_TITLE };
			c = resolver.query(Floors.CONTENT_URI, projection, selection,
					selectionArgs, null);
			while (c != null && c.moveToNext()) {
				Floor floor = new Floor();
				floor.buildId = c.getString(c.getColumnIndex(Floors.BUILD_ID));
				floor.buildName = c.getString(c.getColumnIndex(Floors.BUILD_NAME));
				floor.floor = c.getString(c.getColumnIndex(Floors.FLOOR));
				floor.description = c.getString(c
						.getColumnIndex(Floors.DESCRIPTION));
				floor.description_ = c.getString(c
						.getColumnIndex(Floors.DESCRIPTION_));
				floor.description_1 = c.getString(c
						.getColumnIndex(Floors.DESCRIPTION_1));
				floor.width = c.getString(c.getColumnIndex(Floors.WIDTH));
				floor.height = c.getString(c.getColumnIndex(Floors.HEIGHT));
				floor.levelTile = c.getString(c.getColumnIndex(Floors.LEVEL_TITLE));
				floors.add(floor);
			}
			if (c != null) {
				c.close();
			}
		}
		
		return floors;
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 下午1:46:01
	 * @explain 是否有本地私有建筑物数据，使用contentProvider
	 * @return 有true/无false
	 */
	public boolean isHaveLocalPrivateBuildsData() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		Cursor cursor = resolver.query(Builds.CONTENT_URI,
				new String[] { Builds.CITY_NAME }, " where "
						+ Builds.IS_PRIVATE + " == ?", new String[] { "1" },
				null);

		if (cursor == null) {
			return false;
		}
		int count = cursor.getCount();
		cursor.close();

		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 下午1:46:01
	 * @explain 是否有本地私有楼层数据,使用contentProvider
	 * @return 有true/无false
	 */
	public boolean isHaveLocalPrivateFloorsData() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		Cursor cursor = resolver.query(Floors.CONTENT_URI,
				new String[] { Floors.BUILD_ID }, Floors.IS_PRIVATE + " == ?",
				new String[] { "1" }, null);

		if (cursor == null) {
			return false;
		}
		int count = cursor.getCount();
		cursor.close();

		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/******************************** 新增接口 *********************************/

	/**
	 * @explain 根据关键字 ，模糊查询
	 * @param keyword
	 * @return
	 */
	public List<Build> queryBuildByKeyLike(String keyword) {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> malls = new ArrayList<Build>();
		String[] projection = new String[] { "distinct " + Builds.NAME,
				Builds.CITY_NAME, Builds.BUILD_ID, Builds.SIZE,
				Builds.NAME_JP2, Builds.LAT, Builds.LNG, Builds.FLOORS,
				Builds.VERSION_DATA, Builds.VERSION_MAP,Builds.GOOGLE_LAT,Builds.GOOGLE_LNG };
		String selection = " where " + Builds.NAME + " like ? OR "
				+ Builds.CITY_NAME + " like ? OR lower(" + Builds.NAME_JP2
				+ ") like ?";
		String[] selectionArgs = new String[] { "%" + keyword + "%",
				"%" + keyword + "%", "%" + keyword + "%" };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			build.googleLat = c.getString(c.getColumnIndex(Builds.GOOGLE_LAT));
			build.googleLng = c.getString(c.getColumnIndex(Builds.GOOGLE_LNG));
			malls.add(build);
		}
		if (c != null) {
			c.close();
		}
		return malls;
	}

	/**
	 * 根据build获取本地build对象
	 * 
	 * @param id
	 * @return
	 */
	public Build queryBuildById(String id) {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		Build build = null;
		String[] projection = new String[] { Builds.CITY_NAME, Builds.BUILD_ID,
				Builds.NAME, Builds.SIZE, Builds.NAME_JP2, Builds.LAT,
				Builds.LNG, Builds.FLOORS, Builds.VERSION_DATA,
				Builds.VERSION_MAP, Builds.IS_PRIVATE, Builds.GOOGLE_LAT,Builds.GOOGLE_LNG};
		String selection = " where " + Builds.BUILD_ID + " = ?";
		String[] selectionArgs = new String[] { id };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);
		while (c != null && c.moveToNext()) {
			build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			build.isPrivate = c.getInt(c.getColumnIndex(Builds.IS_PRIVATE));
			build.googleLat = c.getString(c.getColumnIndex(Builds.GOOGLE_LAT));
			build.googleLng = c.getString(c.getColumnIndex(Builds.GOOGLE_LNG));
		}
		if (c != null) {
			c.close();
		}
		return build;
	}

	/**
	 * @explain 获取所有建筑物
	 * @return
	 */
	public List<Build> queryBuildAll() {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> builds = new ArrayList<Build>();
		String[] projection = new String[] { " distinct " + Builds.NAME,
				Builds.CITY_NAME, Builds.BUILD_ID, Builds.SIZE,
				Builds.NAME_JP2, Builds.LAT, Builds.LNG, Builds.FLOORS,
				Builds.VERSION_DATA, Builds.VERSION_MAP ,Builds.GOOGLE_LAT,Builds.GOOGLE_LNG};
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, "", null,
				null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c.getString(c.getColumnIndex(Builds.VERSION_MAP));
			build.googleLat = c.getString(c.getColumnIndex(Builds.GOOGLE_LAT));
			build.googleLng = c.getString(c.getColumnIndex(Builds.GOOGLE_LNG));
			if (Float.valueOf(build.lng) > 1 && Float.valueOf(build.lat) > 1) {
				builds.add(build);
			}
		}
		if (c != null) {
			c.close();
		}
		return builds;
	}
	
	/**
	 * @explain 根据经纬度点以及半径，进行筛选
	 * @return
	 */
	public List<Build> queryBuildsByLngLat(LatLng latLng,double radius) {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> builds = new ArrayList<Build>();
		String[] projection = new String[] { " distinct " + Builds.NAME,
				Builds.CITY_NAME, Builds.BUILD_ID, Builds.SIZE,
				Builds.NAME_JP2, Builds.LAT, Builds.LNG, Builds.FLOORS,
				Builds.VERSION_DATA, Builds.VERSION_MAP,Builds.GOOGLE_LAT,Builds.GOOGLE_LNG };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, "", null,
				null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			build.googleLat = c.getString(c.getColumnIndex(Builds.GOOGLE_LAT));
			build.googleLng = c.getString(c.getColumnIndex(Builds.GOOGLE_LNG));
			double s = XunluUtil.distanceByLatLng(latLng.latitude, latLng.longitude, Double.valueOf(build.lat), Double.valueOf(build.lng));
			if(s <= radius ) {
				builds.add(build);
			}
		}
		if (c != null) {
			c.close();
		}
		return builds;
	}

	/**
	 * @explain 根据城市名称查询该城市的机场
	 * @param cityName
	 * @return
	 */
	public List<Build> queryAirportByCityName(String cityName) {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> airports = new ArrayList<Build>();
		String[] projection = new String[] { "distinct " + Builds.NAME,
				Builds.CITY_NAME, Builds.BUILD_ID, Builds.SIZE,
				Builds.NAME_JP2, Builds.LAT, Builds.LNG, Builds.FLOORS,
				Builds.VERSION_DATA, Builds.VERSION_MAP };
		String selection = " where " + Builds.NAME + " like ? AND "
				+ Builds.CITY_NAME + " == ?";
		String[] selectionArgs = new String[] { "%机场%", cityName };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			airports.add(build);
		}
		if (c != null) {
			c.close();
		}
		return airports;
	}

	/**
	 * @explain 根据城市名称，查询该城市所有的商场
	 * @param cityName
	 * @return
	 */
	public List<Build> queryMallByCityName(String cityName) {
		ContentResolver resolver = XunluApplication.mApp.getContentResolver();
		List<Build> malls = new ArrayList<Build>();
		String[] projection = new String[] { "distinct " + Builds.NAME,
				Builds.CITY_NAME, Builds.BUILD_ID, Builds.SIZE,
				Builds.NAME_JP2, Builds.LAT, Builds.LNG, Builds.FLOORS,
				Builds.VERSION_DATA, Builds.VERSION_MAP };
		String selection = " where " + Builds.NAME + " not like ? AND "
				+ Builds.CITY_NAME + " == ?";
		String[] selectionArgs = new String[] { "%机场%", cityName };
		Cursor c = resolver.query(Builds.CONTENT_URI, projection, selection,
				selectionArgs, null);
		while (c != null && c.moveToNext()) {
			Build build = new Build();
			build.cityName = c.getString(c.getColumnIndex(Builds.CITY_NAME));
			build.id = c.getString(c.getColumnIndex(Builds.BUILD_ID));
			build.name = c.getString(c.getColumnIndex(Builds.NAME));
			build.size = c.getString(c.getColumnIndex(Builds.SIZE));
			build.nameJp2 = c.getString(c.getColumnIndex(Builds.NAME_JP2));
			build.lat = c.getString(c.getColumnIndex(Builds.LAT));
			build.lng = c.getString(c.getColumnIndex(Builds.LNG));
			build.floors = c.getString(c.getColumnIndex(Builds.FLOORS));
			build.versionData = c.getString(c
					.getColumnIndex(Builds.VERSION_DATA));
			build.versionMap = c
					.getString(c.getColumnIndex(Builds.VERSION_MAP));
			malls.add(build);
		}
		if (c != null) {
			c.close();
		}
		return malls;
	}
	

	/******************************** 优惠信息部分 **************************/
	public void insertFavorablePois(List<com.rtm.frm.model.FavorablePoiDbModel> pois) {
		SQLiteDatabase db = XunluDbHelper.getInstance(XunluApplication.mApp).getWritableDatabase();
		synchronized (db) {
			db.beginTransaction();
			try {
				for (com.rtm.frm.model.FavorablePoiDbModel floorObj : pois) {
					String sql = "insert into " + FavorablePois.TABLE_NAME + " values (null, '" + floorObj.cityName + "','" + floorObj.buildId + "','"
							+ floorObj.floor + "','" + floorObj.poiX + "','" + floorObj.poiY + "','" + floorObj.poiId + "','" + floorObj.adUrl + "','"
							+ floorObj.adBigUrl + "',"+ floorObj.adLevel + ",'" + floorObj.categoryCode + "','" + floorObj.poiName + "','" + floorObj.discription + "','"
							+ floorObj.startTime + "','" + floorObj.endTime + "','" + floorObj.number + "','"+floorObj.noCardPay+"');";
					db.execSQL(sql);
				}
				// 设置事务标志为成功，当结束事务时就会提交事务
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 结束事务
				db.endTransaction();
				db.close();
			}
		}
	}

	public List<com.rtm.frm.model.FavorablePoiDbModel> queryFavorablePoisByCity(String cityName) {
		SQLiteDatabase dbDatabase = XunluDbHelper.getInstance(XunluApplication.mApp).getWritableDatabase();
		String sql = "SELECT * FROM " + FavorablePois.TABLE_NAME + " INNER JOIN " + Builds.TABLE_NAME + " ON " + FavorablePois.TABLE_NAME + "."
				+ FavorablePois.BUILD_ID + " = " + Builds.TABLE_NAME + "." + Builds.BUILD_ID;
		String whereString = " WHERE " + FavorablePois.TABLE_NAME + "." + FavorablePois.CITY_NAME + " = ?";
		Cursor c = dbDatabase.rawQuery(sql + whereString, new String[] { cityName });

		List<com.rtm.frm.model.FavorablePoiDbModel> pois = new ArrayList<com.rtm.frm.model.FavorablePoiDbModel>();
		while (c.moveToNext()) {
			com.rtm.frm.model.FavorablePoiDbModel poi = new com.rtm.frm.model.FavorablePoiDbModel();
			poi.cityName = c.getString(c.getColumnIndex(FavorablePois.CITY_NAME));
			poi.buildId = c.getString(c.getColumnIndex(FavorablePois.BUILD_ID));
			poi.floor = c.getString(c.getColumnIndex(FavorablePois.FLOOR));
			poi.poiX = c.getString(c.getColumnIndex(FavorablePois.POI_X));
			poi.poiY = c.getString(c.getColumnIndex(FavorablePois.POI_Y));
			poi.poiId = c.getString(c.getColumnIndex(FavorablePois.POI_ID));
			poi.categoryCode = c.getString(c.getColumnIndex(FavorablePois.CATEGORY_CODE));
			poi.poiName = c.getString(c.getColumnIndex(FavorablePois.POI_NAME));
			poi.adLevel = Integer.valueOf(c.getString(c.getColumnIndex(FavorablePois.AD_LEVEL)));
			poi.discription = c.getString(c.getColumnIndex(FavorablePois.DESCRIPTION));
			poi.startTime = c.getString(c.getColumnIndex(FavorablePois.START_TIME));
			poi.endTime = c.getString(c.getColumnIndex(FavorablePois.END_TIME));
			poi.adUrl = c.getString(c.getColumnIndex(FavorablePois.AD_URL));
			poi.adBigUrl = c.getString(c.getColumnIndex(FavorablePois.AD_BIG_URL));
			poi.number = c.getString(c.getColumnIndex(FavorablePois.POI_NO));
			poi.buildName = c.getString(c.getColumnIndex(Builds.NAME));
			poi.noCardPay = c.getString(c.getColumnIndex(FavorablePois.POI_NO_CARD_PAY));
			pois.add(poi);
		}
		if (c != null) {
			c.close();
		}
		dbDatabase.close();
		return pois;
	}
	
	public List<com.rtm.frm.model.FavorablePoiDbModel> queryFavorablePoisAll() {
		SQLiteDatabase dbDatabase = XunluDbHelper.getInstance(XunluApplication.mApp).getWritableDatabase();
		String sql = "SELECT * FROM " + FavorablePois.TABLE_NAME + " INNER JOIN " + Builds.TABLE_NAME + " ON " + FavorablePois.TABLE_NAME + "."
				+ FavorablePois.BUILD_ID + " = " + Builds.TABLE_NAME + "." + Builds.BUILD_ID;
//		String whereString = " WHERE " + FavorablePois.TABLE_NAME + "." + FavorablePois.CITY_NAME + " = ?";
		Cursor c = dbDatabase.rawQuery(sql, null);

		List<com.rtm.frm.model.FavorablePoiDbModel> pois = new ArrayList<com.rtm.frm.model.FavorablePoiDbModel>();
		while (c.moveToNext()) {
			com.rtm.frm.model.FavorablePoiDbModel poi = new com.rtm.frm.model.FavorablePoiDbModel();
			poi.cityName = c.getString(c.getColumnIndex(FavorablePois.CITY_NAME));
			poi.buildId = c.getString(c.getColumnIndex(FavorablePois.BUILD_ID));
			poi.floor = c.getString(c.getColumnIndex(FavorablePois.FLOOR));
			poi.poiX = c.getString(c.getColumnIndex(FavorablePois.POI_X));
			poi.poiY = c.getString(c.getColumnIndex(FavorablePois.POI_Y));
			poi.poiId = c.getString(c.getColumnIndex(FavorablePois.POI_ID));
			poi.categoryCode = c.getString(c.getColumnIndex(FavorablePois.CATEGORY_CODE));
			poi.poiName = c.getString(c.getColumnIndex(FavorablePois.POI_NAME));
			poi.adLevel = Integer.valueOf(c.getString(c.getColumnIndex(FavorablePois.AD_LEVEL)));
			poi.discription = c.getString(c.getColumnIndex(FavorablePois.DESCRIPTION));
			poi.startTime = c.getString(c.getColumnIndex(FavorablePois.START_TIME));
			poi.endTime = c.getString(c.getColumnIndex(FavorablePois.END_TIME));
			poi.adUrl = c.getString(c.getColumnIndex(FavorablePois.AD_URL));
			poi.adBigUrl = c.getString(c.getColumnIndex(FavorablePois.AD_BIG_URL));
			poi.number = c.getString(c.getColumnIndex(FavorablePois.POI_NO));
			poi.buildName = c.getString(c.getColumnIndex(Builds.NAME));
			poi.noCardPay = c.getString(c.getColumnIndex(FavorablePois.POI_NO_CARD_PAY));
			pois.add(poi);
		}
		if (c != null) {
			c.close();
		}
		dbDatabase.close();
		return pois;
	}
	
	public List<String> queryFavorableCitys() {
		SQLiteDatabase dbDatabase = XunluDbHelper.getInstance(XunluApplication.mApp).getWritableDatabase();
		String sql = "SELECT distinct " + FavorablePois.CITY_NAME + " FROM " + FavorablePois.TABLE_NAME ;
//		String whereString = " WHERE " + FavorablePois.TABLE_NAME + "." + FavorablePois.CITY_NAME + " = ?";
		Cursor c = dbDatabase.rawQuery(sql, null);

		List<String> citys = new ArrayList<String>();
		while (c.moveToNext()) {
			String city = c.getString(c.getColumnIndex(FavorablePois.CITY_NAME));
			citys.add(city);
		}
		if (c != null) {
			c.close();
		}
		dbDatabase.close();
		return citys;
	}
}
