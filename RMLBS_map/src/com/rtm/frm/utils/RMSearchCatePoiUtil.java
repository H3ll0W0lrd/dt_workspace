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
import com.rtm.frm.map.XunluMap;

/**
 * 某一建筑物内的某一分类的所有店铺
 * 
 * @author dingtao
 *
 */
public class RMSearchCatePoiUtil {

	public final static String SEARCHPOI = "RMSearchCatePoiUtil.searchPoi";
	/**
	 * 必填项
	 */
	private String key;
	private String buildid;// 建筑物ID
	private ArrayList<String> classid;

	/**
	 * 非必要选项
	 */
	private String floor;
	private int pagesize;
	private int pageindex;
	private OnSearchPoiListener onSearchPoiListener;

	/**
	 * 构造方法
	 */
	public RMSearchCatePoiUtil() {
		key = XunluMap.getInstance().getApiKey();
	}

	/**
	 * 设置分类结果回调接口
	 * 
	 * @param onSearchCateListener
	 *            分类结果回调接口
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setOnSearchPoiListener(
			OnSearchPoiListener onSearchPoiListener) {
		this.onSearchPoiListener = onSearchPoiListener;
		return this;
	}

	/**
	 * 得到楼层，例：F1
	 * 
	 * @return 楼层
	 */
	public String getFloor() {
		return floor;
	}

	/**
	 * 设置楼层，例：楼层（B1,F1,F1.5） 非必要项
	 * 
	 * @param floor
	 *            楼层，例：F1
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setFloor(String floor) {
		this.floor = floor;
		return this;
	}

	/**
	 * 得到分类id组合
	 * 
	 * @return 分类id组合
	 */
	public ArrayList<String> getClassid() {
		return classid;
	}

	/**
	 * POI分类编号数组 必填项
	 * 
	 * @param classid
	 *            分类编号数组
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setClassid(ArrayList<String> classid) {
		this.classid = classid;
		return this;
	}

	/**
	 * 得到每页数据量长度
	 * 
	 * @return
	 */
	public int getPagesize() {
		return pagesize;
	}

	/**
	 * 设置每页返回数据数量 非必要项
	 * 
	 * @param pagesize
	 *            数据条数
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setPagesize(int pagesize) {
		this.pagesize = pagesize;
		return this;
	}

	/**
	 * 得到页码，例：第5页
	 * 
	 * @return 页码
	 */
	public int getPageindex() {
		return pageindex;
	}

	/**
	 * 设置页码 非必要项
	 * 
	 * @param pageindex
	 *            页码
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setPageindex(int pageindex) {
		this.pageindex = pageindex;
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
	 * 设置智慧图key，如果地图或定位已经初始化，那么构造方法中会读取配置文件中的key 必要项
	 * 
	 * @param key
	 *            智慧图LBS平台key
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setKey(String api_key) {
		this.key = api_key;
		return this;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return 建筑物id
	 */
	public String getBuildid() {
		return buildid;
	}

	/**
	 * 设置建筑物ID 必要项
	 * 
	 * @param buildid
	 *            建筑物id
	 * @return 本类对象
	 */
	public RMSearchCatePoiUtil setBuildid(String buildid) {
		this.buildid = buildid;
		return this;
	}

	/**
	 * 获取某一建筑物内的分类的POI列表
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
				JSONObject value = new JSONObject();
				value.put("key", key);
				value.put("buildid", buildid);
				if (floor != null)
					value.put("floor", floor);
				if (pagesize != 0)
					value.put("pagesize", pagesize);
				if (pageindex != 0)
					value.put("pageindex", pageindex);
				if (classid != null && classid.size() > 0) {
					JSONArray classArray = new JSONArray(classid);
					value.put("classid", classArray);
				}

				String result = RMHttpUtil.postConnection(
						RMHttpUrl.getWEB_URL() + RMHttpUrl.SEARCH_POI_CATE,
						value.toString());
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
