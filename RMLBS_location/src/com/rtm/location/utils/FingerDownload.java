package com.rtm.location.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;

public class FingerDownload {
	private static final String TAG = "FingerDownload";
	public static boolean IS_DOWNING = false;

	/**
	 * 更新wifi建筑物判断文件30001.zip，其中里面会包含一个bbs文件，bbs包括了所有wifi数据信息，
	 * 通过对比wifi数据我们判断出建筑物ID
	 * 
	 * @return 是否下载成功
	 */
	public static boolean updateWifiBuildJudgeFile() {
		boolean isFileUpdate = false;
		File file = new File(RMFileUtil.getBuildJudgeDir()
				+ RMFileUtil.CHINA_BUILD_CODE + "_"
				+ RMFileUtil.WIFI_BUILD_JUDGE_FILE_FLOOR_CODE + ".zip");
		String onLineFileInfo = "";
		String params = "buildId=" + RMFileUtil.CHINA_BUILD_CODE;
		params += "&floorId=" + RMFileUtil.WIFI_BUILD_JUDGE_FILE_FLOOR_CODE
				+ "&extension=" + RMFileUtil.EXT_ZIP;
		onLineFileInfo = post(RMHttpUrl.FILE_INFO_URL, params);

		String onlineFileMd5 = "";
		if (!onLineFileInfo.contains("\"status\":\"0\"")) {
			try {
				JSONObject jObject = new JSONObject(onLineFileInfo);
				onlineFileMd5 = jObject.getString("md5");
			} catch (JSONException e) {

			}
		}
		if (!file.exists()) {
			isFileUpdate = loadWifiBuildJudgeFile(onlineFileMd5);
		} else {
			try {
				String localFileMd5 = RMFileUtil.getMd5ByFile(file);
				if ((!RMStringUtils.isEmpty(onlineFileMd5))
						&& (!RMStringUtils.isEmpty(localFileMd5))
						&& (!localFileMd5.equals(onlineFileMd5))) {
					isFileUpdate = loadWifiBuildJudgeFile(onlineFileMd5);
				}
			} catch (Exception e) {
			}
		}
		return isFileUpdate;
	}

	/**
	 * 更新beacon建筑物判断文件40001.zip，解压后每个建筑物是一个_mac文件
	 * 
	 * @return 是否下载成功
	 */
	public static boolean updateBeaconBuildJudgeFile() {
		boolean isFileUpdate = false;
		File file = new File(RMFileUtil.getBuildJudgeDir()
				+ RMFileUtil.CHINA_BUILD_CODE + "_"
				+ RMFileUtil.BEACON_BUILD_JUDGE_FILE_FLOOR_CODE + ".zip");
		String lastMd5 = getFileMD5(RMFileUtil.CHINA_BUILD_CODE,
				RMFileUtil.BEACON_BUILD_JUDGE_FILE_FLOOR_CODE,
				RMFileUtil.EXT_ZIP);
		if (!file.exists()) {
			isFileUpdate = loadBeaconBuildJudgeFile(lastMd5);
		} else {
			try {
				String currentMd5 = RMFileUtil.getMd5ByFile(file);
				if ((!RMStringUtils.isEmpty(lastMd5))
						&& (!RMStringUtils.isEmpty(currentMd5))
						&& (!currentMd5.equals(lastMd5))) {
					isFileUpdate = loadBeaconBuildJudgeFile(lastMd5);
				}
			} catch (Exception e) {
			}
		}
		return isFileUpdate;
	}

	/**
	 * 下载wifi指纹文件
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @return 是否下载成功
	 */
	public static boolean updateWifiBfp3File(final String buildID) {
		boolean isFileUpdate = false;
		File file = new File(RMFileUtil.getFingerDir() + buildID + "/"
				+ buildID + ".bfp3");
		String lastMd5 = getFileMD5(buildID, "",
				RMFileUtil.WIFI_FINGER_FILE_EXT);
		if (!file.exists()) {
			isFileUpdate = loadBfp3File(RMFileUtil.getFingerDir() + buildID,
					buildID, lastMd5);
		} else {
			try {
				String currentMd5 = RMFileUtil.getMd5ByFile(file);
				if ((!RMStringUtils.isEmpty(lastMd5))
						&& (!RMStringUtils.isEmpty(currentMd5))
						&& (!currentMd5.equals(lastMd5))) {
					isFileUpdate = loadBfp3File(RMFileUtil.getFingerDir()
							+ buildID, buildID, lastMd5);
				}
			} catch (Exception e) {
			}
		}
		return isFileUpdate;
	}

