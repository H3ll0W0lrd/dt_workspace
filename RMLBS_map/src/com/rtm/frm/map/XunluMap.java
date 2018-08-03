package com.rtm.frm.map;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.RMLicense;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMLicenseUtil;
import com.rtm.frm.utils.RMLicenseUtil.OnValidateFinishedListener;

public class XunluMap {
	private static XunluMap instance;

	private Context mContext;
	private String mApiKey;
	private Lock lock;

	public String getApiKey() {
		return mApiKey;
	}

	/**
	 * 更改服务器地址
	 * 
	 * @param url
	 */
	public void setServerUrl(String url) {
		RMHttpUrl.setWEB_URL(url);
	}

	/**
	 * 设置地图主文件名，默认是rtmap
	 * 
	 * @param folderName
	 *            文件名即可，例"rtmap",或者"rtmap/map"这样的方式也都OK
	 */
	public boolean setRootFolder(String folderName) {
		if (folderName != null && !folderName.equals("")) {
			RMFileUtil.MAP_FILEROOT = folderName;
			return true;
		}
		return false;
	}

	/**
	 * 设置api_key
	 * 
	 * @param mLicenseKey
	 */
	public void setApiKey(String mLicenseKey) {
		this.mApiKey = mLicenseKey;
		if (mContext != null)
			RMLicenseUtil.validate(mApiKey, mContext.getPackageName(),
					RMLicenseUtil.MAP, new OnValidateFinishedListener() {

						@Override
						public void onFinished(RMLicense result) {
							Handlerlist.getInstance().notifications(
									Constants.RTMAP_MAP,
									Constants.MAP_LICENSE,
									"error_code:" + result.getError_code()
											+ "++msg:" + result.getError_msg());
							if (result.getError_code() != 0
									&& result.getError_code() != -1)
								RMHttpUrl.IS_LIS_PASS = false;
							else
								RMHttpUrl.IS_LIS_PASS = true;
						}
					});
	}

	/**
	 * 获取实例，请初始化init
	 * 
	 * @return
	 */
	public static XunluMap getInstance() {
		if (instance == null) {
			instance = new XunluMap();
		}

		return instance;
	}

	private XunluMap() {
		lock = new ReentrantLock();
	}
	
	/**
	 * 得到地图同步锁
	 * @return 用于运算的同步锁
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * 地图工具初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		setContext(context);
		String key = RMConfig.getMetaData(context, RMFileUtil.RTMAP_KEY);
		if (!RMStringUtils.isEmpty(key)) {
			mApiKey = key;
			RMLicenseUtil.validate(mApiKey, context.getPackageName(),
					RMLicenseUtil.MAP, new OnValidateFinishedListener() {

						@Override
						public void onFinished(RMLicense result) {
							Handlerlist.getInstance().notifications(
									Constants.RTMAP_MAP,
									Constants.MAP_LICENSE,
									"error_code:" + result.getError_code()
											+ "msg:" + result.getError_msg());
							if (result.getError_code() != 0
									&& result.getError_code() != -1)
								RMHttpUrl.IS_LIS_PASS = false;
							else
								RMHttpUrl.IS_LIS_PASS = true;
						}
					});
		}
	}

	private void setContext(Context context) {
		mContext = context;
	}

	/**
	 * 得到Context对象
	 * 
	 * @return
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * 验证api_key接口
	 * 
	 * @param listener
	 */
	public void validateLicense(OnValidateFinishedListener listener) {
		RMLicenseUtil.validate(mApiKey, mContext.getPackageName(),
				RMLicenseUtil.MAP, listener);
	}
}
