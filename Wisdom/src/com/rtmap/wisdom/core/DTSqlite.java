package com.rtmap.wisdom.core;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import com.rtmap.wisdom.model.MyBuild;
import com.rtmap.wisdom.util.DTFileUtil;

public class DTSqlite {

	private static DTSqlite instance;

	public static DTSqlite getInstance() {
		if (instance == null) {
			instance = new DTSqlite();
		}
		return instance;
	}

	private DTSqlite() {
	}

	/**
	 * DAO对象的使用参见OrmLite的api
	 */
	public Dao<MyBuild, String> createBuildTable() {
		try {

			// 使用SQLiteDatabase创建或者打开sd或者assets下的db文件
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
					DTFileUtil.getDataDir() + "wisdom.db",
					null);
			// ORMLite的android.jar封装SQLiteDatabase
			AndroidConnectionSource connectionSource = new AndroidConnectionSource(
					db);

			/**
			 * 以下为官方首页快速使用方式，没有任何改动的照搬，由于需要ConnectionSource对象，
			 * 通过查看API发现子类AndroidConnectionSource
			 */

			// instantiate the DAO to handle Account with String id
			Dao<MyBuild, String> accountDao = DaoManager.createDao(
					connectionSource, MyBuild.class);

			// if you need to create the 'accounts' table make this call
			TableUtils.createTableIfNotExists(connectionSource,
					MyBuild.class);

			// create an instance of Account
			// connectionSource.close();
			return accountDao;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
