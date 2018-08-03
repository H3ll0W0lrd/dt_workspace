package com.rtm.frm.database;

import android.net.Uri;
import android.provider.BaseColumns;

public interface PrivateBuilds extends BaseColumns {
	/*build table and column*/
	public static final String TABLE_NAME = "privateBuilds";
	
	public static final String ID = BaseColumns._ID;
	
	public static final String CITY_NAME = "cityName";
	
	public static final String BUILD_ID = "buildId";
	
	public static final String NAME = "buildName";
	
	public static final String SIZE = "size";

	public static final String NAME_JP2 = "nameJp2";
	
	public static final String LAT = "lat";
	
	public static final String LNG = "long";
	
	public static final String FLOORS = "floors";
	
	public static final String VERSION_DATA = "versionData";
	
	public static final String VERSION_MAP = "versionMap";
	
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ XunluProvider.AUTHORITY + "/" + TABLE_NAME);
	
	static final String[] TABLE_COLUMNS = {
		ID + " INTEGER PRIMARY KEY",
		CITY_NAME + " TEXT",
		BUILD_ID + " TEXT",
		NAME + " TEXT",
		SIZE + " TEXT",
		NAME_JP2 + " TEXT",
		LAT + " TEXT",
		LNG + " TEXT",
		FLOORS + " TEXT",
		VERSION_DATA + " TEXT",
		VERSION_MAP + " TEXT",
	};
	
	/* default */static final String[] TABLE_INDEXES = {
		// NO Index.
	};
}