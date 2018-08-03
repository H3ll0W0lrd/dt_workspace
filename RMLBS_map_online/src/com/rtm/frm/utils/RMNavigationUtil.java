package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.POI;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMRoute;

/**
 * 获取POI点详情
 * 
 * @author dingtao
 *
 */
public class RMNavigationUtil {
	/**
	 * 请求路线规划数据
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param buildId
	 *            建筑物ID
	 * @param startPoint
	 *            开始点，以POI类型定义开始点是为了方便地图点击后，直接将POI直接请求导航，减少封装处理，实际上如果自定义某个点使用导航，
	 *            只用给POI填写3个属性即可，分别是x,y,floor
	 * @param endPoint
	 *            结束点，以POI类型定义结束点是为了方便地图点击后，直接将POI直接请求导航，减少封装处理，实际上如果自定义某个点使用导航，
	 *            只用给POI填写3个属性即可，分别是x,y,floor
	 * @param routePointList
	 *            路线经过的点的集合，以POI类型定义中间点是为了方便地图点击后，直接将POI直接请求导航，减少封装处理，
	 *            实际上如果自定义某个点使用导航， 只用给POI填写3个属性即可 ，分别是x,y,floor
	 * @param isNeedName
	 *            是否给每个导航点设置附近的POI名字，方便你在导航点周围查找相关店铺
	 * @param listener
	 *            路线规划结果回调方法
	 */
	public static void requestNavigation(String api_key, String buildId,
			POI startPoint, POI endPoint, ArrayList<POI> routePointList,
			boolean isNeedName, OnNavigationListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildId, startPoint,
				endPoint, routePointList, isNeedName)).run();
	}

	/**
	 * 请求路线规划数据
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param buildId
	 *            建筑物ID
	 * @param startPoint
	 *            开始点，以POI类型定义开始点是为了方便地图点击后，直接将POI直接请求导航，减少封装处理，实际上如果自定义某个点使用导航，
	 *            只用给POI填写3个属性即可，分别是x,y,floor
	 * @param endPoint
	 *            结束点，以POI类型定义结束点是为了方便地图点击后，直接将POI直接请求导航，减少封装处理，实际上如果自定义某个点使用导航，
	 *            只用给POI填写3个属性即可，分别是x,y,floor
	 * @param routePointList
	 *            路线经过的点的集合，以POI类型定义中间点是为了方便地图点击后，直接将POI直接请求导航，减少封装处理，
	 *            实际上如果自定义某个点使用导航， 只用给POI填写3个属性即可 ，分别是x,y,floor
	 * @param listener
	 *            路线规划结果回调接口
	 */
	@Deprecated
	public static void requestNavigation(String api_key, String buildId,
			POI startPoint, POI endPoint, ArrayList<POI> routePointList,
			OnNavigationListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildId, startPoint,
				endPoint, routePointList, false)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnNavigationListener listener;
		String key;
		String buildId;
		POI startPoint;
		POI endPoint;
		ArrayList<POI> routePointList;
		boolean isNeedName;

		public CheckCall(OnNavigationListener listener, String key,
				String buildId, POI startPoint, POI endPoint,
				ArrayList<POI> routePointList, boolean isNeedName) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
			this.startPoint = startPoint;
			this.endPoint = endPoint;
			this.routePointList = routePointList;
			this.isNeedName = isNeedName;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMRoute route = new RMRoute();
			try {
				JSONObject value = new JSONObject();
				value.put("key", key);
				value.put("buildid", buildId);
				JSONObject startObj = new JSONObject();
				startObj.put("x", startPoint.getX() + "");
				startObj.put("y", "" + startPoint.getY());
				startObj.put("floor", startPoint.getFloor());
				JSONObject endObj = new JSONObject();
				endObj.put("x", "" + endPoint.getX());
				endObj.put("y", "" + endPoint.getY());
				endObj.put("floor", endPoint.getFloor());
				JSONArray pointlist = new JSONArray();
				pointlist.put(startObj);
				pointlist.put(endObj);
				if (routePointList != null && routePointList.size() > 0) {
					JSONArray route_pointlist = new JSONArray();
					for (POI point : routePointList) {
						JSONObject pointobj = new JSONObject();
						pointobj.put("x", "" + point.getX());
						pointobj.put("y", "" + point.getY());
						pointobj.put("floor", point.getFloor());
						route_pointlist.put(pointobj);
					}
					value.put("route_pointlist", route_pointlist);
				}
				value.put("need_name", isNeedName);
				value.put("pointlist", pointlist);
				String result = RMHttpUtil.postConnection(
						RMHttpUrl.getWEB_URL() + RMHttpUrl.NAVIGATION,
						value.toString());
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						route.setDistance(Integer.parseInt(resultobj
								.getString("distance")));
						JSONArray array = resultobj.getJSONArray("pointlist");
						ArrayList<NavigatePoint> list = new ArrayList<NavigatePoint>();
						for (int i = 0; i < array.length(); i++) {
							NavigatePoint point = new NavigatePoint();
							JSONObject pointobj = array.getJSONObject(i);
							point.setFloor(pointobj.getString("floor"));
							int x = Integer.parseInt(pointobj.getString("x"));
							int y = Integer.parseInt(pointobj.getString("y"));
							point.setX(x / 1000.0f);
							point.setY(y / 1000.0f);
							point.setBuildId(this.buildId);
							if (pointobj.has("distance")) {
								point.setDistance(Integer.parseInt(pointobj
										.getString("distance")));
							}
							if (pointobj.has("poi_name")) {
								point.setAroundPoiName(pointobj
										.getString("poi_name"));
							}
							if (pointobj.has("desc")) {
								point.setDesc(pointobj.getString("desc"));
							}
							if (pointobj.has("action")) {
								point.setAction(Integer.parseInt(pointobj
										.getString("action")));
							}
							if (pointobj.has("important")) {
								point.setImportant(Boolean
										.parseBoolean(pointobj
												.getString("important")));
							}
							list.add(point);
						}
						route.setPointlist(list);
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
				listener.onFinished((RMRoute) obj);
		}
	}

	/**
	 * 路线规划回调接口
	 * 
	 * @author dingtao
	 */
	public interface OnNavigationListener {
		/**
		 * 路线规划回调方法
		 * 
		 * @param route
		 *            具体使用请查看RMRoute
		 */
		public void onFinished(RMRoute route);
	}

}
