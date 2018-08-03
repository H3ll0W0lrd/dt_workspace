package com.rtm.frm.utils;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.vmap.Layer;
import com.rtm.frm.vmap.Shape;

/**
 * 获取POI点详情
 * 
 * @author dingtao
 *
 */
public class RMPoiDetailUtil {
	/**
	 * 获取POI点详情
	 * 
	 * @param api_key
	 *            智慧图key
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            例：楼层（B1,F1,F1.5）
	 * @param poi_no
	 *            POI编号或者POI的id
	 * @param listener
	 *            结果回调接口
	 */
	public static void requestPoiDetail(String api_key, String buildId,
			String floor, String poi_no, OnGetPoiDetailListener listener) {
		new RMAsyncTask(
				new CheckCall(listener, api_key, buildId, floor, poi_no)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetPoiDetailListener listener;
		String key;
		String buildId;
		String floor;
		String poi_no;

		public CheckCall(OnGetPoiDetailListener listener, String key,
				String buildId, String floor, String poi_no) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
			this.floor = floor;
			this.poi_no = poi_no;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMPoiDetail route = new RMPoiDetail();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.POI_INFO,
						new String[] { "key", "buildid", "floor", "poi_no" },
						new String[] { key, buildId, floor, poi_no });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONObject poiobj = resultobj.getJSONObject("poiinfo");
						POI poi = new POI();
						poi.setBuildId(poiobj.getString("buildid"));
						poi.setFloor(poiobj.getString("floor"));
						poi.setPoiNo(Integer.parseInt(poiobj
								.getString("poi_no")));
						if (poiobj.has("name")) {
							poi.setName(poiobj.getString("name"));
						}
						if (poiobj.has("name_en")) {
							poi.setName_en(poiobj.getString("name_en"));
						}
						if (poiobj.has("name_qp")) {
							poi.setName_qp(poiobj.getString("name_qp"));
						}
						if (poiobj.has("name_jp")) {
							poi.setName_jp(poiobj.getString("name_jp"));
						}
						poi.setX(Float.parseFloat(poiobj.getString("x")));
						poi.setY(Float.parseFloat(poiobj.getString("y")));

						poi.setClassid(poiobj.getString("classid"));
						if (poiobj.has("classname")) {
							poi.setClassname(poiobj.getString("classname"));
						}
						poi.setStyle(Integer.parseInt(poiobj.getString("style")));
						route.setPoi(poi);
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
				listener.onFinished((RMPoiDetail) obj);
		}
	}

	/**
	 * 得到当前定位附近的POI信息，当关键字为空，得到离定位点最近的POI信息，当包含关键字，则返回包含此关键字并且离定位点最近的POI信息，
	 * 如果没有找到关键字信息，则返回null
	 * 
	 * @param location
	 *            定位位置，非空
	 * @param keywords
	 *            关键字，可以为空
	 * @param listener
	 *            结果回调接口
	 */
	public static void getPoiInfo(final RMLocation location,
			final String keywords, final OnGetPoiDetailListener listener) {
		new RMAsyncTask(new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				RMPoiDetail detail = new RMPoiDetail();
				final String vector_path = RMFileUtil.getMapDataDir()
						+ RMD5Util.md5(location.getBuildID()
								+ "_"
								+ RMStringUtils.floorTransform(location
										.getFloorID()) + ".imap");
				File file = new File(vector_path);
				if (file.exists()) {
					try {
						Layer mLayer = new Layer();
						if (mLayer.readmap(vector_path)
								&& mLayer.shapes != null
								&& mLayer.shapes.length != 0) {// 地图数据读取成功
							float abs = -1;// 两点之间的距离
							Shape s = null;
							for (int i = 0; i < mLayer.shapes.length; i++) {
								Shape shape = mLayer.shapes[i];
								if (RMStringUtils.isEmpty(shape.mName))
									continue;
								if (RMStringUtils.isEmpty(keywords)) {// 当关键字为空，直接找最近
									if (mLayer.inPolygon(shape,
											location.getX(), location.getY())) {
										s = shape;
										break;
									}
									float length = RMathUtils.distance(
											location.getCoordX(),
											location.getCoordY(),
											shape.mCenter.mX, shape.mCenter.mY);
									if (abs == -1 || abs > length) {
										abs = length;
										s = shape;
									}
								} else if (shape.mName.contains(keywords)) {
									if (mLayer.inPolygon(shape,
											location.getX(), location.getY())) {
										s = shape;
										break;
									}
									float length = RMathUtils.distance(
											location.getCoordX(),
											location.getCoordY(),
											shape.mCenter.mX, shape.mCenter.mY);
									if (abs == -1 || abs >= length) {
										abs = length;
										s = shape;
									}
								}
							}
							if (s != null) {// POI不为空
								POI poi = new POI();
								poi.setPoiNo(s.mId);
								poi.setName(s.mName);
								poi.setX(s.mCenter.mX / 1000f);
								poi.setY(s.mCenter.mY / 1000f);
								poi.setBuildId(location.getBuildID());
								poi.setFloor(location.getFloor());
								detail.setPoi(poi);
								detail.setError_code(0);
							}
						}
					} catch (Exception e) {
						file.delete();
						e.printStackTrace();
					}
				}
				return detail;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null)
					listener.onFinished((RMPoiDetail) obj);
			}
		}).run();

	}

	/**
	 * 获取POI详情回调接口
	 * 
	 * @author dingtao
	 */
	public interface OnGetPoiDetailListener {
		/**
		 * POI详情回调方法
		 * 
		 * @param result
		 *            详情请查看RMPoiDetail
		 */
		public void onFinished(RMPoiDetail result);
	}

	/**
	 * 获取POI描述（相关信息）
	 * 
	 * @param api_key
	 *            智慧图key
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            例：楼层（B1,F1,F1.5）
	 * @param poi_no
	 *            POI编号或者POI的id
	 * @param listener
	 *            结果回调接口
	 */
	public static void requestPoiDesc(String api_key, String buildId,
			String floor, String poi_no, OnGetPoiDescListener listener) {
		new RMAsyncTask(new GetPoiDescCall(listener, api_key, buildId, floor,
				poi_no)).run();
	}

	private static class GetPoiDescCall implements RMCallBack {
		OnGetPoiDescListener listener;
		String key;
		String buildId;
		String floor;
		String poi_no;

		public GetPoiDescCall(OnGetPoiDescListener listener, String key,
				String buildId, String floor, String poi_no) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
			this.floor = floor;
			this.poi_no = poi_no;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
					RMHttpUrl.getWEB_URL() + RMHttpUrl.POI_DESC, new String[] {
							"key", "buildid", "floor", "poi_no" },
					new String[] { key, buildId, floor, poi_no });
			if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
				return result;
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object result) {
			if (listener != null)
				listener.onGetPoiDesc((String) result);
		}
	}

	/**
	 * 获取POI描述（相关信息）回调接口
	 * 
	 * @author dingtao
	 *
	 */
	public interface OnGetPoiDescListener {
		/**
		 * POI描述回调方法
		 * 
		 * @param result
		 *            JSON格式的数据，请自行解析
		 */
		public void onGetPoiDesc(String result);
	}
}
