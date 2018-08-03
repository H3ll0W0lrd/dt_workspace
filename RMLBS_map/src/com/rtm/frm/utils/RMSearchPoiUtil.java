package com.rtm.frm.utils;

import java.io.File;
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
import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.vmap.Layer;
import com.rtm.frm.vmap.Shape;

/**
 * 在某一建筑物下关键字搜索POI信息
 * 
 * @author dingtao
 *
 */
public class RMSearchPoiUtil {

	public final static String SEARCHPOI = "RMSearchPoiUtil.searchPoi";
	public final static String SEARCHAROUNDPOILISTBYMAP = "RMSearchPoiUtil.searchAroundPoiListByMap";
	public final static String SEARCHPOIBYMAP = "RMSearchPoiUtil.searchPoiByMap";

	/**
	 * 必填项
	 */
	private String key;
	private String buildid;// 建筑物ID
	private String keywords;// 关键字
	private OnSearchPoiListener onSearchPoiListener;

	/**
	 * 非必要选项
	 */
	private String floor;
	private int pagesize;
	private int pageindex;

	/**
	 * 构造方法
	 */
	public RMSearchPoiUtil() {
		key = XunluMap.getInstance().getApiKey();
	}

	/**
	 * 设置POI信息回调接口，必须设置
	 * 
	 * @param onSearchPoiListener
	 *            POI结果回调接口
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setOnSearchPoiListener(
			OnSearchPoiListener onSearchPoiListener) {
		this.onSearchPoiListener = onSearchPoiListener;
		return this;
	}

	/**
	 * 得到楼层，例：F1
	 * 
	 * @return 楼层，例：F1
	 */
	public String getFloor() {
		return floor;
	}

	/**
	 * 设置楼层，例：楼层（B1,F1,F1.5）
	 * 
	 * @param floor
	 *            楼层，例：F1
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setFloor(String floor) {
		this.floor = floor;
		return this;
	}

	/**
	 * 设置楼层，例：楼层（20100）
	 * 
	 * @param floor
	 *            楼层，例：20100
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setFloor(int floor) {
		this.floor = RMStringUtils.floorTransform(floor);
		return this;
	}

	/**
	 * 得到每页数据数量
	 * 
	 * @return 数据条数
	 */
	public int getPagesize() {
		return pagesize;
	}

	/**
	 * 设置每页返回数据数量
	 * 
	 * @param pagesize
	 *            数据条数
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setPagesize(int pagesize) {
		this.pagesize = pagesize;
		return this;
	}

	/**
	 * 得到页码
	 * 
	 * @return 页码
	 */
	public int getPageindex() {
		return pageindex;
	}

	/**
	 * 设置页码
	 * 
	 * @param pageindex
	 *            页码
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setPageindex(int pageindex) {
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
	 * 设置智慧图LBS平台key，如果地图或定位已经初始化，那么构造方法中会读取配置文件中的key 必须设置
	 * 
	 * @param key
	 *            智慧图LBS平台key
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setKey(String api_key) {
		this.key = api_key;
		return this;
	}

	/**
	 * 得到建筑物id
	 * 
	 * @return 建筑物id
	 */
	public String getBuildid() {
		return buildid;
	}

	/**
	 * 设置建筑物ID 必须设置
	 * 
	 * @param buildid
	 *            建筑物id
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setBuildid(String buildid) {
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
	 * 设置关键字 必须设置
	 * 
	 * @param keywords
	 *            关键字
	 * @return 本类对象
	 */
	public RMSearchPoiUtil setKeywords(String keywords) {
		this.keywords = keywords;
		return this;
	}

	/**
	 * 获取POI列表
	 * 
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
						RMHttpUrl.getWEB_URL() + RMHttpUrl.SEARCH_KEYWORDS,
						new String[] { "key", "buildid", "keywords", "floor",
								"pagesize", "pageindex" }, new String[] { key,
								buildid, keywords, floor, pagesize + "",
								pageindex + "" });
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

	/**
	 * 通过本地缓存的地图文件，根据POI名称中的关键字查找本楼层中所有符合条件的POI
	 * 
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            楼层，例如：20100
	 * @param keywords
	 *            POI名称中的关键字，多个不同的关键字请用英文分号分开(;)，例如：①肯德基；②肯德基;服务台;耐克
	 * @param isFullNameSearch
	 *            是否全名查找，true：使用全名，即关键字为POI名字；false：则POI名字包含关键字便可以
	 * @param listener
	 *            结果回调接口OnSearchPoiListener
	 */
	public static void searchPoiByMap(final String buildId, final int floor,
			final String keywords, final boolean isFullNameSearch,
			final OnSearchPoiListener listener) {
		searchPoiByMap(buildId, RMStringUtils.floorTransform(floor), keywords,
				isFullNameSearch, listener);
	}

