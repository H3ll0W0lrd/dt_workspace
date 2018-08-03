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
 * 获得某一建筑物下所有POI的分类
 * 
 * @author dingtao
 *
 */
public class RMBuildPoiCateUtil {
	/**
	 * 获得某一建筑物下所有POI的分类
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param buildId
	 *            建筑物ID
	 * @param listener
	 *            结果回调
	 */
	public static void requestBuildPoiCate(String api_key, String buildId,
			OnGetBuildPoiCateListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildId)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetBuildPoiCateListener listener;
		String key;
		String buildId;

		public CheckCall(OnGetBuildPoiCateListener listener, String key,
				String buildId) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMCateList route = new RMCateList();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.BUILD_POI_CATE,
						new String[] { "key", "buildid" }, new String[] { key,
								buildId });
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
	 * 获得某一建筑物下所有POI的分类回调接口
	 * 
	 * @author dingtao
	 */
	public interface OnGetBuildPoiCateListener {
		/**
		 * 分类结果回调方法
		 * @param result 具体使用请查看RMCateList
		 */
		public void onFinished(RMCateList result);
	}

}
