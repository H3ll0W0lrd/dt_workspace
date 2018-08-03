package com.rtm.frm.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.RMLicense;

/**
 * License验证类
 * 
 * @author dingtao
 *
 */
public class RMLicenseUtil {

	/**
	 * 验证类型：定位
	 */
	public final static String LOCATION = "1";
	/**
	 * 验证类型：地图
	 */
	public final static String MAP = "2";

	/**
	 * 验证
	 * @param api_key 智慧图LBS平台key
	 * @param packages 应用的包名
	 * @param type 验证类型，LOCATION或者MAP
	 * @param listener 回调
	 */
	public static void validate(String api_key, String packages, String type,
			OnValidateFinishedListener listener) {
		new RMAsyncTask(new CheckCall(listener)).run(api_key, packages, type);
	}

	private static class CheckCall implements RMCallBack {
		OnValidateFinishedListener listener;

		public CheckCall(OnValidateFinishedListener listener) {
			this.listener = listener;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMLicense license = new RMLicense();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.LICENSE, new String[] { "key", "packages",
								"type","app_version","app_os" }, new String[] { (String) obj[0],
								(String) obj[1], (String) obj[2],RMVersionMap.VERSION, "Android"});
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					license.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					license.setError_msg(errorobj.getString("error_msg"));
					if (license.getError_code() == 0) {
						JSONObject lisobj = resultobj.getJSONObject("license");
						license.setTime_limit(lisobj.getString("time_limit"));
						license.setAuth_key(lisobj.getString("auth_key"));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return license;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (listener != null)
				listener.onFinished((RMLicense) obj);
		}
	}

	/**
	 * License验证结果回调
	 * @author dingtao
	 */
	public interface OnValidateFinishedListener {
		/**
		 * 验证结果回调方法
		 * @param result 具体使用请查看RMLicense
		 */
		public void onFinished(RMLicense result);
	}
}
