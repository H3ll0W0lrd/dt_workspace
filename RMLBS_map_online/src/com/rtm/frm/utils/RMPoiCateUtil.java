package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.CateInfo;
import com.rtm.frm.model.RMCateList;

/**
 * 获取某一楼层下所有POI的分类
 * 
 * @author dingtao
 *
 */
public class RMPoiCateUtil {
	/**
	 * 获取某一楼层下所有POI的分类
	 * 
	 * @param api_key
	 *            智慧图key
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            例：楼层（B1,F1,F1.5）
	 * @param listener
	 *            结果回调接口
	 */
	public static void requestPoiCate(String api_key, String buildId,
			String floor, onGetPoiCateListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildId, floor)).run();
	}

	private static class CheckCall implements RMCallBack {
		onGetPoiCateListener listener;
		String key;
		String buildId;
		String floor;

		public CheckCall(onGetPoiCateListener listener, String key,
				String buildId, String floor) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
			this.floor = floor;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMCateList route = new RMCateList();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.FLOOR_POI_CATE,
						new String[] { "key", "buildid", "floor" },
						new String[] { key, buildId, floor });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONArray array = resultobj
								.getJSONArray("classification");
						ArrayList<CateInfo> list = new ArrayList<CateInfo>();
						for (int i = 0; i < array.length(); i++) {
							CateInfo point = new CateInfo();
							JSONObject pointobj = array.getJSONObject(i);
							point.setId(Integer.parseInt(pointobj
									.getString("id")));
							point.setName(pointobj.getString("name"));
							list.add(point);
						}
						route.setCatelist(list);
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
				listener.onFinished((RMCateList) obj);
		}
	}

	/**
	 * POI分类回调接口
	 * 
	 * @author dingtao
	 */
	public interface onGetPoiCateListener {
		/**
		 * POI分类结果回调方法
		 * 
		 * @param result
		 *            详细说明请查看RMCateList
		 */
		public void onFinished(RMCateList result);
	}

}
