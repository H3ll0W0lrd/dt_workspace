package com.rtm.location.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.location.entity.RMUser;

/**
 * 白名单的
 * 
 * @author dingtao
 *
 */
public class RMUserUtil {
	public static void getUserInfo(Context context,String apikey,
			OnGetUserListener listener) {
		new RMAsyncTask(new UserCall(context,apikey, listener)).run();
	}

	private static class UserCall implements RMCallBack {
		private Context context;
		private OnGetUserListener listener;
		private String key;

		public UserCall(Context context,String key,
				OnGetUserListener listener) {
			this.context = context;
			this.key = key;
			this.listener = listener;
		}

		@SuppressLint("NewApi")
		@Override
		public Object onCallBackStart(Object... o) {
			try {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				JSONObject obj = new JSONObject();
				obj.put("key", key);
				obj.put("pakagename", context.getPackageName());
				obj.put("sdkversion", RMVersionLocation.VERSION);
				// obj.put("otheruid", "");
				obj.put("mac", PhoneManager.getMac(context));
				obj.put("imei", tm.getDeviceId());//
				// obj.put("idfa", "");
				// obj.put("idfv", "");
				obj.put("tel", tm.getLine1Number());
				// obj.put("deviceid", "");//手机串号就是IMEI
				obj.put("device_type", android.os.Build.MODEL);
				obj.put("device_brand", android.os.Build.BRAND);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
						&& Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					obj.put("cpu_type", android.os.Build.CPU_ABI2);
				else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					obj.put("cpu_type", android.os.Build.SUPPORTED_ABIS[0]);
				obj.put("os_version", Build.VERSION.RELEASE);
				obj.put("android_id", Settings.Secure.getString(
						context.getContentResolver(), Settings.Secure.ANDROID_ID));
				String result = RMHttpUtil.postHttpsConnection(RMHttpUrl.LBS_USER_ID,
						obj.toString());
				// {"result":{"req":"lbsid","error_code":"0","error_msg":"Success"},
				// "lbsid":"408a237dfba171239b13cbdffc5db974","delaylocate_time":"30",
				// "isbadlog_return":"1","isphone_whitelist":"1","expiration_time":"1473478588392"}
				if(result != null && !RMHttpUtil.NET_ERROR.equals(result)){
					JSONObject resultobj = new JSONObject(result);
					JSONObject errorobj = resultobj.getJSONObject("result");
					if (Integer.parseInt(errorobj
							.getString("error_code")) == 0) {
						RMUser user = new RMUser();
						user.setLbsid(resultobj.getString("lbsid"));
						user.setDelaylocate_time(Integer.valueOf(resultobj.getString("delaylocate_time")));
						user.setExpiration_time(resultobj.getString("expiration_time"));
						user.setIsbadlog_return(Integer.valueOf(resultobj.getString("isbadlog_return")));
						user.setIsphone_whitelist(Integer.valueOf(resultobj.getString("isphone_whitelist")));
						RMSqlite.getInstance().addUser(user);
						return user;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null&&listener!=null) {
				listener.onGetUser((RMUser) obj);
			}
		}

	}

	public interface OnGetUserListener {
		void onGetUser(RMUser result);
	}
}
