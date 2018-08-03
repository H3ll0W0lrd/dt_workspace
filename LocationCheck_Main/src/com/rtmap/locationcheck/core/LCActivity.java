package com.rtmap.locationcheck.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import com.rtm.common.utils.RMFileUtil;
import com.rtm.location.LocationApp;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
import com.rtmap.locationcheck.core.model.BroadcastInfo;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;

public class LCActivity extends FragmentActivity {

	public final static int PHOTO = 0;// 相册选取
	public final static int CAMERA = 1;// 拍照
	public Dialog mLoadDialog;// 加载框
	public float adjustLength = 5;// 调整距离为1像素

	/** 记录处于前台的Activity */
	private static LCActivity mForegroundActivity = null;
	public String USER_NAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DTLog.e("getTaskId = " + getTaskId());
		USER_NAME = LCApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, "");
		initLoad();
		adjustLength = Float.parseFloat(LCApplication.getInstance().getShare()
				.getString("step_adjust", "5"));
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * 加载beacon数据
	 * 
	 * @param mBeaconFilePath
	 * @return
	 */
	public BeaconList loadBeaconData(String mBeaconFilePath) {
		File file = new File(mBeaconFilePath);
		if (!file.exists())
			return null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			String line;
			StringBuilder build = new StringBuilder();
			while ((line = br.readLine()) != null) {
				// 将文本打印到控制台
				build.append(line);
			}
			br.close();
			String result = build.toString();
			if (!DTStringUtils.isEmpty(result)) {

				JSONObject o = new JSONObject(result);
				JSONArray array = o.getJSONArray("list");
				if (array.length() > 0) {
					BeaconList list = new BeaconList();
					list.setList(new ArrayList<BeaconInfo>());
					for (int j = 0; j < array.length(); j++) {
						BeaconInfo info = new BeaconInfo();
						JSONObject obj = array.getJSONObject(j);
						if (obj.has("uuid"))
							info.setUuid(obj.getString("uuid"));
						if (obj.has("minor16"))
							info.setMinor16(obj.getString("minor16"));
						if (obj.has("broadcast_id"))
							info.setBroadcast_id(obj.getString("broadcast_id"));
						if (obj.has("minor"))
							info.setMinor(obj.getString("minor"));
						if (obj.has("major16"))
							info.setMajor16(obj.getString("major16"));
						if (obj.has("major"))
							info.setMajor(obj.getString("major"));
						if (obj.has("mac"))
							info.setMac(obj.getString("mac"));
						if (obj.has("inshop"))
							info.setInshop(obj.getInt("inshop"));
						if (obj.has("finger"))
							info.setFinger(obj.getInt("finger"));
						if (obj.has("edit_status"))
							info.setEdit_status(obj.getInt("edit_status"));
						if (obj.has("Threshold_switch_min"))
							info.setThreshold_switch_min(obj
									.getInt("Threshold_switch_min"));
						if (obj.has("Threshold_switch_max"))
							info.setThreshold_switch_max(obj
									.getInt("Threshold_switch_max"));
						if (obj.has("output_power"))
							info.setOutput_power(obj.getInt("output_power"));
						if (obj.has("rssi"))
							info.setRssi(obj.getInt("rssi"));
						if (obj.has("rssi_max"))
							info.setRssi_max(obj.getInt("rssi_max"));
						if (obj.has("work_status"))
							info.setWork_status(obj.getInt("work_status"));
						if (obj.has("x"))
							info.setX(obj.getInt("x"));
						if (obj.has("y"))
							info.setY(obj.getInt("y"));
						if (obj.has("buildId"))
							info.setBuildId(obj.getString("buildId"));
						if (obj.has("floor"))
							info.setFloor(obj.getString("floor"));
						if (obj.has("isClick"))
							info.setClick(obj.getBoolean("isClick"));
						if (obj.has("maclist")) {
							JSONArray maclistarray = obj
									.getJSONArray("maclist");
							info.setMaclist(new ArrayList<BroadcastInfo>());
							for (int i = 0; i < maclistarray.length(); i++) {
								JSONObject u = maclistarray.getJSONObject(i);
								BroadcastInfo broad = new BroadcastInfo();
								broad.setMac(u.getString("mac"));
								broad.setMajor(u.getString("major"));
								broad.setMinor(u.getString("minor"));
								broad.setUuid(u.getString("uuid"));
								info.getMaclist().add(broad);
							}
						}
						list.getList().add(info);
					}
					return list;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void initLocation() {
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		if (istext)
			LocationApp.getInstance().setRootFolder(
					"rtmapCheck" + File.separator + "test");
		else
			LocationApp.getInstance().setRootFolder(
					"rtmapCheck" + File.separator + "publish");
		RMFileUtil.createPath(RMFileUtil.getBuildJudgeDir());
		try {
			if (!new File(RMFileUtil.getBuildJudgeDir() + "beacons.bei")
					.exists())
				DTFileUtils.copyAssestToSD(RMFileUtil.getBuildJudgeDir()
						+ "beacons.bei", "beacons.bei");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		LocationApp.getInstance().init(getApplicationContext());// 初始化定位
		LocationApp.getInstance().setUseRtmapError(true);// 设置使用智慧图错误码
		LocationApp.getInstance().setTestStatus(istext);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mForegroundActivity = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mForegroundActivity = this;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onViewClick(View v) {

	}

	/**
	 * 初始化加载框
	 */
	private void initLoad() {
		mLoadDialog = new ProgressDialog(this);// 加载框
		mLoadDialog.setCanceledOnTouchOutside(false);
		mLoadDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (mLoadDialog.isShowing() && keyCode == KeyEvent.KEYCODE_BACK) {
					cancelLoadDialog();
					mLoadDialog.cancel();
				}
				return false;
			}
		});
	}

	public void cancelLoadDialog() {
	}

	/**
	 * 得到图片的路径
	 * 
	 * @param fileName
	 * @param requestCode
	 * @param data
	 * @return
	 */
	public String getFilePath(String fileName, int requestCode, Intent data) {
		if (requestCode == CAMERA) {
			return fileName;
		} else if (requestCode == PHOTO) {
			Uri uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			String img_path = actualimagecursor
					.getString(actual_image_column_index);
			// 4.0以上平台会自动关闭cursor,所以加上版本判断,OK
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				actualimagecursor.close();
			return img_path;
		}
		return null;
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return [0-9]{5,9}
	 */
	public boolean isMobileNO(String mobiles) {
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobiles);
			return m.matches();
		} catch (Exception e) {
		}
		return false;
	}

	/** 获取当前处于前台的activity */
	public static LCActivity getForegroundActivity() {
		return mForegroundActivity;
	}
}