	/**
	 * 下载地磁辅助定位文件
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @return 是否下载成功
	 */
	public static boolean updateMgn1File(final String buildID) {
		boolean isFileUpdate = false;
		File file = new File(RMFileUtil.getFingerDir() + buildID + "/"
				+ buildID + ".mgn1");
		String lastMd5 = getFileMD5(buildID, "", RMFileUtil.MAGNETIC_FILE_EXT);
		if (!file.exists()) {
			isFileUpdate = loadMgn1File(RMFileUtil.getFingerDir() + buildID,
					buildID, lastMd5);
		} else {
			try {
				String currentMd5 = RMFileUtil.getMd5ByFile(file);
				if ((!RMStringUtils.isEmpty(lastMd5))
						&& (!RMStringUtils.isEmpty(currentMd5))
						&& (!currentMd5.equals(lastMd5))) {
					isFileUpdate = loadMgn1File(RMFileUtil.getFingerDir()
							+ buildID, buildID, lastMd5);
				}
			} catch (Exception e) {
			}
		}
		return isFileUpdate;
	}

	/**
	 * 更新beacon指纹文件bbp2
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @return 是否下载成功
	 */
	public static boolean updateBeaconBbp2File(final String buildID) {
		boolean isFileUpdate = false;
		File file = new File(RMFileUtil.getFingerDir() + buildID + "/"
				+ buildID + ".bbp2");
		String lastMd5 = getFileMD5(buildID, "", RMFileUtil.BEACON_LOC_FILE_EXT);
		if (!file.exists()) {
			isFileUpdate = loadbbp2File(RMFileUtil.getFingerDir() + buildID,
					buildID, lastMd5);
		} else {
			try {
				String currentMd5 = RMFileUtil.getMd5ByFile(file);
				if ((!RMStringUtils.isEmpty(lastMd5))
						&& (!RMStringUtils.isEmpty(currentMd5))
						&& (!currentMd5.equals(lastMd5))) {
					isFileUpdate = loadbbp2File(RMFileUtil.getFingerDir()
							+ buildID, buildID, lastMd5);
				}
			} catch (Exception e) {
			}
		}
		return isFileUpdate;
	}

	/**
	 * 更新地图约束文件
	 * 
	 * @param buildID
	 * @return
	 */
	public static boolean updateMapMatchFile(final String buildID) {
		boolean isFileUpdate = false;
		File file = new File(RMFileUtil.getMapDir() + buildID + "_"
				+ RMFileUtil.MAP_MATCH_FILE_FLOOR_CODE + ".zip");
		String lastMd5 = getFileMD5(buildID,
				RMFileUtil.MAP_MATCH_FILE_FLOOR_CODE, RMFileUtil.EXT_ZIP);
		if (!file.exists()) {
			isFileUpdate = loadMapMatchFile(RMFileUtil.getMapDir(), buildID,
					lastMd5);
		} else {
			try {
				String currentMd5 = RMFileUtil.getMd5ByFile(file);
				if ((!RMStringUtils.isEmpty(lastMd5))
						&& (!RMStringUtils.isEmpty(currentMd5))
						&& (!currentMd5.equals(lastMd5))) {
					isFileUpdate = loadMapMatchFile(RMFileUtil.getMapDir(),
							buildID, lastMd5);
				}
			} catch (Exception e) {
			}
		}
		return isFileUpdate;
	}

	/**
	 * 联网拿到文件的得到文件MD5
	 * 
	 * @param buildID
	 *            建筑物ID
	 * @param floorID
	 *            楼层
	 * @param ext
	 *            扩展名,根据扩展名不同，拿到不同文件的MD5
	 * @return
	 */
	private static String getFileMD5(String buildID, String floorID, String ext) {
		String md5 = "";
		if ((!buildID.equals("")) && (!ext.equals(""))) {
			String params = "buildId=" + buildID;
			if (!floorID.equals("")) {
				params += "&floorId=" + floorID + "&extension=" + ext;
			} else {
				params += "&extension=" + ext;
			}
			String retJson = post(RMHttpUrl.FILE_INFO_URL, params);
			if (retJson.contains("\"status\":\"0\"")) {
				md5 = "";
			} else {
				try {
					JSONObject jObject = new JSONObject(retJson);
					String bid = jObject.getString("buildingId");
					if (bid.equals(buildID)) {
						md5 = jObject.getString("md5");
					}
				} catch (JSONException e) {
				}
			}
		}
		return md5;
	}

	

