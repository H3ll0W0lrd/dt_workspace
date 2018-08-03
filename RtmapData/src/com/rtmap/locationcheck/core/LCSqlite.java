package com.rtmap.locationcheck.core;

import java.io.File;
import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTFileUtils;

public class LCSqlite {

	private static LCSqlite instance;

	public static LCSqlite getInstance() {
		if (instance == null) {
			instance = new LCSqlite();
		}
		return instance;
	}

	private LCSqlite() {
	}

	/**
	 * DAO对象的使用参见OrmLite的api
	 */
	public Dao<BeaconInfo, String> createBeaconTable(String buildId,
			String floor) {
		try {

			// 使用SQLiteDatabase创建或者打开sd或者assets下的db文件
			DTFileUtils.createDirs(DTFileUtils.getDataDir() + buildId
					+ File.separator);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
					DTFileUtils.getDataDir() + buildId + File.separator
							+ buildId + "_" + floor + ".db", null);
			// ORMLite的android.jar封装SQLiteDatabase
			AndroidConnectionSource connectionSource = new AndroidConnectionSource(
					db);

			/**
			 * 以下为官方首页快速使用方式，没有任何改动的照搬，由于需要ConnectionSource对象，
			 * 通过查看API发现子类AndroidConnectionSource
			 */

			// instantiate the DAO to handle Account with String id
			Dao<BeaconInfo, String> accountDao = DaoManager.createDao(
					connectionSource, BeaconInfo.class);

			// if you need to create the 'accounts' table make this call
			TableUtils.createTableIfNotExists(connectionSource,
					BeaconInfo.class);

			// create an instance of Account
			// connectionSource.close();
			return accountDao;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
