package com.rtmap.driver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.rtmap.driver.App;

public class PreferencesUtil {
	private static final SharedPreferences SHARED_PREFERENCES;
	private static final String DRIVER_PRE_DIR = "driver_pre_dir";


	static {
		SHARED_PREFERENCES = App.getInstance().getSharedPreferences(DRIVER_PRE_DIR, Context.MODE_PRIVATE);
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

	public static void putObj(String key, Object object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			// 将对象的转为base64码
			// String objBase64 = new String(Base64.encodeBase64(baos
			// .toByteArray()));
			String objBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
			SHARED_PREFERENCES.edit().putString(key, objBase64).commit();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Object getObj(String key) {
		String objBase64 = SHARED_PREFERENCES.getString(key, null);
		if (TextUtils.isEmpty(objBase64))
			return null;

		// 对Base64格式的字符串进行解码
		byte[] base64Bytes = Base64.decode(objBase64.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);

		ObjectInputStream ois;
		Object obj = null;
		try {
			ois = new ObjectInputStream(bais);
			obj = (Object) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
}
