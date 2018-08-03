package com.rtmap.locationcheck.adapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.rtm.common.utils.RMD5Util;
import com.rtm.frm.utils.OnMapDownLoadFinishListener;
import com.rtm.frm.utils.RMDownLoadMapUtil;
import com.rtm.location.utils.ZipUtils;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.LCSqlite;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
import com.rtmap.locationcheck.core.model.Build;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.FloorList;
import com.rtmap.locationcheck.page.LCPickBuildActivity;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCMapListNewAdapter extends BaseExpandableListAdapter implements
		OnClickListener {

	private Dialog mUploadDialog;// 间隔dialog
	private Floor mFloor;
	private Activity mActivity;
	private ArrayList<Build> mFloorList;
	private ProgressDialog mLoadDialog;// 加载框
	private Build mBuild;

	public LCMapListNewAdapter(Activity activity) {
		mActivity = activity;
		mFloorList = new ArrayList<Build>();
		initUploadDialog();
		initLoad(activity);
	}

	/**
	 * 初始化加载框
	 */
	private void initLoad(Context context) {
		mLoadDialog = new ProgressDialog(context);// 加载框
		mLoadDialog.setMessage(context.getString(R.string.map_download_start));
		mLoadDialog.setCanceledOnTouchOutside(false);
	}

	static class ViewHolder {
		TextView mTextMap;
		TextView mDownLoad;
		TextView mCommit;
		TextView mStatus;
		TextView pick;
		ImageView sign;
	}

	/**
	 * show弹出框
	 */
	private void initUploadDialog() {
		mUploadDialog = new Dialog(mActivity, R.style.dialog);
		mUploadDialog.setContentView(R.layout.dialog_map_layout);
		mUploadDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mUploadDialog
				.findViewById(R.id.set_list);
		String[] interDate = mActivity.getResources().getStringArray(
				R.array.upload_dialog);
		mInterList.setAdapter(new LCMapDialogAdapter(mActivity, interDate));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mUploadDialog.cancel();
			}
		});
	}

	private void uploadMapFinger() throws LCException, JSONException {
		// info.name = "860100010040500002-F2-0"
		// 将包含此楼层的所有文件查找出来
		String[] mapFiles = DTFileUtils.listFiles(DTFileUtils.getDataDir()
				+ mFloor.getBuildId() + File.separator, new FilenameFilter() {
			// 860100010040500002-F2*.*
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + "-")
						&& (filename.endsWith(".walk1") || filename
								.endsWith(".sensor"))) {
					return true;
				}
				if (filename.contains(mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + "_")
						&& filename.endsWith(".json")) {
					return true;
				}
				return false;
			}
		});

		String result = null;
		if (mapFiles != null && mapFiles.length > 0) {// 有上传的文件

			ArrayList<File> filesBeforeZip = new ArrayList<File>();
			for (int i = 0; i < mapFiles.length; i++) {
				DTLog.e("filename : " + DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + File.separator + mapFiles[i]);
				filesBeforeZip.add(new File(DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + File.separator + mapFiles[i]));
			}
			String zipPath = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + mFloor.getBuildId() + "-"
					+ mFloor.getFloor() + "-0_finger_"
					+ System.currentTimeMillis() + ".zip";// 压缩后文件的名字.zip
			File zipFile = new File(zipPath);
			try {
				ZipUtils.zipFiles(filesBeforeZip, zipFile);

				String url = String.format(
						LCHttpUrl.FINGER_INFO,
						LCApplication.getInstance().getShare()
								.getString(DTFileUtils.PREFS_TOKEN, ""));

				String result1 = LCHttpClient.postUpFile(url, zipFile);

				JSONObject json = new JSONObject(result1);
				if ("1".equals(json.getString("status"))) {
					DTUIUtils.showToastSafe("指纹数据上传成功");
					for (File file : filesBeforeZip) {
						file.delete();
					}
					String destPath = DTFileUtils.getBackupDir()
							+ mFloor.getBuildId() + File.separator
							+ mFloor.getBuildId() + "-" + mFloor.getFloor()
							+ "-0_finger_" + System.currentTimeMillis()
							+ ".zip";// 压缩后文件的名字.zip
					DTFileUtils.copyFile(zipFile.getAbsolutePath(), destPath,
							true);
				} else {
					DTUIUtils.showToastSafe("指纹数据上传失败");
				}
			} catch (IOException e) {
				zipFile.delete();
				e.printStackTrace();
			}
		}
	}

	private void uploadMapPick() throws LCException, JSONException {
		// info.name = "860100010040500002-F2-0"
		// 将包含此楼层的所有文件查找出来
		String[] mapFiles = DTFileUtils.listFiles(DTFileUtils.getDataDir()
				+ mFloor.getBuildId() + File.separator, new FilenameFilter() {
			// 860100010040500002-F2*.*
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + "_")
						&& filename.endsWith(".mc")) {
					return true;
				}
				if (filename.equals(mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + ".door")) {
					return true;
				}
				if (filename.equals(mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + ".poi")) {
					return true;
				}
				if (filename.contains(mFloor.getBuildId() + "_"
						+ mFloor.getFloor() + "_poi_")
						&& filename.endsWith(".jpg")) {
					return true;
				}
				return false;
			}
		});
		String result = null;
		if (mapFiles != null && mapFiles.length > 0) {// 有上传的文件

			ArrayList<File> filesBeforeZip = new ArrayList<File>();
			for (int i = 0; i < mapFiles.length; i++) {
				DTLog.e("filename : " + DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + File.separator + mapFiles[i]);
				filesBeforeZip.add(new File(DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + File.separator + mapFiles[i]));
			}
			String zipPath = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + mFloor.getBuildId() + "-"
					+ mFloor.getFloor() + "-0_pick_"
					+ System.currentTimeMillis() + ".zip";// 压缩后文件的名字.zip
			File zipFile = new File(zipPath);
			try {
				ZipUtils.zipFiles(filesBeforeZip, zipFile);

				String url = String.format(
						LCHttpUrl.PICK_INFO,
						LCApplication.getInstance().getShare()
								.getString(DTFileUtils.PREFS_TOKEN, ""));

				String result1 = LCHttpClient.postUpFile(url, zipFile);

				JSONObject json = new JSONObject(result1);
				if ("1".equals(json.getString("status"))) {
					DTUIUtils.showToastSafe("采集数据上传成功");
					for (File file : filesBeforeZip) {
						if (file.getPath().endsWith(".door")) {
							file.renameTo(new File(DTFileUtils.getDataDir()
									+ mFloor.getBuildId() + File.separator
									+ mFloor.getBuildId() + "-"
									+ mFloor.getFloor() + "_"
									+ System.currentTimeMillis()
									+ ".door_upload"));
						} else if (file.getPath().endsWith(".mc")) {
							file.renameTo(new File(file.getPath() + "_upload"));
						} else if (file.getPath().endsWith(".poi")) {
							file.renameTo(new File(DTFileUtils.getDataDir()
									+ mFloor.getBuildId() + File.separator
									+ mFloor.getBuildId() + "-"
									+ mFloor.getFloor() + "_"
									+ System.currentTimeMillis()
									+ ".poi_upload"));
						} else if (file.getPath().endsWith(".jpg")) {
							file.renameTo(new File(file.getPath() + "_upload"));
						} else {
							file.delete();
						}
					}
					String destPath = DTFileUtils.getBackupDir()
							+ mFloor.getBuildId() + File.separator
							+ mFloor.getBuildId() + "-" + mFloor.getFloor()
							+ "-0_pick_" + System.currentTimeMillis() + ".zip";// 压缩后文件的名字.zip
					DTFileUtils.copyFile(zipFile.getAbsolutePath(), destPath,
							true);
				} else {
					DTUIUtils.showToastSafe("采集数据上传失败");
				}
			} catch (IOException e) {
				zipFile.delete();
				e.printStackTrace();
			}
		}
	}

	/**
	 * 上传精度测试文件
	 * 
	 * @throws IOException
	 * @throws LCException
	 */
	private void uploadCheck() throws IOException, LCException {
		FilenameFilter filter = new FilenameFilter() {
			// 860100010040500002-F2*.*
			@Override
			public boolean accept(File dir, String filename) {
				if (filename
						.contains(mFloor.getBuildId()
								+ "_"
								+ DTStringUtils.floorTransform(mFloor
										.getFloor()) + "_")
						&& (filename.endsWith(".lcrpt1") || filename
								.endsWith(".off"))) {
					return true;
				}
				return false;
			}
		};
		String[] filePaths = new File(DTFileUtils.getDataDir()
				+ mFloor.getBuildId() + File.separator).list(filter);
		ArrayList<File> list = new ArrayList<File>();
		if (filePaths.length == 0)
			return;
		for (String path : filePaths) {
			list.add(new File(DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + path));
		}
		String zipPath = DTFileUtils.getDataDir() + mFloor.getBuildId()
				+ File.separator + mFloor.getBuildId() + "_"
				+ mFloor.getFloor() + ".zip";// 压缩后文件的名字.zip
		File zipFile = new File(zipPath);
		DTFileUtils.zipFiles(list, zipFile);

		String result1 = LCHttpClient.postUpFile(
				String.format(
						LCHttpUrl.UPLOAD_DATA,
						LCApplication.getInstance().getShare()
								.getString(DTFileUtils.PREFS_TOKEN, ""),
						mFloor.getBuildId()), zipFile);
		if (result1 != null) {
			String destPath = DTFileUtils.getBackupDir() + mFloor.getBuildId()
					+ File.separator + mFloor.getBuildId() + "_"
					+ mFloor.getFloor() + "_check_"
					+ System.currentTimeMillis() + ".zip";// 压缩后文件的名字.zip
			DTFileUtils.copyFile(zipFile.getAbsolutePath(), destPath, true);
			for (File file : list) {
				file.delete();
			}
		}
	}

	/**
	 * 上传beacon文件
	 * 
	 * @throws IOException
	 * @throws LCException
	 * @throws SQLException
	 * @throws JSONException
	 */
	private void uploadBeacon() throws IOException, LCException, SQLException {
		Dao<BeaconInfo, String> mBeaconDao = LCSqlite.getInstance()
				.createBeaconTable(mFloor.getBuildId(), mFloor.getFloor());
		List<BeaconInfo> list = mBeaconDao.queryForAll();
		long time = System.currentTimeMillis();// 上传时间
		if (list != null && list.size() > 0) {
			String str = "";
			for (int i = 0; i < list.size(); i++) {
				BeaconInfo info = list.get(i);
				if (i == 0)
					str += "<aps>\n";
				str += "<ap><mac>" + info.getMac()
						+ "</mac><Threshold_switch_min>"
						+ info.getThreshold_switch_min()
						+ "</Threshold_switch_min><Threshold_switch_max>"
						+ info.getThreshold_switch_max()
						+ "</Threshold_switch_max><buildId>"
						+ info.getBuildId() + "</buildId><floor>"
						+ info.getFloor() + "</floor><x>" + info.getX()
						+ "</x><y>" + info.getY() + "</y><inshop>"
						+ info.getInshop() + "</inshop><finger>"
						+ info.getFinger() + "</finger><output_power>" + "-45"
						+ "</output_power><work_status>"
						+ info.getWork_status() + "</work_status><edit_status>"
						+ info.getEdit_status() + "</edit_status><time>" + time
						+ "</time><rssi_max>" + info.getRssi_max()
						+ "</rssi_max><uuid>" + info.getUuid()
						+ "</uuid><broadcast_id>" + info.getBroadcast_id()
						+ "</broadcast_id><major>" + info.getMajor()
						+ "</major><minor>" + info.getMinor() + "</minor></ap>";
				if (i == list.size() - 1)
					str += "</aps>";
			}

			String filepath = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + mFloor.getBuildId() + "_"
					+ mFloor.getFloor() + ".xml";
			File xmlfile = new File(filepath);
			if (!xmlfile.exists())
				xmlfile.createNewFile();
			BufferedWriter xmlbr = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(xmlfile), "utf-8"));
			xmlbr.write(str);
			xmlbr.flush();
			xmlbr.close();
			String beaconZipPath = DTFileUtils.getDataDir()
					+ mFloor.getBuildId() + File.separator
					+ mFloor.getBuildId() + "_" + mFloor.getFloor()
					+ "_beacon.zip";// 压缩后文件的名字.zip
			final File beaconzipFile = new File(beaconZipPath);
			DTFileUtils.zipFile(xmlfile, beaconzipFile);

			String result1 = LCHttpClient.postUpFile(String.format(
					LCHttpUrl.UPLOAD_BEACON, LCApplication.getInstance()
							.getShare().getString(DTFileUtils.PREFS_TOKEN, ""),
					mFloor.getBuildId(),
					DTStringUtils.floorTransform(mFloor.getFloor())),
					beaconzipFile);
			JSONObject json;
			try {
				json = new JSONObject(result1);
				if ("1".equals(json.getString("status"))) {
					xmlfile.delete();
					String destPath = DTFileUtils.getBackupDir()
							+ mFloor.getBuildId() + File.separator
							+ mFloor.getBuildId() + "_" + mFloor.getFloor()
							+ "_beacon_" + System.currentTimeMillis() + ".zip";
					DTFileUtils.copyFile(beaconzipFile.getAbsolutePath(),
							destPath, true);
					mBeaconDao.delete(list);
				} else {
					DTUIUtils.showToastSafe(json.getString("message"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.button_download:// 下载或者删除
			final Floor info = (Floor) v.getTag();
			DTFileUtils.createDirs(DTFileUtils.MAP_DATA);
			final String vector_path = DTFileUtils.MAP_DATA
					+ RMD5Util.md5(info.getBuildId() + "_" + info.getFloor()
							+ ".imap");
			final String bitmap_path = DTFileUtils.getImageDir()
					+ info.getBuildId() + "-" + info.getFloor() + ".jpg";
			if (DTFileUtils.checkFile(vector_path)
					|| DTFileUtils.checkFile(bitmap_path)) {
				AlertDialog.Builder build = new Builder(mActivity);
				build.setTitle("删除后可以下载新数据");
				build.setMessage("删除已存在的地图吗？");
				build.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								File vector = new File(vector_path);
								File bitmap = new File(bitmap_path);
								if (vector.exists())
									vector.delete();
								if (bitmap.exists())
									bitmap.delete();
								notifyDataSetChanged();
							}
						});
				build.setNegativeButton("取消", null);
				build.create().show();
			} else {
				if (!DTFileUtils.checkFile(vector_path)) {
					mLoadDialog.setMessage("下载中..");
					mLoadDialog.show();
					RMDownLoadMapUtil.downLoadMap("IjccjkPVWv",
							info.getBuildId(), info.getFloor(),
							new OnMapDownLoadFinishListener() {

								@Override
								public void OnMapDownLoadFinish() {
									mLoadDialog.cancel();
									// /mnt/sdcard/rtmap/mdata/MD5(860100010040500002_F2.imap)
									File file = new File(vector_path);
									if (file.exists())
										DTUIUtils
												.showToastSafe(R.string.map_download_success_vector);
									notifyDataSetChanged();
								}
							});
				}

				if (!DTFileUtils.checkFile(bitmap_path)) {
					String bitmap_url = String.format(
							LCHttpUrl.MAP_DOWNLOAD_URL,
							LCApplication.getInstance().getShare()
									.getString(DTFileUtils.PREFS_TOKEN, ""),
							info.getFloor(), info.getBuildId(), 1);
					mFloor = info;
					new LCAsyncTask(new DownLoadMapCall()).run(bitmap_path,
							bitmap_url);
				}
			}
			break;
		case R.id.button_commit:// 上传
			final Floor info1 = (Floor) v.getTag();
			mFloor = info1;
			// mUploadDialog.show();
			mLoadDialog.setMessage("上传中...");
			mLoadDialog.show();
			new LCAsyncTask(new LCCallBack() {

				@Override
				public Object onCallBackStart(Object... obj) {
					try {
						DTFileUtils.createDirs(DTFileUtils.getBackupDir()
								+ mFloor.getBuildId() + File.separator);
						uploadBeacon();
						uploadMapFinger();
						uploadCheck();
						uploadMapPick();
						return null;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (LCException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					DTUIUtils.showToastSafe("上传失败");
					return null;
				}

				@Override
				public void onCallBackFinish(Object obj) {
					notifyDataSetChanged();
					mLoadDialog.cancel();
				}
			}).run();
			// 上传
			break;
		case R.id.info:// 采集建筑物信息
			Build build = (Build) v.getTag();
			mBuild = build;
			Intent s = new Intent(mActivity, LCPickBuildActivity.class);
			Bundle b = new Bundle();
			b.putSerializable("build", mBuild);
			s.putExtras(b);
			mActivity.startActivity(s);
			break;
		}
	}

	/**
	 * 下载线程
	 * 
	 * @author dingtao
	 *
	 */
	class DownLoadMapCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			boolean status = LCHttpClient.downloadFile((String) obj[0],
					(String) obj[1]);
			return status;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if ((Boolean) obj)
				DTUIUtils.showToastSafe(R.string.map_download_success_bitmap);
			notifyDataSetChanged();
			new LCAsyncTask(new LCCallBack() {

				@Override
				public Object onCallBackStart(Object... obj) {
					try {
						String result = LCHttpClient.getOrDelete(
								LCHttpClient.GET,
								String.format(
										LCHttpUrl.FLOOR_INFO,
										LCApplication
												.getInstance()
												.getShare()
												.getString(
														DTFileUtils.PREFS_TOKEN,
														""), mFloor.getFloor(),
										mFloor.getBuildId()), null, null);
						Gson gson = new Gson();
						return gson.fromJson(result, FloorList.class);
					} catch (LCException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public void onCallBackFinish(Object obj) {
					mLoadDialog.cancel();
					if (obj != null) {
						FloorList list = (FloorList) obj;
						if (list.getResults() == null
								|| list.getResults().size() == 0) {
							DTUIUtils.showToastSafe("获取位图比例尺失败，请注销重开软件");
						} else {
							if (list.getResults().get(0).getScale() != 0) {
								Log.i("rtmap", "更新之后比例尺："
										+ list.getResults().get(0).getScale());
								mFloor.setScale(list.getResults().get(0)
										.getScale());
							} else {
								DTUIUtils.showToastSafe("位图比例尺为0，请联系内业修改");
							}
						}
					} else {
						DTUIUtils.showToastSafe("获取位图比例尺失败，请注销重开软件");
					}
				}

			}).run();
		}
	}

	/**
	 * 添加分组list
	 * 
	 * @param list
	 */
	public void addChildList(Build list) {
		mFloorList.add(list);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public Floor getChild(int groupPosition, int childPosition) {
		return mFloorList.get(groupPosition).getScale().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtils.inflate(R.layout.map_list_item);
			holder = new ViewHolder();
			holder.mTextMap = (TextView) convertView
					.findViewById(R.id.text_map);
			holder.mDownLoad = (TextView) convertView
					.findViewById(R.id.button_download);
			holder.mCommit = (TextView) convertView
					.findViewById(R.id.button_commit);
			holder.mStatus = (TextView) convertView
					.findViewById(R.id.down_status);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Floor info = mFloorList.get(groupPosition).getScale()
				.get(childPosition);
		holder.mTextMap.setText(info.getFloor());

		DTFileUtils.createDirs(DTFileUtils.MAP_DATA);
		String vector_path = DTFileUtils.MAP_DATA
				+ RMD5Util.md5(info.getBuildId() + "_" + info.getFloor()
						+ ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
		String bitmap_path = DTFileUtils.getImageDir() + info.getBuildId()
				+ "-" + info.getFloor() + ".jpg";
		String result = "";
		if (DTFileUtils.checkFile(vector_path)
				|| DTFileUtils.checkFile(bitmap_path)) {
			holder.mDownLoad.setText(R.string.delete_map);
			if (!DTFileUtils.checkFile(vector_path)) {
				result = "有位图";
			} else if (!DTFileUtils.checkFile(bitmap_path)) {
				result = "有矢量图";
			} else {
				result = "有矢量图有位图";
			}
		} else {
			holder.mDownLoad.setText(R.string.download_map);
			result = "";
		}
		holder.mStatus.setText(result);// 设置图的状态
		holder.mDownLoad.setTag(info);
		holder.mDownLoad.setOnClickListener(this);

		String[] mapFiles = DTFileUtils.listFiles(DTFileUtils.getDataDir()
				+ info.getBuildId() + File.separator, new FilenameFilter() {
			// 860100010040500002-F2*.*
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(info.getBuildId() + "-" + info.getFloor()
						+ "_")
						&& filename.endsWith(".mc")) {
					return true;
				}
				if (filename.contains(info.getBuildId() + "-" + info.getFloor()
						+ "-")
						&& (filename.endsWith(".walk1") || filename
								.endsWith(".sensor"))) {
					return true;
				}
				if (filename.contains(info.getBuildId() + "_"
						+ DTStringUtils.floorTransform(info.getFloor()) + "_")
						&& (filename.endsWith(".lcrpt1") || filename
								.endsWith(".off"))) {
					return true;
				}
				if (filename.equals(info.getBuildId() + "_" + info.getFloor()
						+ ".txt")) {
					return true;
				}
				if (filename.equals(info.getBuildId() + "-" + info.getFloor()
						+ ".door")) {
					return true;
				}
				if (filename.equals(info.getBuildId() + "-" + info.getFloor()
						+ ".poi")) {
					return true;
				}
				return false;
			}
		});
		Dao<BeaconInfo, String> dao = LCSqlite.getInstance().createBeaconTable(
				info.getBuildId(), info.getFloor());
		try {
			DTLog.i("beacon数量：" + dao.countOf());
			if ((mapFiles != null && mapFiles.length > 0) || dao.countOf() > 0) {
				holder.mCommit.setVisibility(View.VISIBLE);
			} else {
				holder.mCommit.setVisibility(View.GONE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		holder.mCommit.setTag(info);
		holder.mCommit.setOnClickListener(this);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mFloorList.get(groupPosition).getScale().size();
	}

	@Override
	public Build getGroup(int groupPosition) {
		return mFloorList.get(groupPosition);
	}

	public ArrayList<Build> getFloorList() {
		return mFloorList;
	}

	public void setFloorList(ArrayList<Build> list) {
		mFloorList.addAll(list);
	}

	@Override
	public int getGroupCount() {
		return mFloorList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtils.inflate(R.layout.map_list_title_item);
			holder = new ViewHolder();
			holder.mTextMap = (TextView) convertView.findViewById(R.id.name);
			holder.mCommit = (TextView) convertView.findViewById(R.id.info);
			holder.sign = (ImageView) convertView.findViewById(R.id.red_sign);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextMap.setText(mFloorList.get(groupPosition).getBuildName());

		String[] mapFiles = DTFileUtils.listFiles(DTFileUtils.getDataDir()
				+ mFloorList.get(groupPosition).getBuildId() + File.separator,
				new FilenameFilter() {
					// 860100010040500002-F2*.*
					@Override
					public boolean accept(File dir, String filename) {
						if (filename.endsWith(".mc")
								|| filename.endsWith(".txt")
								|| filename.endsWith(".poi")
								|| filename.endsWith(".door")
								|| filename.endsWith(".jpg")) {
							return true;
						}
						if (filename.endsWith(".walk1")
								|| filename.endsWith(".sensor")) {
							return true;
						}
						if (filename.endsWith(".lcrpt1")
								|| filename.endsWith(".off")) {
							return true;
						}
						return false;
					}
				});
		if (mapFiles != null && mapFiles.length > 0) {
			holder.sign.setVisibility(View.VISIBLE);
		} else {
			holder.sign.setVisibility(View.GONE);
		}
		holder.mCommit.setTag(mFloorList.get(groupPosition));
		holder.mCommit.setOnClickListener(this);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
