package com.rtm.frm.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.Floor;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.frm.model.RMFloorDetail;

/**
 * 获取某一建筑物具体楼层详情
 * 
 * @author dingtao
 *
 */
public class RMFloorDetailUtil {
	/**
	 * 获取某一建筑物具体楼层详情
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param buildId
	 *            楼层ID
	 * @param floor
	 *            例：楼层（B1,F1,F1.5）
	 * @param listener
	 *            结果回调
	 */
	public static void requestFloorDetail(String api_key, String buildId,
			String floor, OnGetFloorDetailListener listener) {
		new RMAsyncTask(new CheckCall(listener, api_key, buildId, floor)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnGetFloorDetailListener listener;
		String key;
		String buildId;
		String floor;

		public CheckCall(OnGetFloorDetailListener listener, String key,
				String buildId, String floor) {
			this.listener = listener;
			this.key = key;
			this.buildId = buildId;
			this.floor = floor;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			RMFloorDetail route = new RMFloorDetail();
			try {
				String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
						RMHttpUrl.getWEB_URL() + RMHttpUrl.FLOOR_INFO,
						new String[] { "key", "buildid", "floor" },
						new String[] { key, buildId, floor });
				if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					route.setError_code(Integer.parseInt(errorobj
							.getString("error_code")));
					route.setError_msg(errorobj.getString("error_msg"));
					if (route.getError_code() == 0) {
						JSONObject floorobj = resultobj.getJSONObject("floor");
						Floor floor = new Floor();
						floor.setBuildid(floorobj.getString("buildid"));
						floor.setFloor(floorobj.getString("floor"));
						if (floorobj.has("desc")) {
							floor.setDescription(floorobj.getString("desc"));
						}
						route.setFloor(floor);
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
				listener.onFinished((RMFloorDetail) obj);
		}
	}

	/**
	 * 获取某一建筑物具体楼层详情回调
	 * 
	 * @author dingtao
	 */
	public interface OnGetFloorDetailListener {
		/**
		 * 楼层详情回调方法
		 * @param result
		 *            楼层详情，具体请查看RMFloorDetail
		 */
		public void onFinished(RMFloorDetail result);
	}

}
