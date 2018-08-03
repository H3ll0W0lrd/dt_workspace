package com.rtm.frm.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.Floor;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.RMBuildDetail;

/**
 * 获取建筑物详情
 * 
 * @author dingtao
 *
 */
public class RMBuildDetailUtil {
	/**
	 * 获取建筑物详情
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param buildId
	 *            建筑物ID
	 * @param listener
	 *            结果回调接口
	 */
	public static void requestBuildDetail(String api_key, String buildId,
			OnGetBuildDetailListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildId)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetBuildDetailListener listener;
		String key;
		String buildId;

		public CheckCall(OnGetBuildDetailListener listener, String key,
				String buildId) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMBuildDetail route = new RMBuildDetail();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.BUILD_DETAIL,
						new String[] { "key", "buildid" }, new String[] { key,
								buildId });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONObject buildobj = resultobj
								.getJSONObject("build_detail");
						BuildInfo build = new BuildInfo();
						build.setBuildId(buildobj.getString("buildid"));
						if (buildobj.has("name_chn")) {
							build.setBuildName(buildobj.getString("name_chn"));
						}
						if (buildobj.has("lat") && buildobj.has("long")) {
							Float lat = Float.parseFloat(buildobj
									.getString("lat"));
							Float lng = Float.parseFloat(buildobj
									.getString("long"));
							build.setLatLong(lat, lng);
						}
						if (buildobj.has("name_en")) {
							build.setName_en(buildobj.getString("name_en"));
						}
						if (buildobj.has("name_jp")) {
							build.setName_jp(buildobj.getString("name_jp"));
						}
						if (buildobj.has("name_qp")) {
							build.setName_qp(buildobj.getString("name_qp"));
						}

						JSONArray array = buildobj.getJSONArray("floorinfo");
						ArrayList<Floor> list = new ArrayList<Floor>();
						for (int i = 0; i < array.length(); i++) {
							Floor point = new Floor();
							JSONObject pointobj = array.getJSONObject(i);
							point.setFloor(pointobj.getString("floor"));
							if (pointobj.has("desc")) {
								point.setDescription(pointobj.getString("desc"));
							}
							list.add(point);
						}
						build.setFloorlist(list);
						route.setBuild(build);
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
				listener.onFinished((RMBuildDetail) obj);
		}
	}

	/**
	 * 获取建筑物详情回调
	 * 
	 * @author dingtao
	 */
	public interface OnGetBuildDetailListener {
		/**
		 * 请求返回结果
		 * 
		 * @param result
		 *            使用请查看RMBuildDetail
		 */
		public void onFinished(RMBuildDetail result);
	}

}
