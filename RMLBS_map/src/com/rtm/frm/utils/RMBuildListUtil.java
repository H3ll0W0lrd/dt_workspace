package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.CityInfo;
import com.rtm.frm.model.RMBuildList;

/**
 * 获取建筑物列表
 * 
 * @author dingtao
 *
 */
public class RMBuildListUtil {
	/**
	 * 获取建筑物列表
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param city
	 *            城市，当为null时，返回所有城市的所有建筑物
	 * @param listener
	 *            结果回调
	 */
	public static void requestBuildList(String api_key, String city,
			OnGetBuildListListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, city)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetBuildListListener listener;
		String key;
		String city;

		public CheckCall(OnGetBuildListListener listener, String key,
				String city) {
			this.listener = listener;
			this.key = key;
			this.city = city;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMBuildList route = new RMBuildList();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.BUILD_LIST,
						new String[] { "key", "city" }, new String[] { key,
								city });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONArray cityarray = resultobj
								.getJSONArray("buildlist");
						ArrayList<CityInfo> list = new ArrayList<CityInfo>();
						for (int j = 0; j < cityarray.length(); j++) {
							CityInfo city = new CityInfo();
							city.setCity(cityarray.getJSONObject(j).getString(
									"city"));
							JSONArray array = cityarray.getJSONObject(j)
									.getJSONArray("buildinfo");
							ArrayList<BuildInfo> buildlist = new ArrayList<BuildInfo>();
							for (int i = 0; i < array.length(); i++) {
								BuildInfo point = new BuildInfo();
								JSONObject pointobj = array.getJSONObject(i);
								point.setBuildId(pointobj.getString("buildid"));
								if (pointobj.has("name_chn")) {
									point.setBuildName(pointobj
											.getString("name_chn"));
								}
								if (pointobj.has("name_en")) {
									point.setName_en(pointobj
											.getString("name_en"));
								}
								if (pointobj.has("name_jp")) {
									point.setName_jp(pointobj
											.getString("name_jp"));
								}
								if (pointobj.has("name_qp")) {
									point.setName_qp(pointobj
											.getString("name_qp"));
								}
								if (pointobj.has("lat") && pointobj.has("long")) {
									Float lat = Float.parseFloat(pointobj
											.getString("lat"));
									Float lng = Float.parseFloat(pointobj
											.getString("long"));
									point.setLatLong(lat, lng);
								}
								buildlist.add(point);
							}
							city.setBuildlist(buildlist);
							list.add(city);
						}
						route.setCitylist(list);
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
				listener.onFinished((RMBuildList) obj);
		}
	}

	/**
	 * 获取服务范围内的建筑物列表回调
	 * 
	 * @author dingtao
	 */
	public interface OnGetBuildListListener {
		/**
		 * 建筑物列表回调方法
		 * @param result
		 *            具体使用请查看RMBuildList
		 */
		public void onFinished(RMBuildList result);
	}

}