	/**
	 * @param strURL
	 * @param params
	 * @return
	 */
	private static String post(String strURL, String params) {
		String ret = "";
		try {
			URL url = new URL(strURL);// 创建连接
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setConnectTimeout(RMHttpUtil.TIME_OUT);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST"); // 设置请求方式
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream(), "UTF-8"); // utf-8编码
			out.write(params);
			out.flush();
			out.close();
			// 读取响应
			if (connection.getResponseCode() == 200) {
				InputStream is = connection.getInputStream();
				return convertStreamToString(is);
			}
		} catch (IOException e) {
			RMLog.w(TAG, "url: "+strURL+"\n"+ e.getMessage());
		}
		return ret;
	}
	
	/**
	 * 将InputStream转换成某种字符编码的String
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public synchronized static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		RMLog.i(TAG, "result : " + sb.toString());
		return sb.toString();
	}


	/**
	 * 下载wifi建筑物判断文件
	 * 
	 * @param md5
	 * @return
	 */
	private static boolean loadWifiBuildJudgeFile(String md5) {
		boolean isUploadSucess = false;
		try {
			String params = "buildId=" + RMFileUtil.CHINA_BUILD_CODE
					+ "&floorId=" + RMFileUtil.WIFI_BUILD_JUDGE_FILE_FLOOR_CODE
					+ "&extension=" + RMFileUtil.EXT_ZIP;
			String retJson = post(RMHttpUrl.DOWNLOAD_URL, params);
			if (retJson.contains("\"status\":\"1\"")) {
				String url = new JSONObject(retJson).getString("url");
				if (!RMStringUtils.isEmpty(url)) {
					String loadFileAbsolutePath = RMFileUtil.getBuildJudgeDir()
							+ RMFileUtil.CHINA_BUILD_CODE + "_"
							+ RMFileUtil.WIFI_BUILD_JUDGE_FILE_FLOOR_CODE
							+ ".zip.tmp";
					File f = new File(loadFileAbsolutePath);
					f.delete();
					RMHttpUtil.downloadFile(url, loadFileAbsolutePath);
					String loadFileMd5 = RMFileUtil.getMd5ByFile(new File(
							loadFileAbsolutePath));
					if (!RMStringUtils.isEmpty(loadFileMd5)
							&& (!RMStringUtils.isEmpty(md5))) {
						if (loadFileMd5.equals(md5)) {
							String hisFileAbsolutePath = RMFileUtil
									.getBuildJudgeDir()
									+ RMFileUtil.CHINA_BUILD_CODE
									+ "_"
									+ RMFileUtil.WIFI_BUILD_JUDGE_FILE_FLOOR_CODE
									+ ".zip";
							// 删除历史zip文件
							RMFileUtil.deleteFile(hisFileAbsolutePath);
							// 删除all_build_mac.bbs文件
							RMFileUtil.deleteFile(RMFileUtil.getBuildJudgeDir()
									+ RMFileUtil.BUILD_JUDGE_FILE_NAME);
							// 重命名下载文件
							File loadFile = new File(loadFileAbsolutePath);
							loadFile.renameTo(new File(hisFileAbsolutePath));
							// 解压下载文件
							ZipUtils.upZipFile(new File(hisFileAbsolutePath),
									RMFileUtil.getBuildJudgeDir());
							isUploadSucess = true;
						} else {
							// 下载完的文件md5值和接口给出的md5不一致
							RMFileUtil.deleteFile(loadFileAbsolutePath);
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isUploadSucess;
	}

	/**
	 * 下载beacon建筑物判断文件
	 * 
	 * @param md5
	 * @return
	 */
	private static boolean loadBeaconBuildJudgeFile(String md5) {
		boolean isUploadSucess = false;
		try {
			String params = "buildId=" + RMFileUtil.CHINA_BUILD_CODE
					+ "&floorId="
					+ RMFileUtil.BEACON_BUILD_JUDGE_FILE_FLOOR_CODE
					+ "&extension=" + RMFileUtil.EXT_ZIP;
			String retJson = post(RMHttpUrl.DOWNLOAD_URL, params);
			if (retJson.contains("\"status\":\"1\"")) {
				String url = new JSONObject(retJson).getString("url");
				if (!RMStringUtils.isEmpty(url)) {
					String loadFileAbsolutePath = RMFileUtil.getBuildJudgeDir()
							+ RMFileUtil.CHINA_BUILD_CODE + "_"
							+ RMFileUtil.BEACON_BUILD_JUDGE_FILE_FLOOR_CODE
							+ ".zip.tmp";
					File f = new File(loadFileAbsolutePath);
					f.delete();
					RMHttpUtil.downloadFile(url, loadFileAbsolutePath);
					String loadFileMd5 = RMFileUtil.getMd5ByFile(new File(
							loadFileAbsolutePath));
					if (!RMStringUtils.isEmpty(loadFileMd5)
							&& (!RMStringUtils.isEmpty(md5))) {
						if (loadFileMd5.equals(md5)) {
							String hisFileAbsolutePath = RMFileUtil
									.getBuildJudgeDir()
									+ RMFileUtil.CHINA_BUILD_CODE
									+ "_"
									+ RMFileUtil.BEACON_BUILD_JUDGE_FILE_FLOOR_CODE
									+ ".zip";
							// 删除历史zip文件
							RMFileUtil.deleteFile(hisFileAbsolutePath);
							// 删除XML文件
							RMFileUtil.deleteAllFile(
									RMFileUtil.getBuildJudgeDir(), ".xml");
							// 重命名下载文件
							File loadFile = new File(loadFileAbsolutePath);
							loadFile.renameTo(new File(hisFileAbsolutePath));
							// 解压下载文件
							ZipUtils.upZipFile(new File(hisFileAbsolutePath),
									RMFileUtil.getBuildJudgeDir());
							isUploadSucess = true;
						} else {
							// 下载完的文件md5值和接口给出的md5不一致
							RMFileUtil.deleteFile(loadFileAbsolutePath);
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isUploadSucess;
	}

	/**
	 * 下载bfp3文件
	 * 
	 * @param path
	 * @param buildID
	 * @param md5
	 * @return
	 */
	private static boolean loadBfp3File(String path, String buildID, String md5) {
		boolean isUploadSucess = false;
		try {
			String params = "buildId=" + buildID + "&extension=bfp3";
			String retJson = post(RMHttpUrl.DOWNLOAD_URL, params);
			if (retJson.contains("\"status\":\"1\"")) {
				String url = new JSONObject(retJson).getString("url");
				if (!RMStringUtils.isEmpty(url)) {
					RMFileUtil.createPath(RMFileUtil.getFingerDir() + buildID);
					RMHttpUtil.downloadFile(url, path + "/" + buildID + ".bfp3.tmp");
					String loadFileMd5 = RMFileUtil.getMd5ByFile(new File(path
							+ "/" + buildID + ".bfp3.tmp"));
					if (!RMStringUtils.isEmpty(loadFileMd5)
							&& (!RMStringUtils.isEmpty(md5))) {
						if (loadFileMd5.equals(md5)) {
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".bfp3");
							File loadFile = new File(path + "/" + buildID
									+ ".bfp3.tmp");
							loadFile.renameTo(new File(path + "/" + buildID
									+ ".bfp3"));
							isUploadSucess = true;
						} else {
							// 下载完的文件md5值和接口给出的md5不一致
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".bfp3.tmp");
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isUploadSucess;
	}

	/**
	 * 下载文件
	 * 
	 * @param path
	 * @param buildID
	 * @param md5
	 * @return
	 */
	private static boolean loadMgn1File(String path, String buildID, String md5) {
		boolean isUploadSucess = false;
		try {
			String params = "buildId=" + buildID + "&extension=mgn1";
			String retJson = post(RMHttpUrl.DOWNLOAD_URL, params);
			if (retJson.contains("\"status\":\"1\"")) {
				String url = new JSONObject(retJson).getString("url");
				if (!RMStringUtils.isEmpty(url)) {
					RMFileUtil.createPath(RMFileUtil.getFingerDir() + buildID);
					RMHttpUtil.downloadFile(url, path + "/" + buildID + ".mgn1.tmp");
					String loadFileMd5 = RMFileUtil.getMd5ByFile(new File(path
							+ "/" + buildID + ".mgn1.tmp"));
					if (!RMStringUtils.isEmpty(loadFileMd5)
							&& (!RMStringUtils.isEmpty(md5))) {
						if (loadFileMd5.equals(md5)) {
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".mgn1");
							File loadFile = new File(path + "/" + buildID
									+ ".mgn1.tmp");
							loadFile.renameTo(new File(path + "/" + buildID
									+ ".mgn1"));
							isUploadSucess = true;
						} else {
							// 下载完的文件md5值和接口给出的md5不一致
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".mgn1.tmp");
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isUploadSucess;
	}

	/**
	 * 下载bbp2文件
	 * 
	 * @param path
	 * @param buildID
	 * @param md5
	 * @return
	 */
	private static boolean loadbbp2File(String path, String buildID, String md5) {
		boolean isUploadSucess = false;
		try {
			String params = "buildId=" + buildID + "&extension=bbp2";
			String retJson = post(RMHttpUrl.DOWNLOAD_URL, params);
			if (retJson.contains("\"status\":\"1\"")) {
				String url = new JSONObject(retJson).getString("url");
				if (!RMStringUtils.isEmpty(url)) {
					RMFileUtil.createPath(RMFileUtil.getFingerDir() + buildID);
					RMHttpUtil.downloadFile(url, path + "/" + buildID + ".bbp2.tmp");
					String loadFileMd5 = RMFileUtil.getMd5ByFile(new File(path
							+ "/" + buildID + ".bbp2.tmp"));
					if (!RMStringUtils.isEmpty(loadFileMd5)
							&& (!RMStringUtils.isEmpty(md5))) {
						if (loadFileMd5.equals(md5)) {
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".bbp2");
							File loadFile = new File(path + "/" + buildID
									+ ".bbp2.tmp");
							loadFile.renameTo(new File(path + "/" + buildID
									+ ".bbp2"));
							isUploadSucess = true;
						} else {
							// 下载完的文件md5值和接口给出的md5不一致
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".bbp2.tmp");
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isUploadSucess;
	}

	/**
	 * 下载地图约束文件
	 * 
	 * @param path
	 * @param buildID
	 * @param md5
	 * @return
	 */
	private static boolean loadMapMatchFile(String path, String buildID,
			String md5) {
		boolean isUploadSucess = false;
		try {
			String params = "buildId=" + buildID + "&extension"
					+ RMFileUtil.MAP_MATCH_FILE_FLOOR_CODE + "&extension="
					+ RMFileUtil.EXT_ZIP;
			String retJson = post(RMHttpUrl.DOWNLOAD_URL, params);
			if (retJson.contains("\"status\":\"1\"")) {
				String url = new JSONObject(retJson).getString("url");
				if (!RMStringUtils.isEmpty(url)) {
					String loadFileAbsolutePath = path + "/" + buildID + "_"
							+ RMFileUtil.MAP_MATCH_FILE_FLOOR_CODE + ".zip.tmp";
					File f = new File(loadFileAbsolutePath);
					f.delete();
					RMHttpUtil.downloadFile(url, loadFileAbsolutePath);
					String loadFileMd5 = RMFileUtil.getMd5ByFile(new File(
							loadFileAbsolutePath));
					if (!RMStringUtils.isEmpty(loadFileMd5)
							&& (!RMStringUtils.isEmpty(md5))) {
						if (loadFileMd5.equals(md5)) {
							String hisFileAbsolutePath = path + File.separator
									+ buildID + "_"
									+ RMFileUtil.MAP_MATCH_FILE_FLOOR_CODE
									+ ".zip";
							// 删除历史zip文件
							RMFileUtil.deleteFile(hisFileAbsolutePath);
							// 删除tpb文件
							RMFileUtil.deleteAllFile(RMFileUtil.getMapDir(),
									buildID, ".tpb");
							// 重命名下载文件
							File loadFile = new File(loadFileAbsolutePath);
							loadFile.renameTo(new File(hisFileAbsolutePath));
							// 解压下载文件
							ZipUtils.upZipFile(new File(hisFileAbsolutePath),
									RMFileUtil.getMapDir());
							isUploadSucess = true;
						} else {
							// 下载完的文件md5值和接口给出的md5不一致
							RMFileUtil.deleteFile(path + "/" + buildID
									+ ".zip.tmp");
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return isUploadSucess;
	}

}
