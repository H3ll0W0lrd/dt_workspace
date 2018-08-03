package com.rtm.frm.utils;

import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;
import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.vmap.readmap;

/**
 * 下载地图工具类
 * 
 * @author dingtao
 *
 */
public class RMDownLoadMapUtil {
	private static HashMap<String, RMCallBack> downTask = new HashMap<String, RMCallBack>();

	/**
	 * 下载地图
	 * 
	 * @param key
	 *            智慧图LBS平台key
	 * @param build
	 *            建筑物ID
	 * @param floor
	 *            楼层，例：20100
	 * @param onMapDownLoadFinishListener
	 *            地图加载结束后回调接口
	 */
	public static void downLoadMap(String key, final String build, int floor,
			OnMapDownLoadFinishListener onMapDownLoadFinishListener) {
		downLoadMap(key, build, RMStringUtils.floorTransform(floor),
				onMapDownLoadFinishListener);
	}

	/**
	 * 下载地图
	 * 
	 * @param key
	 *            智慧图LBS平台key
	 * @param build
	 *            建筑物ID
	 * @param floor
	 *            楼层，例：F2
	 * @param onMapDownLoadFinishListener
	 *            地图加载完后的回调接口
	 */
	public static void downLoadMap(final String key, final String build,
			final String floor,
			final OnMapDownLoadFinishListener onMapDownLoadFinishListener) {
		final String vector_path = RMFileUtil.getMapDataDir()
				+ RMD5Util.md5(build + "_" + floor + ".imap");
		if (!downTask.containsKey(vector_path)) {
			RMFileUtil.createPath(RMFileUtil.getMapDataDir());
			RMCallBack call = new RMCallBack() {

				@Override
				public Object onCallBackStart(Object... obj) {
					downTask.put(vector_path, this);
					String str = RMHttpUtil.connInfo(RMHttpUtil.POST,
							RMHttpUrl.getWEB_URL() + RMHttpUrl.CHECK_IMAP,
							new String[] { "key", "buildid", "floor" },
							new String[] { key, build, floor });
					if (str != null && !RMHttpUtil.NET_ERROR.equals(str)) {// 非连接失败
						try {
							JSONObject json = new JSONObject(str);
							JSONObject result = new JSONObject(
									json.getString("result"));
							if (result.getString("error_code").equals("0")) {
								File file = new File(vector_path);
								String release_no = json
										.getString("release_no");
								if (!file.exists()
										|| !(readmap
												.getFileVersion(vector_path) + "")
												.equals(release_no)) {
									String imap_url = json
											.getString("imap_url");
									return downloadFile(vector_path, imap_url);
								}
							} else {
								Handlerlist.getInstance().notifications(
										Constants.RTMAP_MAP,
										Constants.MAP_FailNetResult, str);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						Handlerlist.getInstance().notifications(
								Constants.RTMAP_MAP,
								Constants.MAP_FailCheckNet, null);
					}
					return false;
				}

				@Override
				public void onCallBackFinish(Object obj) {
					// 如果下载成功或者校验成功，则保留路径，说明文件夹有，如果没成功，移除后，重新再来一次
					downTask.remove(vector_path);
					if (onMapDownLoadFinishListener != null)
						onMapDownLoadFinishListener.OnMapDownLoadFinish();
				}
			};
			new RMAsyncTask(call).run();
		}
	}

	/**
	 * 下载地图文件
	 * 
	 * @param path
	 * @param url
	 * @return
	 */
	private static boolean downloadFile(String path, String url) {
		boolean isUpdate;
		File file1 = new File(path);
		if (file1.exists()) {
			isUpdate = true;
		} else {
			isUpdate = false;
		}

		File file = new File(path + ".map");
		file.delete();
		RMHttpUtil.downloadFile(url, path + ".map");
		if (file.exists()) {// 如果存在说明是下载或者更新成功
			if (file1.exists())
				RMFileUtil.deleteFile(path);// 删除源文件
			file.renameTo(file1);// 更改为源文件
			if (isUpdate) {//
				Handlerlist.getInstance().notifications(Constants.RTMAP_MAP,
						Constants.MAP_Update_Success, null);
			} else {
				Handlerlist.getInstance().notifications(Constants.RTMAP_MAP,
						Constants.MAP_Down_Success, null);
			}
			return true;
		} else {
			if (isUpdate) {
				Handlerlist.getInstance().notifications(Constants.RTMAP_MAP,
						Constants.MAP_Update_Fail, null);
			} else {
				Handlerlist.getInstance().notifications(Constants.RTMAP_MAP,
						Constants.MAP_Down_Fail, null);
			}
			return false;
		}
	}
}
