package com.rtm.location.utils;

/**
 * 文件下载完成回调接口
 * @author dingtao
 *
 */
public interface OnFileDownLoadFinishListener {
	
	/**
	 * 文件下载完成回调方法
	 * @param isSuccess true是文件下载成功
	 */
	void onFileDownLoadFinish(boolean isSuccess);
}
