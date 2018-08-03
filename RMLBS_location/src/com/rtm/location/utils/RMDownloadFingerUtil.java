package com.rtm.location.utils;

import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMCallBack;

/**
 * 下载定位文件，用于定位库离线计算并实现离线定位。包含下载建筑物判断文件、离线计算文件等。
 * @author dingtao
 *
 */
public class RMDownloadFingerUtil {

	/**
	 * 更新wifi建筑物判断文件30001.zip，其中里面会包含一个bbs文件，bbs包括了所有wifi数据信息，
	 * 通过对比wifi数据我们判断出建筑物ID
	 * 
	 * @param listener
	 *            更新完成后的回调器
	 */
	public static void updateWifiBuildJudgeFile(
			final OnFileDownLoadFinishListener listener) {
		RMCallBack call = new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				return FingerDownload.updateWifiBuildJudgeFile();
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (listener != null)
					listener.onFileDownLoadFinish((Boolean) obj);
			}
		};
		new RMAsyncTask(call).run();
	}

	/**
	 * 更新beacon建筑物判断文件40001.zip，解压后每个建筑物是一个_mac文件
	 * 
	 * @param listener
	 *            更新完成后的回调器
	 */
	public static void updateBeaconBuildJudgeFile(
			final OnFileDownLoadFinishListener listener) {
		RMCallBack call = new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				return FingerDownload.updateBeaconBuildJudgeFile();
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (listener != null)
					listener.onFileDownLoadFinish((Boolean) obj);
			}
		};
		new RMAsyncTask(call).run();
	}

	/**
	 * 下载wifi指纹文件
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @param listener
	 *            更新完成后回调器
	 */
	public static void updateWifiBfp3File(final String buildID,
			final OnFileDownLoadFinishListener listener) {
		RMCallBack call = new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				return FingerDownload.updateWifiBfp3File(buildID);
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (listener != null)
					listener.onFileDownLoadFinish((Boolean) obj);
			}
		};
		new RMAsyncTask(call).run();
	}

	/**
	 * 下载地磁辅助定位文件
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @param listener
	 *            更新完成后回调器
	 */
	public static void updateMgn1File(final String buildID,
			final OnFileDownLoadFinishListener listener) {
		RMCallBack call = new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				return FingerDownload.updateMgn1File(buildID);
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (listener != null)
					listener.onFileDownLoadFinish((Boolean) obj);
			}
		};
		new RMAsyncTask(call).run();
	}

	/**
	 * 更新beacon指纹文件bbp2
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @param listener
	 *            更新完成的回调器
	 */
	public static void updateBeaconBbp2File(final String buildID,
			final OnFileDownLoadFinishListener listener) {
		RMCallBack call = new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				return FingerDownload.updateBeaconBbp2File(buildID);
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (listener != null)
					listener.onFileDownLoadFinish((Boolean) obj);
			}
		};
		new RMAsyncTask(call).run();
	}

	/**
	 * 更新地图约束文件
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @param listener
	 *            更新完成回调器
	 */
	public static void updateMapMatchFile(final String buildID,
			final OnFileDownLoadFinishListener listener) {
		RMCallBack call = new RMCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				return FingerDownload.updateMapMatchFile(buildID);
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (listener != null)
					listener.onFileDownLoadFinish((Boolean) obj);
			}
		};
		new RMAsyncTask(call).run();
	}
}
