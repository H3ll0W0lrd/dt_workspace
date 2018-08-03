package com.rtm.frm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.rtm.frm.XunluApplication;

public class PreferencesUtil {
	private static final SharedPreferences SHARED_PREFERENCES;

	static {
		SHARED_PREFERENCES = XunluApplication.mApp.getSharedPreferences(
				ConstantsUtil.PREF_RTMAP, Context.MODE_PRIVATE);
	}

	public static boolean putBoolean(String key, boolean value) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.edit().putBoolean(key, value).commit();
	}

	public static boolean putInt(String key, int value) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.edit().putInt(key, value).commit();
	}

	public static boolean putFloat(String key, float value) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.edit().putFloat(key, value).commit();
	}

	public static boolean putString(String key, String value) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.edit().putString(key, value).commit();
	}

	public static boolean putLong(String key, long value) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.edit().putLong(key, value).commit();
	}

	public static boolean getBoolean(String key) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.getBoolean(key, false);
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		if (SHARED_PREFERENCES == null) {
			return false;
		}

		return SHARED_PREFERENCES.getBoolean(key, defaultValue);
	}

	public static int getInt(String key, int defaultValue) {
		if (SHARED_PREFERENCES == null) {
			return -1;
		}

		return SHARED_PREFERENCES.getInt(key, defaultValue);
	}

	public static float getFloat(String key) {
		if (SHARED_PREFERENCES == null) {
			return -1;
		}

		return SHARED_PREFERENCES.getFloat(key, -1);
	}

	public static String getString(String key) {
		if (SHARED_PREFERENCES == null) {
			return null;
		}

		return SHARED_PREFERENCES.getString(key, null);
	}

	public static long getLong(String key) {
		if (SHARED_PREFERENCES == null) {
			return -1;
		}

		return SHARED_PREFERENCES.getLong(key, -1);
	}

	public static String getString(String key, String defaultStr) {
		if (SHARED_PREFERENCES == null) {
			return null;
		}
		return SHARED_PREFERENCES.getString(key, defaultStr);
	}
}
