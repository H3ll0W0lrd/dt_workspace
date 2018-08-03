package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.frm.map.XunluMap;

/**
 * 某一建筑物下搜索获取联想列表
 * 
 * @author dingtao
 *
 */
public class RMSearchAssoicationUtil {

	public final static String SEARCHPOI = "RMSearchAssoicationUtil.searchPoi";

	/**
	 * 必填项
	 */
	private String key;
	private String buildid;// 建筑物ID
	private String keywords;// 关键字
	private OnSearchPoiListener onSearchPoiListener;

	/**
	 * 构造方法
	 */
	public RMSearchAssoicationUtil() {
		key = XunluMap.getInstance().getApiKey();
	}

	/**
	 * 设置搜索POI结果回调接口
	 * 
	 * @param onSearchAssoicationListener
	 *            POI结果回调接口
	 * @return 返回本类对象
	 */
	public RMSearchAssoicationUtil setOnSearchPoiListener(
			OnSearchPoiListener onSearchPoiListener) {
		this.onSearchPoiListener = onSearchPoiListener;
		return this;
	}

	/**
	 * 得到key
	 * 
	 * @return 智慧图LBS平台key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 设置智慧图key，如果地图或定位已经初始化，那么构造方法中会读取配置文件中的key
	 * 
	 * @param key
	 *            智慧图LBS平台key
	 * @return 本类对象
	 */
	public RMSearchAssoicationUtil setKey(String api_key) {
		this.key = api_key;
		return this;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return 建筑物ID
	 */
	public String getBuildid() {
		return buildid;
	}

	/**
	 * 设置建筑物ID
	 * 
	 * @param buildid
	 *            建筑物ID
	 * @return 本类对象
	 */
	public RMSearchAssoicationUtil setBuildid(String buildid) {
		this.buildid = buildid;
		return this;
	}

	/**
	 * 得到关键字
	 * 
	 * @return 关键字
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * 设置关键字
	 * 
	 * @param keywords
	 *            关键字
	 * @return 本类对象
	 */
	public RMSearchAssoicationUtil setKeywords(String keywords) {
		this.keywords = keywords;
		return this;
	}

	/**
	 * 发起搜索请求，搜索POI
	 */
	public void searchPoi() {
		new RMAsyncTask(new CheckCall()).run();
	}

	private class CheckCall implements RMCallBack {
		@Override
		public Object onCallBackStart(Object... obj) {
			RMPois route = new RMPois();
			route.setInterfaceName(SEARCHPOI);
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.SEARCH_ASSOICATION,
						new String[] { "key", "buildid", "keywords" },
						new String[] { key, buildid, keywords });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						ArrayList<POI> list = new ArrayList<POI>();
						if (resultobj.has("poilist")) {
							JSONArray array = resultobj.getJSONArray("poilist");
							for (int i = 0; i < array.length(); i++) {
								POI point = new POI();
								JSONObject pointobj = array.getJSONObject(i);
								point.setFloor(pointobj.getString("floor"));
								point.setX(Float.parseFloat(pointobj
										.getString("x")));
								point.setY(Float.parseFloat(pointobj
										.getString("y")));
								point.setBuildId(pointobj.getString("buildid"));
								point.setPoiNo(Integer.parseInt(pointobj
										.getString("poi_no")));
								if (pointobj.has("name")) {
									point.setName(pointobj.getString("name"));
								}
								point.setClassid(pointobj.getString("classid"));
								if (pointobj.has("classname")) {
									point.setClassname(pointobj
											.getString("classname"));
								}
								list.add(point);
							}
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
