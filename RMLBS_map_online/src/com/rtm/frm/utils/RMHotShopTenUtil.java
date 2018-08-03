package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.frm.map.XunluMap;

/**
 * 获取热门店铺列表TOP10
 * 
 * @author dingtao
 *
 */
public class RMHotShopTenUtil {
	/**
	 * 必填项
	 */
	private String key;
	private String buildid;// 建筑物ID
	private String city;// 城市
	private OnSearchShopListener onSearchShopListener;

	/**
	 * 构造方法
	 */
	public RMHotShopTenUtil() {
		key = XunluMap.getInstance().getApiKey();
	}

	/**
	 * 设置结果回调接口
	 * 
	 * @param onSearchShopListener
	 *            回调接口参数
	 * @return 本类对象
	 */
	public RMHotShopTenUtil setOnSearchShopListener(
			OnSearchShopListener onSearchShopListener) {
		this.onSearchShopListener = onSearchShopListener;
		return this;
	}

	/**
	 * 得到智慧图LBS平台key
	 * 
	 * @return 智慧图LBS平台key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 设置智慧图key，如果地图或定位已经初始化，那么构造方法中会读取配置文件中的key 必须设置
	 * 
	 * @param key
	 *            智慧图LBS平台key
	 * @return 本类对象
	 */
	public RMHotShopTenUtil setKey(String api_key) {
		this.key = api_key;
		return this;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return 建筑物ID
	 * 
	 */
	public String getBuildid() {
		return buildid;
	}

	/**
	 * 设置建筑物ID 非必须设置
	 * 
	 * @param buildid
	 *            建筑物ID
	 * @return 本类对象
	 */
	public RMHotShopTenUtil setBuildid(String buildid) {
		this.buildid = buildid;
		return this;
	}

	/**
	 * 得到城市
	 * 
	 * @return 城市名称
	 */
	public String getCity() {
		return city;
	}

	/**
	 * 城市，非必要设置
	 * 
	 * @param city
	 *            城市名称
	 * @return 本类对象
	 */
	public RMHotShopTenUtil setCity(String city) {
		this.city = city;
		return this;
	}

	/**
	 * 店铺列表TOP10
	 * 
	 */
	public void searchShopTen() {
		new RMAsyncTask(new CheckCall()).run();
	}

	private class CheckCall implements RMCallBack {
		@Override
		public Object onCallBackStart(Object... obj) {
			RMPois route = new RMPois();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.SEARCH_POI_CITY,
						new String[] { "key", "buildid", "city" },
						new String[] { key, buildid, city });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONArray array = resultobj.getJSONArray("poilist");
						ArrayList<POI> list = new ArrayList<POI>();
						for (int i = 0; i < array.length(); i++) {
							POI point = new POI();
							JSONObject pointobj = array.getJSONObject(i);
							point.setFloor(pointobj.getString("floor"));
							if (pointobj.has("x")) {
								point.setX(Float.parseFloat(pointobj
										.getString("x")));
							}
							if (pointobj.has("y")) {
								point.setY(Float.parseFloat(pointobj
										.getString("y")));
							}
							point.setBuildId(pointobj.getString("buildid"));
							point.setPoiNo(Integer.parseInt(pointobj
									.getString("poi_no")));
							if (pointobj.has("name")) {
								point.setName(pointobj.getString("name"));
							}
							if (pointobj.has("classid")) {
								point.setClassid(pointobj.getString("classid"));
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
			if (onSearchShopListener != null)
				onSearchShopListener.onFinished((RMPois) obj);
		}
	}

	/**
	 * 热门店铺列表回调
	 * 
	 * @author dingtao
	 */
	public interface OnSearchShopListener {
		/**
		 * 热门店铺结果回调方法
		 * 
		 * @param result
		 *            具体使用请查看RMPois
		 */
		public void onFinished(RMPois result);
	}
}