	/**
	 * 通过本地缓存的地图文件，根据POI名称中的关键字查找本楼层中所有符合条件的POI
	 * 
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            楼层，例如：F1
	 * @param keywords
	 *            POI名称中的关键字，多个不同的关键字请用英文分号分开(;)，例如：①肯德基；②肯德基;服务台;耐克
	 * @param isFullNameSearch
	 *            是否全名查找，true：使用全名，即关键字为POI名字；false：则POI名字包含关键字便可以
	 * @param listener
	 *            结果回调接口OnSearchPoiListener
	 */
	public static void searchPoiByMap(final String buildId, final String floor,
			final String keywords, final boolean isFullNameSearch,
			final OnSearchPoiListener listener) {
		new RMAsyncTask(new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				RMPois pois = new RMPois();
				pois.setInterfaceName(SEARCHPOIBYMAP);
				pois.setPoilist(new ArrayList<POI>());
				if (RMStringUtils.isEmpty(keywords)) {
					pois.setError_msg("keywords is null or empty");
					return pois;
				}
				if (RMStringUtils.isEmpty(buildId)
						|| RMStringUtils.isEmpty(floor)) {
					pois.setError_msg("(buildId or floor) is null or empty");
					return pois;
				}
				String[] key = keywords.split(";");
				if (key == null || key.length == 0) {
					pois.setError_msg("keywords is null or empty");
					return pois;
				}
				final String vector_path = RMFileUtil.getMapDataDir()
						+ RMD5Util.md5(buildId + "_" + floor + ".imap");
				File file = new File(vector_path);
				if (file.exists()) {
					try {
						Layer mLayer = new Layer();
						if (mLayer.readmap(vector_path)
								&& mLayer.shapes != null
								&& mLayer.shapes.length != 0) {// 地图数据读取成功
							for (int i = 0; i < mLayer.shapes.length; i++) {
								Shape shape = mLayer.shapes[i];
								if (!RMStringUtils.isEmpty(shape.mName)) {// 当关键字为空，直接找最近
									POI poi = null;
									for (String str : key) {
										if (RMStringUtils.isEmpty(str))
											continue;
										if (isFullNameSearch) {// 如果全名搜索
											if (str.equals(shape.mName)) {
												poi = shapeToPoi(shape,
														buildId, floor);
												break;
											}
										} else {
											if (shape.mName.contains(str)) {
												poi = shapeToPoi(shape,
														buildId, floor);
												break;
											}
										}
									}
									if (poi != null) {
										pois.getPoilist().add(poi);
										pois.setError_code(0);
										pois.setError_msg("Success");
									}
								}
							}
						}
					} catch (Exception e) {
						file.delete();
						e.printStackTrace();
					}
				} else {
					pois.setError_msg("map is not exist");
				}
				return pois;
			}

			private POI shapeToPoi(Shape s, String buildId, String floor) {
				POI poi = new POI();
				poi.setPoiNo(s.mId);
				poi.setName(s.mName);
				poi.setX(s.mCenter.mX / 1000f);
				poi.setY(s.mCenter.mY / 1000f);
				poi.setBuildId(buildId);
				poi.setFloor(floor);
				return poi;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null)
					listener.onSearchPoi((RMPois) obj);
			}
		}).run();

	}

	/**
	 * 通过本地缓存的地图文件，搜索某一点周围多少米内的所有POI
	 * 
	 * @param poi
	 *            中间点，需要包含建筑物id，楼层，x和y坐标值
	 * @param radius
	 *            半径，不可以为0，单位：米
	 * @param listener
	 *            结果回调接口OnSearchPoiListener
	 */
	public static void searchAroundPoiListByMap(final POI poi,
			final int radius, final OnSearchPoiListener listener) {
		new RMAsyncTask(new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				RMPois pois = new RMPois();
				pois.setInterfaceName(SEARCHAROUNDPOILISTBYMAP);
				pois.setPoilist(new ArrayList<POI>());
				if (poi == null) {
					pois.setError_msg("poi object is null");
					return pois;
				}
				if (RMStringUtils.isEmpty(poi.getBuildId())
						|| RMStringUtils.isEmpty(poi.getFloor())) {
					pois.setError_msg("(buildId or floor) is null or empty");
					return pois;
				}
				if (radius == 0) {
					pois.setError_msg("radius is 0");
					return pois;
				}
				final String vector_path = RMFileUtil.getMapDataDir()
						+ RMD5Util.md5(poi.getBuildId() + "_" + poi.getFloor()
								+ ".imap");
				File file = new File(vector_path);
				if (file.exists()) {
					try {
						Layer mLayer = new Layer();
						if (mLayer.readmap(vector_path)
								&& mLayer.shapes != null
								&& mLayer.shapes.length != 0) {// 地图数据读取成功
							for (int i = 0; i < mLayer.shapes.length; i++) {
								Shape shape = mLayer.shapes[i];
								if (!RMStringUtils.isEmpty(shape.mName)) {// 必须名字不为空
									POI p = null;

									if (RMathUtils.distance(poi.getX(),
											poi.getY(),
											shape.mCenter.mX / 1000f,
											shape.mCenter.mY / 1000f) <= radius) {// 在范围内就添加
										p = shapeToPoi(shape, poi.getBuildId(),
												poi.getFloor());
										pois.getPoilist().add(p);
										pois.setError_code(0);
										pois.setError_msg("Success");
									}
								}
							}
						}
					} catch (Exception e) {
						file.delete();
						e.printStackTrace();
					}
				} else {
					pois.setError_msg("map is not exist");
				}
				return pois;
			}

			private POI shapeToPoi(Shape s, String buildId, String floor) {
				POI poi = new POI();
				poi.setPoiNo(s.mId);
				poi.setName(s.mName);
				poi.setX(s.mCenter.mX / 1000f);
				poi.setY(s.mCenter.mY / 1000f);
				poi.setBuildId(buildId);
				poi.setFloor(floor);
				return poi;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null)
					listener.onSearchPoi((RMPois) obj);
			}
		}).run();

	}
}
