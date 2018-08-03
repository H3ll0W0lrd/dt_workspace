package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.RMCityList;

/**
 * 获取城市列表
 * 
 * @author dingtao
 *
 */
public class RMCityListUtil {
	/**
	 * 获取城市列表
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param listener
	 *            结果回调
	 */
	public static void requestCityList(String api_key,
			OnGetCityListListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetCityListListener listener;
		String key;

		public CheckCall(OnGetCityListListener listener, String key) {
			this.listener = listener;
			this.key = key;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMCityList route = new RMCityList();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.CITY_LIST,
						new String[] { "key" }, new String[] { key });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONArray buildobj = resultobj.getJSONArray("city");
						ArrayList<String> citylist = new ArrayList<String>();
						for (int i = 0; i < buildobj.length(); i++) {
							citylist.add(buildobj.getString(i));
						}
						route.setCitylist(citylist);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return route;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (listener != null)
				listener.onFinished((RMCityList) obj);
		}
	}

	/**
	 * 获取服务范围内的城市列表回调
	 * 
	 * @author dingtao
	 */
	public interface OnGetCityListListener {
		/**
		 * 城市列表回调方法
		 * @param result
		 *            城市列表 具体使用请查看RMCityList
		 */
		public void onFinished(RMCityList result);
	}

}
