package com.rtm.location.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;

/**
 * 搜索定位点附近的POI列表
 * 
 * @author dingtao
 *
 */
public class RMSearchLbsPoiUtil {

	public final static String SEARCHLBSPOI = "RMSearchLbsPoiUtil.searchLbsPoi";

	/**
	 * 搜索定位点附近的POI，如果传入location，那么则返回离此位置最近的3个POI，如果不传入，则自动采用定位位置
	 * 
	 * @param key
	 *            智慧图LBS开放平台key
	 * @param location
	 *            定位点位置
	 * @param listener
	 *            搜索结果回调接口
	 */
	public static void searchLbsPoi(String key, RMLocation location,
			OnSearchPoiListener onSearchPoiListener) {
		new RMAsyncTask(new CheckCall(key, location, onSearchPoiListener))
				.run();
	}

	private static class CheckCall implements RMCallBack {

		/**
		 * 必填项
		 */
		private String key;
		private RMLocation location;// 建筑物ID
		private OnSearchPoiListener onSearchPoiListener;

		public CheckCall(String key, RMLocation location,
				OnSearchPoiListener onSearchPoiListener) {
			this.key = key;
			this.location = location;
			this.onSearchPoiListener = onSearchPoiListener;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMPois route = new RMPois();
			route.setInterfaceName(SEARCHLBSPOI);
			try {
				String result = RMHttpUtil.connInfo(
						RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.LBS_POI,
						new String[] { "key", "buildid", "floor", "x", "y" },
						new String[] { key, location.getBuildID(),
								location.getFloor(), location.getX() + "",
								location.getY() + "" });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONArray array = resultobj.getJSONArray("poiinfo");
						ArrayList<POI> list = new ArrayList<POI>();
						for (int i = 0; i < array.length(); i++) {
							POI point = new POI();
							JSONObject pointobj = array.getJSONObject(i);
							point.setFloor(pointobj.getString("floor"));
							point.setPoiNo(Integer.parseInt(pointobj
									.getString("poi_id")));
							if (pointobj.has("poi_name")) {
								point.setName(pointobj.getString("poi_name"));
							}
							if (pointobj.has("build_name")) {
								point.setBuildName(pointobj
										.getString("build_name"));
							}
							if (pointobj.has("is_inside")) {
								point.setIsInside(pointobj
										.getString("is_inside"));
							}
							if (pointobj.has("classname")) {
								point.setClassname(pointobj
										.getString("classname"));
							}
							list.add(point);
						}
						route.setPoilist(list);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return route;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (onSearchPoiListener != null)
				onSearchPoiListener.onSearchPoi((RMPois) obj);
		}
	}
}
