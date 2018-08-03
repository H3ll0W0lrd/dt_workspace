package com.rtmap.locationcheck.page;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCBrandAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.Build;
import com.rtmap.locationcheck.pageNew.LCImageActivity;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCPickBuildActivity extends LCActivity implements OnClickListener,
		OnLongClickListener {

	private EditText mName, mAddress;
	private ImageView mNamePhoto, mAddressPhoto;
	private GridView mBrandGrid;
	private LCBrandAdapter mBrandAdapter;
	private String fileUrl;
	private Build mBuild;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.build_info_dialog);

		mName = (EditText) findViewById(R.id.build_name);
		mAddress = (EditText) findViewById(R.id.build_address);
		mNamePhoto = (ImageView) findViewById(R.id.name_image);
		mAddressPhoto = (ImageView) findViewById(R.id.address_image);
		mBrandGrid = (GridView) findViewById(R.id.brand_grid);
		mBrandAdapter = new LCBrandAdapter();
		mBrandGrid.setAdapter(mBrandAdapter);
		mBrandGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 == mBrandAdapter.getCount() - 1) {
					fileUrl = DTFileUtils.getDataDir() + mBuild.getBuildId()
							+ File.separator + mBuild.getBuildId() + "_brand_"
							+ System.currentTimeMillis() + ".jpg";
					File file = new File(fileUrl);
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
					startActivityForResult(intent, LCActivity.CAMERA);
				} else {
					LCImageActivity.interActivity(LCPickBuildActivity.this,
							mBrandAdapter.getItem(arg2));
				}
			}
		});
		mBrandGrid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (arg2 != mBrandAdapter.getCount() - 1) {
					DTFileUtils.deleteFile(mBrandAdapter.getItem(arg2));
					mBrandAdapter.removeItem(arg2);
					mBrandAdapter.notifyDataSetChanged();
					return true;
				}
				return false;
			}
		});

		mNamePhoto.setOnClickListener(this);
		mAddressPhoto.setOnClickListener(this);
		mNamePhoto.setOnLongClickListener(this);
		mAddressPhoto.setOnLongClickListener(this);
		findViewById(R.id.ok).setOnClickListener(this);

		TextView title = (TextView) findViewById(R.id.title);
		TextView buildId = (TextView) findViewById(R.id.build_id);

		if (savedInstanceState == null)
			mBuild = (Build) getIntent().getExtras().getSerializable("build");
		else {
			onReciver(savedInstanceState);
		}
		title.setText(mBuild.getBuildName());
		buildId.setText(mBuild.getBuildId());
		initData();
	}

	@Override
	protected void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		onSave(arg0);
	}

	public void onSave(Bundle save) {
		save.putString("path", fileUrl);
		save.putSerializable("build", mBuild);
	}

	public void onReciver(Bundle save) {
		fileUrl = save.getString("path");
		mBuild = (Build) save.getSerializable("build");
	}

	private void initData() {
		mName.setText(mBuild.getBuildName());
		mAddress.setText(mBuild.getAddress());
		DTFileUtils.createDirs(DTFileUtils.getDataDir() + mBuild.getBuildId()
				+ File.separator);
		File file3 = new File(DTFileUtils.getDataDir() + mBuild.getBuildId()
				+ File.separator + mBuild.getBuildId() + "_name.jpg");
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fileUrl, options);
		options.inSampleSize = 4;
		options.inJustDecodeBounds = false;
		if (file3.exists()) {
			mNamePhoto.setImageBitmap(BitmapFactory.decodeFile(
					file3.getAbsolutePath(), options));
		} else {
			mNamePhoto.setImageResource(R.drawable.d);
		}
		File file4 = new File(DTFileUtils.getDataDir() + mBuild.getBuildId()
				+ File.separator + mBuild.getBuildId() + "_address.jpg");
		if (file4.exists()) {
			mAddressPhoto.setImageBitmap(BitmapFactory.decodeFile(
					file4.getAbsolutePath(), options));
		} else {
			mAddressPhoto.setImageResource(R.drawable.d);
		}
		String[] brandArray = DTFileUtils.listFiles(DTFileUtils.getDataDir()
				+ mBuild.getBuildId() + File.separator, new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.startsWith(mBuild.getBuildId() + "_brand_")
						&& filename.endsWith(".jpg")) {
					return true;
				}
				return false;
			}
		});
		mBrandAdapter.clearList();
		if (brandArray != null && brandArray.length > 0) {
			ArrayList<String> list = new ArrayList<String>();
			for (String p : brandArray) {
				DTLog.i(p);
				list.add(DTFileUtils.getDataDir() + mBuild.getBuildId()
						+ File.separator + p);
			}
			mBrandAdapter.addList(list);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			String name = mName.getText().toString();
			String address = mAddress.getText().toString();
			if ((!DTStringUtils.isEmpty(name) && !DTStringUtils
					.isEmpty(address))) {
				File file1 = new File(DTFileUtils.getDataDir()
						+ mBuild.getBuildId() + File.separator
						+ mBuild.getBuildId() + "_name.jpg");
				if (name.equals(mBuild.getBuildName()) && !file1.exists()) {

				} else if (!name.equals(mBuild.getBuildName())
						&& file1.exists()) {

				} else {
					DTUIUtils.showToastSafe("照片和名字都得改");
					return;
				}
				File file2 = new File(DTFileUtils.getDataDir()
						+ mBuild.getBuildId() + File.separator
						+ mBuild.getBuildId() + "_address.jpg");
				if (address.equals(mBuild.getAddress()) && !file2.exists()) {

				} else if (!address.equals(mBuild.getAddress())
						&& file2.exists()) {

				} else {
					DTUIUtils.showToastSafe("照片和地址都得改");
					return;
				}
				if (!name.equals(mBuild.getBuildName())
						|| !address.equals(mBuild.getAddress())) {
					Build b = new Build();
					b.setBuildId(mBuild.getBuildId());
					b.setBuildName(name);
					b.setAddress(address);
					Gson gson = new Gson();
					DTFileUtils.writeFile(gson.toJson(b),
							DTFileUtils.getDataDir() + mBuild.getBuildId()
									+ File.separator + mBuild.getBuildId()
									+ "_info.txt", false);
				}
				// 品宣名字
				// mBuild.getBuildId()+"_brand_"+System.currentTimeMillis()+".jpg"
				mLoadDialog.show();
				new LCAsyncTask(new UploadBuildInfoCall()).run();
			} else {
				DTUIUtils.showToastSafe("请输入名称和地址");
			}
			break;
		case R.id.name_image:
			fileUrl = DTFileUtils.getDataDir() + mBuild.getBuildId()
					+ File.separator + mBuild.getBuildId() + "_name.jpg";
			File file = new File(fileUrl);
			if (file.exists()) {
				LCImageActivity.interActivity(this, fileUrl);
			} else {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(fileUrl)));
				startActivityForResult(intent, LCActivity.CAMERA);
			}
			break;
		case R.id.address_image:
			fileUrl = DTFileUtils.getDataDir() + mBuild.getBuildId()
					+ File.separator + mBuild.getBuildId() + "_address.jpg";
			File file1 = new File(fileUrl);
			if (file1.exists()) {
				LCImageActivity.interActivity(this, fileUrl);
			} else {
				Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent1.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(fileUrl)));
				startActivityForResult(intent1, LCActivity.CAMERA);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void setImage() {
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fileUrl, options);
		options.inSampleSize = 4;
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(fileUrl, options);// 得到缩小后的图片
		if (fileUrl.endsWith("name.jpg")) {
			mNamePhoto.setImageBitmap(bitmap);
		} else if (fileUrl.endsWith("_address.jpg")) {
			mAddressPhoto.setImageBitmap(bitmap);
		} else {
			boolean isAdd = true;
			for (String p : mBrandAdapter.getList()) {
				if (p.equals(fileUrl)) {
					isAdd = false;
					break;
				}
			}
			if (isAdd) {
				mBrandAdapter.addItemLast(fileUrl);
				mBrandAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.name_image:
			DTFileUtils.deleteFile(DTFileUtils.getDataDir()
					+ mBuild.getBuildId() + File.separator
					+ mBuild.getBuildId() + "_name.jpg");
			mNamePhoto.setImageResource(R.drawable.d);
			break;
		case R.id.address_image:
			DTFileUtils.deleteFile(DTFileUtils.getDataDir()
					+ mBuild.getBuildId() + File.separator
					+ mBuild.getBuildId() + "_address.jpg");
			mAddressPhoto.setImageResource(R.drawable.d);
			break;
		}
		return true;
	}

	/**
	 * 上传建筑物信息
	 * 
	 * @author dingtao
	 *
	 */
	class UploadBuildInfoCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			File file = new File(DTFileUtils.getDataDir() + mBuild.getBuildId()
					+ File.separator + mBuild.getBuildId() + "_info.txt");
			File file1 = new File(DTFileUtils.getDataDir()
					+ mBuild.getBuildId() + File.separator
					+ mBuild.getBuildId() + "_name.jpg");
			File file2 = new File(DTFileUtils.getDataDir()
					+ mBuild.getBuildId() + File.separator
					+ mBuild.getBuildId() + "_address.jpg");
			ArrayList<File> list = new ArrayList<File>();
			if (file.exists())
				list.add(file);
			if (file1.exists())
				list.add(file1);
			if (file2.exists())
				list.add(file2);
			for (int i = 0; i < mBrandAdapter.getCount() - 1; i++) {
				list.add(new File(mBrandAdapter.getItem(i)));
			}
			if (list.size() == 0) {
				return null;
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
			String time = format.format(new Date(System.currentTimeMillis()))
					.replaceAll("-", "T");
			File zipfile = new File(DTFileUtils.getDataDir()
					+ mBuild.getBuildId() + File.separator
					+ mBuild.getBuildId() + "_info_" + time + ".zip");
			try {
				DTFileUtils.zipFiles(list, zipfile);
				String result = LCHttpClient.postUpFile(String.format(
						LCHttpUrl.BUILD_INFO,
						LCApplication.getInstance().getShare()
								.getString(DTFileUtils.PREFS_TOKEN, ""),
						mBuild.getBuildId()), zipfile);
				JSONObject json = new JSONObject(result);
				if ("1".equals(json.getString("status"))) {
					DTUIUtils.showToastSafe("建筑物信息上传成功");

					String destPath = DTFileUtils.getBackupDir()
							+ mBuild.getBuildId() + File.separator
							+ mBuild.getBuildId() + "_info_" + time + ".zip";
					DTFileUtils.copyFile(zipfile.getAbsolutePath(), destPath,
							true);
					for (int i = 0; i < list.size(); i++) {
						list.get(i).delete();
					}
					return result;
				} else {
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LCException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj == null) {
				DTUIUtils.showToastSafe("建筑物信息上传失败或信息缺失");
			} else {
				finish();
			}
			mLoadDialog.cancel();
		}
	}

	class ImageCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			return DTIOUtils.rotaingImageView(fileUrl, getApplicationContext());
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				File file = (File) obj;
				if (file.exists()) {
					setImage();
				}
			} else {
				DTUIUtils.showToastSafe("无法读取图片地址");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		if (requestCode == CAMERA) {
			String fileurl = getFilePath(fileUrl, requestCode, data);// 得到文件路径
			new LCAsyncTask(new ImageCall()).execute(fileurl);
		}
	}
}
