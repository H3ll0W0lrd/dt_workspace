package com.rtm.location.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.location.entity.BuildAngleList;

/**
 * 获取建筑物偏转角工具
 * 
 * @author dingtao
 *
 */
public class RMBuildAngleUtil {

	/**
	 * 请求建筑物偏转角
	 * 
	 * @param api_key
	 *            智慧图api_key
	 * @param buildid
	 *            建筑物ID数组
	 * @param listener
	 *            回调接口
	 */
	public static void requestBuildAngle(String api_key, String[] buildidArray,
			OnGetBuildAngleListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildidArray)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetBuildAngleListener listener;
		String key;
		String[] buildidArray;

		public CheckCall(OnGetBuildAngleListener listener, String key,
				String[] buildidArray) {
			this.listener = listener;
			this.key = key;
			this.buildidArray = buildidArray;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			BuildAngleList route = new BuildAngleList();
			try {
				JSONArray array = new JSONArray();
				for (int i = 0; i < buildidArray.length; i++) {
					array.put(buildidArray[i]);
				}
				JSONObject json = new JSONObject();
				json.put("buildid_list", array);
				json.put("key", key);
				String result = RMHttpUtil.postConnection(
						RMHttpUrl.getWEB_URL() + RMHttpUrl.MAP_ANGLE,
						json.toString());
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONArray cityarray = resultobj
								.getJSONArray("build_angle_list");
						ArrayList<BuildInfo> list = new ArrayList<BuildInfo>();
						for (int j = 0; j < cityarray.length(); j++) {
							BuildInfo build = new BuildInfo();
							build.setBuildId(cityarray.getJSONObject(j)
									.getString("buildid"));
							build.setMapAngle(Float.parseFloat(cityarray
									.getJSONObject(j).getString("angle")));
							list.add(build);
						}
						route.setList(list);
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
				listener.onGetBuildAngle((BuildAngleList) obj);
		}
	}

	/**
	 * 获取建筑物偏转角回调接口
	 * 
	 * @author dingtao
	 */
	public interface OnGetBuildAngleListener {
		/**
		 * 建筑物偏转角回调方法
		 * 
		 * @param result
		 *            具体使用请查看{@link BuildAngleList}
		 */
		public void onGetBuildAngle(BuildAngleList result);
	}

}
