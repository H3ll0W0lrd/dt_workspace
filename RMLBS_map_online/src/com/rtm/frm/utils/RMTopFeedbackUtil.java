package com.rtm.frm.utils;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;

/**
 * 得到热门店铺排行榜
 * 
 * @author dingtao
 *
 */
public class RMTopFeedbackUtil {
	/**
	 * 获取热门店铺排行榜
	 * 
	 * @param api_key
	 *            智慧图LBS平台key
	 * @param buildId
	 *            建筑物id
	 * @param floor
	 *            楼层 例：楼层（B1,F1,F1.5）
	 * @param poi_no
	 *            POI编号或者id
	 * @param listener 结果回调接口
	 */
	public static void requestTopFeedback(String api_key, String buildId,
			String floor, String poi_no, OnTopFeedbackListener listener) {
		new RMAsyncTask(
				new CheckCall(listener, api_key, buildId, floor, poi_no)).run();
	}

	private static class CheckCall implements RMCallBack {
		OnTopFeedbackListener listener;
		String key;
		String buildId;
		String floor;
		String poi_no;

		public CheckCall(OnTopFeedbackListener listener, String key,
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
					RMHttpUrl.getWEB_URL() + RMHttpUrl.TOP_FEEDBACK,
					new String[] { "key", "buildid", "floor", "poi_no" },
					new String[] { key, buildId, floor, poi_no });
			if (RMHttpUtil.NET_ERROR.equals(result)) {
				return null;
			}
			return result;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (listener != null)
				listener.onFinished((String) obj);
		}
	}

	/**
	 * 热门店铺排行榜回调接口
	 * @author dingtao
	 */
	public interface OnTopFeedbackListener {
		/**
		 * 搜索结果回调方法，此方法结果为json数据，不做任何封装
		 * @param result json数据，请自行处理
		 */
		public void onFinished(String result);
	}

}
