package com.rtmap.wifipicker.adapter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.utils.RMD5Util;
import com.rtm.frm.model.Floor;
import com.rtm.frm.utils.OnMapDownLoadFinishListener;
import com.rtm.frm.utils.RMDownLoadMapUtil;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.DTAsyncTask;
import com.rtmap.wifipicker.core.DTCallBack;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.http.WPHttpUrl;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.rtmap.wifipicker.util.DownloadTask;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.NetworkService;
import com.rtmap.wifipicker.util.WebCommunication;
import com.rtmap.wifipicker.util.ZipUtils;

public class WPMapListNewAdapter extends BaseExpandableListAdapter implements
		OnClickListener {

	private Floor mFloor;
	private Activity mActivity;
	private ArrayList<String> mTitleList;
	private ArrayList<ArrayList<Floor>> mFloorList;
	private ProgressDialog mLoadDialog;// 加载框
	private static String ROOT_PATH;
	private boolean isDownbitmap;

	public WPMapListNewAdapter(Activity activity) {
		mActivity = activity;
		mTitleList = new ArrayList<String>();
		mFloorList = new ArrayList<ArrayList<Floor>>();
		initLoad(activity);
		String mUserName = WPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, "");
		ROOT_PATH = Constants.WIFI_PICKER_PATH + mUserName + "/";
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
	}

	@Override
	public void onClick(View v) {
		final Floor info = (Floor) v.getTag();
		switch (v.getId()) {
		case R.id.button_download:// 下载
			FileHelper.checkDir(Constants.MAP_DATA);
			final String root = ROOT_PATH + info.getBuildid() + "/";
			FileHelper.checkDir(root);
			final String vector_path = Constants.MAP_DATA
					+ RMD5Util.md5(info.getBuildid() + "_" + info.getFloor()
							+ ".imap");// /mnt/sdcard/rtmap/mdata/MD5(860100010040500002_F2.imap)
			final String bitmap_path = root + info.getBuildid() + "-"
					+ info.getFloor() + "-0.jpg";
			DTLog.e("矢量图：" + vector_path);
			DTLog.e("位图：" + bitmap_path);
			if (FileHelper.checkFile(vector_path)
					&& FileHelper.checkFile(bitmap_path)) {
			} else {
				mLoadDialog.setMessage("下载中..");
				mLoadDialog.show();
				if (!FileHelper.checkFile(vector_path)) {
					RMDownLoadMapUtil.downLoadMap("2cN2gvWIKP",
							info.getBuildid(), info.getFloor(),
							new OnMapDownLoadFinishListener() {

								@Override
								public void OnMapDownLoadFinish() {
									notifyDataSetChanged();
									mLoadDialog.cancel();
								}
							});
				}
				if (!FileHelper.checkFile(bitmap_path)) {
					final String bitmap_url = String.format(
							WPHttpUrl.MAP_DOWNLOAD_URL,
							WPApplication.getInstance().getShare()
									.getString(DTFileUtils.PREFS_TOKEN, ""),
							info.getFloor(), info.getBuildid(), 1);
					if (!isDownbitmap)
						isDownbitmap = !isDownbitmap;
					else
						return;
					NetworkService.downloadFile(bitmap_url, bitmap_path, null,
							new DownloadTask.OnDownloadTaskCompleteListener() {
								@Override
								public void onDownloadTaskComplete(
										Boolean result) {
									isDownbitmap = false;
									notifyDataSetChanged();
									mLoadDialog.cancel();
									if (!FileHelper.checkFile(bitmap_path)) {
										DTUIUtils.showToastSafe("位图下载失败");
									}
								}
							});
				}
			}
			break;
		case R.id.button_commit:// 上传
			mFloor = info;
			mLoadDialog.setMessage("上传中...");
			mLoadDialog.show();
			new DTAsyncTask(new UpdaLoadFileCall()).run();// 上传
			break;
		}
	}

	/**
	 * 添加组
	 * 
	 * @param list
	 */
	public void addGroupList(ArrayList<String> list) {
		mTitleList.addAll(list);
	}

	public void addGroup(String group) {
		mTitleList.add(group);
	}

	/**
	 * 添加分组list
	 * 
	 * @param list
	 */
	public void addChildList(ArrayList<Floor> list) {
		mFloorList.add(list);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public Floor getChild(int groupPosition, int childPosition) {
		return mFloorList.get(groupPosition).get(childPosition);
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
			convertView = DTUIUtils.inflate(R.layout.item_map);
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

		final Floor info = mFloorList.get(groupPosition).get(childPosition);
		holder.mTextMap.setText(info.getFloor());

		final String root = ROOT_PATH + info.getBuildid() + "/";
		FileHelper.checkDir(root);
		FileHelper.checkDir(Constants.MAP_DATA);
		String vector_path = Constants.MAP_DATA
				+ RMD5Util.md5(info.getBuildid() + "_" + info.getFloor()
						+ ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
		String bitmap_path = root + info.getBuildid() + "-" + info.getFloor()
				+ "-0.jpg";
		String result = "";
		if (FileHelper.checkFile(vector_path)
				&& FileHelper.checkFile(bitmap_path)) {
			result += "有矢量图有位图";
			holder.mDownLoad.setVisibility(View.GONE);
		} else {
			holder.mDownLoad.setVisibility(View.VISIBLE);
			if (FileHelper.checkFile(vector_path)) {
				result += "只有矢量图";
			} else if (FileHelper.checkFile(bitmap_path)) {
				result += "只有位图";
			}
		}
		holder.mStatus.setText(result);// 设置图的状态

		// 判断是否有当前楼层的指纹点数据存在
		holder.mCommit.setVisibility(View.GONE);
		String[] files = FileHelper.listFiles(root, new FilenameFilter() {// 只需要上传.walk1文件

					@Override
					public boolean accept(File dir, String filename) {
						if ((filename.contains(info.getBuildid() + "-"
								+ info.getFloor() + "-") || filename
									.contains(info.getBuildid() + "-"
											+ info.getFloor() + "_"))
								&& (filename.endsWith(".walk1")||filename.endsWith(".sensor")
										|| filename.endsWith(".mc")
										|| filename.endsWith(".door")
										|| filename.endsWith(".poi") || filename
											.endsWith("mapcorrection.jpg"))) {
							return true;
						}
						if (filename.contains(info.getBuildid() + "-"
								+ info.getFloor() + ".poi")) {
							return true;
						}
						if (filename.contains(info.getBuildid() + "-"
								+ info.getFloor() + ".door")) {
							return true;
						}
						return false;
					}
				});
		if (files != null && files.length > 0) {
			holder.mCommit.setVisibility(View.VISIBLE);
		}
		holder.mDownLoad.setTag(info);
		holder.mDownLoad.setOnClickListener(this);
		holder.mCommit.setTag(info);
		holder.mCommit.setOnClickListener(this);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mFloorList.get(groupPosition).size();
	}

	@Override
	public String getGroup(int groupPosition) {
		return mTitleList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mTitleList.size();
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
			convertView = DTUIUtils.inflate(R.layout.map_list_item);
			holder = new ViewHolder();
			holder.mTextMap = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextMap.setText(mTitleList.get(groupPosition));
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

	/**
	 * 上传文件回调
	 * 
	 * @author dingtao
	 *
	 */
	class UpdaLoadFileCall implements DTCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			// info.name = "860100010040500002-F2-0"
			// 将包含此楼层的所有文件查找出来
			String[] mapFiles = FileHelper.listFiles(
					ROOT_PATH + mFloor.getBuildid() + "/",
					new FilenameFilter() {
						// 860100010040500002-F2*.*
						@Override
						public boolean accept(File dir, String filename) {
							if ((filename.contains(mFloor.getBuildid() + "-"
									+ mFloor.getFloor() + "-") || filename
										.contains(mFloor.getBuildid() + "-"
												+ mFloor.getFloor() + "_"))
									&& (filename.endsWith(".walk1")
											|| filename.endsWith(".sensor")
											|| filename.endsWith(".mc")
											|| filename.endsWith(".door")
											|| filename.endsWith(".poi") || filename
												.endsWith("mapcorrection.jpg"))) {
								return true;
							}
							if (filename.contains(mFloor.getBuildid() + "-"
									+ mFloor.getFloor() + ".poi")) {
								return true;
							}
							if (filename.contains(mFloor.getBuildid() + "-"
									+ mFloor.getFloor() + ".door")) {
								return true;
							}
							return false;
						}
					});

			String result = null;
			if (mapFiles != null && mapFiles.length > 0) {// 有上传的文件

				ArrayList<File> filesBeforeZip = new ArrayList<File>();
				for (int i = 0; i < mapFiles.length; i++) {
					DTLog.e("filename : " + ROOT_PATH + mFloor.getBuildid()
							+ "/" + mapFiles[i]);
					filesBeforeZip.add(new File(ROOT_PATH + mFloor.getBuildid()
							+ "/" + mapFiles[i]));
				}
				String zipPath = ROOT_PATH + mFloor.getBuildid() + "/"
						+ mFloor.getBuildid() + "-" + mFloor.getFloor() + "-0_"
						+ System.currentTimeMillis() + ".zip";// 压缩后文件的名字.zip
				File zipFile = new File(zipPath);
				try {
					ZipUtils.zipFiles(filesBeforeZip, zipFile);

					result = uploadZipFile(zipFile.getAbsolutePath(),
							mFloor.getBuildid());
					DTLog.e(result);
					zipFile.delete();// 不管是否成功删除压缩包
				} catch (IOException e) {
					zipFile.delete();
					e.printStackTrace();
				}
			}
			return result;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mLoadDialog.cancel();
			if (obj != null) {// 返回值为1，上传成功
				JSONObject json;
				try {
					json = new JSONObject((String) obj);
					if (json.getInt("code") == 0) {
						Toast.makeText(mActivity, "上传成功", Toast.LENGTH_LONG)
								.show();
					} else {
						Toast.makeText(mActivity, "上传失败", Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(mActivity, "上传失败", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	private String uploadZipFile(String path, String buildId) {
		WebCommunication cm = new WebCommunication();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("submit", "");
		String url = String.format(WPHttpUrl.URL_UPLOAD_ZIP, WPApplication
				.getInstance().getShare()
				.getString(DTFileUtils.PREFS_TOKEN, ""));
		DTLog.e(url);
		String result = cm.uploadFile(url, params, path, "uploadedfile");
		DTLog.e(result);
		return result;
	}

}
