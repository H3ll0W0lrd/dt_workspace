package com.rtm.frm.database;

import android.net.Uri;
import android.provider.BaseColumns;

public interface FavorablePois  extends BaseColumns {
		/*build table and column*/
		public static final String TABLE_NAME = "favorablePois";
		
		public static final String ID = BaseColumns._ID;
		
		public static final String CITY_NAME = "cityName";
		
		public static final String BUILD_ID = "buildId";
		
		public static final String FLOOR = "floor";

		public static final String POI_X = "poiX";
		
		public static final String POI_Y = "poiY";
		
		public static final String POI_ID = "poiId";
		
		public static final String CATEGORY_CODE = "categoryCode";
		
		public static final String POI_NAME = "poiName";
		
		public static final String AD_LEVEL = "adLevel";
		
		public static final String DESCRIPTION = "discription";
		
		public static final String START_TIME = "startTime";
		
		public static final String END_TIME = "endTime";
		
		public static final String AD_URL = "adUrl";
		
		public static final String AD_BIG_URL = "adBigUrl";
		
		public static final String POI_NO = "poiNo";
		
		public static final String POI_NO_CARD_PAY = "noCardPay";
		
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ XunluProvider.AUTHORITY + "/" + TABLE_NAME);
		
		static final String[] TABLE_COLUMNS = {
			ID + " INTEGER PRIMARY KEY",
			CITY_NAME + " TEXT",
			BUILD_ID + " TEXT",
			FLOOR + " TEXT",
			POI_X + " TEXT",
			POI_Y + " TEXT",
			POI_ID + " TEXT",
			AD_URL + " TEXT",
			AD_BIG_URL + " TEXT",
			AD_LEVEL + " TEXT",
			CATEGORY_CODE + " TEXT",
			POI_NAME + " TEXT",
			DESCRIPTION + " TEXT",
			START_TIME + " TEXT",
			END_TIME + " INTEGER",
			POI_NO + " TEXT",
			POI_NO_CARD_PAY + " TEXT"
		};
		
		/* default */static final String[] TABLE_INDEXES = {
			// NO Index.
		};
	}
