package com.rtmap.driver.cache;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

/**
 * 
 * 
 * project_name：RtmapCustomer description： 缓存图片 author：fushenghua date：2015-3-17
 * 上午10:10:55
 * 
 * @version
 * 
 */
public class ImageCache {
	public static final String IMAGE_NAME = "/driver/passenger_pic/";
	public static final String IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGE_NAME;

	private static final int DIS_CACHE_SIZE = 1024 * 1024 * 1024; // 10MB
	// sd卡缓存
	private DiskLruCache diskLruCache;
	private Context context;

	public ImageCache(Context context) {
		this.context = context;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			diskLruCache = DiskLruCache.openCache(context, new File(IMAGE_PATH), DIS_CACHE_SIZE);
		}
	}

	/**
	 * 得到图片
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap get(Object key) {
		Bitmap bitmap = null;
		if (diskLruCache != null) {
			bitmap = diskLruCache.get(key.toString());
		}
		return bitmap;
	}

	/**
	 * 缓存到sd卡
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void putDiskCache(Object key, Bitmap bitmap) {
		if (diskLruCache != null) {
			diskLruCache.put(key.toString(), bitmap);
		}
	}

}
