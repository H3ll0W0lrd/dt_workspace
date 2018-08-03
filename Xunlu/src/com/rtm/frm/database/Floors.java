package com.rtm.frm.database;

import android.net.Uri;
import android.provider.BaseColumns;

public interface Floors extends BaseColumns {
	/*floor table and column*/
	public static final String TABLE_NAME = "floors";
	
	public static final String ID = BaseColumns._ID;
	
	public static final String BUILD_ID = Builds.BUILD_ID;
	
	public static final String BUILD_NAME = Builds.NAME;

	public static final String FLOOR = "floor";
	
	public static final String DESCRIPTION = "description";
	
	public static final String DESCRIPTION_1= "description_1";
	
	public static final String DESCRIPTION_ = "description_";
	
	public static final String WIDTH = "width";
	
	public static final String HEIGHT = "height";
	
	public static final String LEVEL_TITLE = "level_tile";
	
	public static final String IS_PRIVATE = "isPrivate";
	
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ XunluProvider.AUTHORITY + "/" + TABLE_NAME);
	
	static final String[] TABLE_COLUMNS = {
		ID + " INTEGER PRIMARY KEY",
		BUILD_ID + " TEXT",
		BUILD_NAME + " TEXT",
		FLOOR + " TEXT",
		DESCRIPTION + " TEXT",
		DESCRIPTION_1 + " TEXT",
		DESCRIPTION_ + " TEXT",
		WIDTH + " TEXT",
		HEIGHT + " TEXT",
		LEVEL_TITLE + " TEXT",
		IS_PRIVATE + " INTEGER",
	};
	
	/* default */static final String[] TABLE_INDEXES = {
		// NO Index.
	};
}
