package com.rtmap.locationcheck.adapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.utils.RMD5Util;
import com.rtm.frm.utils.OnMapDownLoadFinishListener;
import com.rtm.frm.utils.RMDownLoadMapUtil;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCMapListNewAdapter extends BaseExpandableListAdapter implements
		OnClickListener {

	private Dialog mUploadDialog;// 间隔dialog
	private Floor mFloor;
	private Activity mActivity;
	private ArrayList<String> mTitleList;
	private ArrayList<ArrayList<Floor>> mFloorList;
	private ProgressDialog mLoadDialog;// 加载框

	public LCMapListNewAdapter(Activity activity) {
		mActivity = activity;
		mTitleList = new ArrayList<String>();
		mFloorList = new ArrayList<ArrayList<Floor>>();
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
				mLoadDialog.setMessage("上传中...");
				mLoadDialog.show();
				switch (position) {
				case 0:
					new LCAsyncTask(new LCCallBack() {

						@Override
						public Object onCallBackStart(Object... obj) {
							try {
								File txtfile = new File(DTFileUtils
										.getDataDir()
										+ mFloor.getBuildId()
										+ "_" + mFloor.getFloor() + ".txt");
								if (!txtfile.exists()) {
									return null;
								}
								BufferedReader txtbr;
								txtbr = new BufferedReader(
										new InputStreamReader(
												new FileInputStream(txtfile),
												"utf-8"));

								String line, result = "";
								while ((line = txtbr.readLine()) != null) {
									// 将文本打印到控制台
									result += line;
								}
								txtbr.close();
								BeaconList mGsonList = null;
								Gson gson = new Gson();
								if (!DTStringUtils.isEmpty(result)) {
									mGsonList = gson.fromJson(result,
											BeaconList.class);
								}
								String str = "";
								long time = System.currentTimeMillis();
								if (mGsonList != null) {
									for (int i = 0; i < mGsonList.getList()
											.size(); i++) {
										BeaconInfo info = mGsonList.getList()
												.get(i);
										if (i == 0)
											str += "<aps>\n";
										str += "<ap><mac>"
												+ info.getMac()
												+ "</mac><Threshold_switch_min>"
												+ info.getThreshold_switch_min()
												+ "</Threshold_switch_min><Threshold_switch_max>"
												+ info.getThreshold_switch_max()
												+ "</Threshold_switch_max><buildId>"
												+ info.getBuildId()
												+ "</buildId><floor>"
												+ info.getFloor()
												+ "</floor><x>"
												+ info.getX()
												+ "</x><y>"
												+ info.getY()
												+ "</y><inshop>"
												+ info.getInshop()
												+ "</inshop><finger>"
												+ info.getFinger()
												+ "</finger><output_power>"
												+ "-45"
												+ "</output_power><work_status>"
												+ info.getWork_status()
												+ "</work_status><edit_status>"
												+ info.getEdit_status()
												+ "</edit_status><time>" + time
												+ "</time><rssi_max>"
												+ info.getRssi_max()
												+ "</rssi_max><uuid>"
												+ info.getUuid()
												+ "</uuid><broadcast_id>"
												+ info.getBroadcast_id()
												+ "</broadcast_id><major>"
												+ info.getMajor()
												+ "</major><minor>"
												+ info.getMinor()
												+ "</minor></ap>";
										if (i == mGsonList.getList().size() - 1)
											str += "</aps>";
									}
								}
								String filepath = DTFileUtils.getDataDir()
										+ mFloor.getBuildId() + "_"
										+ mFloor.getFloor() + ".xml";
								File xmlfile = new File(filepath);
								if (!xmlfile.exists())
									xmlfile.createNewFile();
								BufferedWriter xmlbr = new BufferedWriter(
										new OutputStreamWriter(
												new FileOutputStream(xmlfile),
												"utf-8"));
								xmlbr.write(str);
								xmlbr.flush();
								xmlbr.close();
								String beaconZipPath = DTFileUtils.getDataDir()
										+ mFloor.getBuildId() + "_"
										+ mFloor.getFloor() + "_beacon.zip";// 压缩后文件的名字.zip
								final File beaconzipFile = new File(
										beaconZipPath);
								DTFileUtils.zipFile(xmlfile, beaconzipFile);

								return LCHttpClient.postUpFile(
										String.format(
												LCHttpUrl.UPLOAD_BEACON,
												LCApplication
														.getInstance()
														.getShare()
														.getString(
																DTFileUtils.PREFS_TOKEN,
																""), mFloor
														.getBuildId(),
												DTStringUtils
														.floorTransform(mFloor
																.getFloor())),
										beaconzipFile);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (LCException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}

						@Override
						public void onCallBackFinish(Object obj) {
							mLoadDialog.cancel();
							if (obj != null) {
								String result = (String) obj;
								JSONObject json;
								try {
									json = new JSONObject(result);
									if ("1".equals(json.getString("status"))) {
										String filepath = DTFileUtils
												.getDataDir()
												+ mFloor.getBuildId()
												+ "_"
												+ mFloor.getFloor() + ".xml";
										String beaconZipPath = DTFileUtils
												.getDataDir()
												+ mFloor.getBuildId()
												+ "_"
												+ mFloor.getFloor()
												+ "_beacon.zip";// 压缩后文件的名字.zip
										File file = new File(filepath);
										file.delete();
										File f = new File(beaconZipPath);
										f.delete();

										File txtfile = new File(DTFileUtils
												.getDataDir()
												+ mFloor.getBuildId()
												+ "_"
												+ mFloor.getFloor() + ".txt");
										txtfile.delete();
										DTUIUtils
												.showToastSafe(R.string.upload_success);
									}
								} catch (JSONException e) {
									e.printStackTrace();
									DTUIUtils
											.showToastSafe(R.string.upload_fail);
								}
							} else {
								DTUIUtils.showToastSafe(R.string.upload_fail);
							}
						}
					}).run();
					break;
				case 1:
					new LCAsyncTask(new LCCallBack() {

						@Override
						public Object onCallBackStart(Object... obj) {
							try {
								FilenameFilter filter = new FilenameFilter() {
									// 860100010040500002-F2*.*
									@Override
									public boolean accept(File dir,
											String filename) {
										if (filename.contains(mFloor
												.getBuildId()
												+ "_"
												+ DTStringUtils
														.floorTransform(mFloor
																.getFloor())
												+ "_")
												&& (filename
														.endsWith(".lcrpt1") || filename
														.endsWith(".off"))) {
											return true;
										}
										return false;
									}
								};
								String[] filePaths = new File(DTFileUtils
										.getDataDir()).list(filter);
								ArrayList<File> list = new ArrayList<File>();
								if (filePaths.length == 0)
									return null;
								for (String path : filePaths) {
									list.add(new File(DTFileUtils.getDataDir()
											+ path));
								}
								String zipPath = DTFileUtils.getDataDir()
										+ mFloor.getBuildId() + "_"
										+ mFloor.getFloor() + ".zip";// 压缩后文件的名字.zip
								File zipFile = new File(zipPath);
								DTFileUtils.zipFiles(list, zipFile);

								return LCHttpClient.postUpFile(
										String.format(
												LCHttpUrl.UPLOAD_DATA,
												LCApplication
														.getInstance()
														.getShare()
														.getString(
																DTFileUtils.PREFS_TOKEN,
																""),
												mFloor.getBuildId()), zipFile);
							} catch (IOException e) {
								e.printStackTrace();
							} catch (LCException e) {
								e.printStackTrace();
							}
							return null;
						}

						@Override
						public void onCallBackFinish(Object obj) {
							mLoadDialog.cancel();
							if (obj != null) {
								String zipPath = DTFileUtils.getDataDir()
										+ mFloor.getBuildId() + "_"
										+ mFloor.getFloor() + ".zip";// 压缩后文件的名字.zip
								File zipFile = new File(zipPath);
								zipFile.delete();
								DTUIUtils
										.showToastSafe(R.string.upload_success);
							} else {
								DTUIUtils.showToastSafe(R.string.upload_fail);
							}
						}
					}).run();
					break;
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		final Floor info = (Floor) v.getTag();
		switch (v.getId()) {
		case R.id.button_download:// 下载
			DTFileUtils.createDirs(DTFileUtils.MAP_DATA);
			final String vector_path = DTFileUtils.MAP_DATA
					+ RMD5Util.md5(info.getBuildId() + "_" + info.getFloor()
							+ ".imap");
			String bitmap_path = DTFileUtils.getImageDir() + info.getBuildId()
					+ "-" + info.getFloor() + ".jpg";
			DTLog.e("矢量图：" + vector_path);

			if (!DTFileUtils.checkFile(vector_path)) {
				mLoadDialog.setMessage("下载中..");
				mLoadDialog.show();
				// new LCAsyncTask(new DownLoadMapCall()).run(
				// vector_path,
				// String.format(
				// LCHttpUrl.MAP_DOWNLOAD_URL,
				// LCApplication
				// .getInstance()
				// .getShare()
				// .getString(
				// DTFileUtils.PREFS_TOKEN,
				// ""), info.getFloor(),
				// info.getBuildId()));
				RMDownLoadMapUtil.downLoadMap("IjccjkPVWv", info.getBuildId(),
						info.getFloor(), new OnMapDownLoadFinishListener() {

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
				new LCAsyncTask(new DownLoadMapCall()).run(bitmap_path,bitmap_url);
			}
			break;
		case R.id.button_commit:// 上传
			mFloor = info;
			mUploadDialog.show();
			// 上传
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
			mLoadDialog.cancel();
			if ((Boolean) obj)
				DTUIUtils.showToastSafe(R.string.map_download_success_bitmap);
			notifyDataSetChanged();
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

		final Floor info = mFloorList.get(groupPosition).get(childPosition);
		holder.mTextMap.setText(info.getFloor());

		DTFileUtils.createDirs(DTFileUtils.MAP_DATA);
		String vector_path = DTFileUtils.MAP_DATA
				+ RMD5Util.md5(info.getBuildId() + "_" + info.getFloor()
						+ ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
		String bitmap_path = DTFileUtils.getImageDir() + info.getBuildId()
				+ "-" + info.getFloor() + ".jpg";
		String result = "";
		if (DTFileUtils.checkFile(vector_path)
				&& DTFileUtils.checkFile(bitmap_path)) {
			result += "有矢量图有位图";
			holder.mDownLoad.setVisibility(View.GONE);
		} else {
			holder.mDownLoad.setVisibility(View.VISIBLE);
			if (DTFileUtils.checkFile(vector_path)) {
				result += "只有矢量图";
			} else if (DTFileUtils.checkFile(bitmap_path)) {
				result += "只有位图";
			}
		}
		holder.mStatus.setText(result);// 设置图的状态
		if (DTFileUtils.checkFile(vector_path)) {
			holder.mDownLoad.setVisibility(View.GONE);
		} else {
			holder.mDownLoad.setVisibility(View.VISIBLE);
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
			convertView = DTUIUtils.inflate(R.layout.map_list_title_item);
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
}
