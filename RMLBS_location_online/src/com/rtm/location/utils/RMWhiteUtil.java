package com.rtm.location.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.common.utils.RMConfig;
import com.rtm.location.entity.BlueBeacocnStatus;

/**
 * 白名单的
 * 
 * @author dingtao
 *
 */
public class RMWhiteUtil {
	public static void getWhiteInfo(String key,
			OnGetWhiteListener onGetWhiteListener) {
		new RMAsyncTask(new WhiteCall(key, onGetWhiteListener)).run();
	}

	private static class WhiteCall implements RMCallBack {
		private String key;
		private OnGetWhiteListener onGetWhiteListener;

		public WhiteCall(String key, OnGetWhiteListener onGetWhiteListener) {
			this.key = key;
			this.onGetWhiteListener = onGetWhiteListener;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			String result = RMHttpUtil.connInfo(RMHttpUtil.POST,
					RMHttpUrl.getWEB_URL() + RMHttpUrl.WHITE_LIST,
					new String[] { "key", "model", "device_id2", "id_type2" },
					new String[] { key, android.os.Build.MODEL, RMConfig.mac,
							"MAC" });
			if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
				return result;
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				onGetWhiteListener.onGetWhite((String) obj);
			}
		}

	}

	public static ArrayList<BlueBeacocnStatus> getWhite(String result) {
		try {
			JSONObject obj = new JSONObject(result);
			JSONObject errorobj = obj.getJSONObject("result");
			if (errorobj.getString("error_code").equals("0")) {
				JSONObject dataobj = obj.getJSONObject("data");
				JSONArray array = dataobj.getJSONArray("signal_rule");
				ArrayList<BlueBeacocnStatus> list = new ArrayList<BlueBeacocnStatus>();
				for (int i = 0; i < array.length(); i++) {
					JSONObject a = array.getJSONObject(i);
					JSONObject status = a.getJSONObject("status");
					JSONObject beacon_rule = a.getJSONObject("beacon_rule");
					BlueBeacocnStatus bbs = new BlueBeacocnStatus();
					bbs.setBeacon(Integer.valueOf(status.getInt("bluethooth")));
					bbs.setWifi(Integer.valueOf(status.getInt("wifi")));
					bbs.setMax(Integer.valueOf(beacon_rule.getInt("max")));
					bbs.setMin(Integer.valueOf(beacon_rule.getInt("min")));
					bbs.setWeight(Double.valueOf(beacon_rule.getDouble("weight")));
					list.add(bbs);
				}
				return list;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public interface OnGetWhiteListener {
		void onGetWhite(String result);
	}
}
