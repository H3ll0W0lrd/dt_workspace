package com.rtmap.locationcheck.pageNew;

import java.io.File;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCMapDialogAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.model.RMPoi;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.PoiClassify;

public class LCPoiInfoActivity extends LCActivity implements OnClickListener,
		OnLongClickListener {

	private EditText mPoiName;
	private TextView mPoiCate;
	private TextView mPoiCateDesc;

	private ImageView mCamera, mPhoto;

	private RMPoi mPoi;
	private String fileUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_poi_info);
		mPoiName = (EditText) findViewById(R.id.poi_name);
		mPoiCate = (TextView) findViewById(R.id.poi_cate);
		mPoiCateDesc = (TextView) findViewById(R.id.poi_cate_desc);
		mCamera = (ImageView) findViewById(R.id.camera);
		mPhoto = (ImageView) findViewById(R.id.photo);

		findViewById(R.id.ok).setOnClickListener(this);
		mCamera.setOnLongClickListener(this);
		mCamera.setOnClickListener(this);
		mPoiCate.setOnClickListener(this);
		mPhoto.setOnClickListener(this);

		if (savedInstanceState == null)
			mPoi = (RMPoi) getIntent().getExtras().getSerializable("poi");
		else {
			onReciver(savedInstanceState);
		}
		initData();
		initCateDialog();
	}

	private void initData() {
		mPoiName.setText(mPoi.getName());
		if (!DTStringUtils.isEmpty(mPoi.getDesc())) {
			String[] str = mPoi.getDesc().split("-");
			mPoiCate.setText(str[1]);
			mPoiCateDesc.setText(str[2]);
		} else {
			mPoi.setDesc(PoiClassify.country[0] + "-"
					+ PoiClassify.province[0][0] + "-" + PoiClassify.city[0][0]);
		}
		setImage();
	}

	private void setImage() {
		if (!DTStringUtils.isEmpty(mPoi.getImage())) {
			final Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(
					DTFileUtils.getDataDir() + mPoi.getBuildId()
							+ File.separator + mPoi.getImage(), options);
			options.inSampleSize = 4;
			options.inJustDecodeBounds = false;
			File file4 = new File(DTFileUtils.getDataDir() + mPoi.getBuildId()
					+ File.separator + mPoi.getImage());
			if (file4.exists()) {
				mCamera.setImageBitmap(BitmapFactory.decodeFile(
						file4.getAbsolutePath(), options));
				mPhoto.setVisibility(View.GONE);
			} else {
				mCamera.setVisibility(View.VISIBLE);
				mPhoto.setVisibility(View.VISIBLE);
			}
		}
	}

	private Dialog mCateDialog;
	private int mOneIndex, mTwoIndex;
	private int mCateIndex;

	private void initCateDialog() {
		mCateDialog = new Dialog(this, R.style.dialog);
		mCateDialog.setContentView(R.layout.dialog_cate_layout);
		mCateDialog.setCanceledOnTouchOutside(true);
		ListView mOneList = (ListView) mCateDialog.findViewById(R.id.cate_one);
		mOneList.setAdapter(new LCMapDialogAdapter(this, PoiClassify.country));

		ListView mTwoList = (ListView) mCateDialog.findViewById(R.id.cate_two);
		final LCMapDialogAdapter mTwoAdapter = new LCMapDialogAdapter(this,
				PoiClassify.province[0]);
		mTwoList.setAdapter(mTwoAdapter);
		mOneList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mCateIndex = position;
				mTwoAdapter.setInterList(PoiClassify.province[position]);
				mTwoAdapter.notifyDataSetChanged();
			}
		});
		mTwoList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mOneIndex = mCateIndex;
				mTwoIndex = arg2;
				mPoi.setDesc(PoiClassify.country[mOneIndex] + "-"
						+ PoiClassify.province[mCateIndex][arg2] + "-"
						+ PoiClassify.city[mCateIndex][arg2]);
				mPoiCateDesc.setText(PoiClassify.city[mCateIndex][arg2]);
				mPoiCate.setText(PoiClassify.province[mCateIndex][arg2]);
				mCateIndex = 0;
				mCateDialog.cancel();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		onSave(arg0);
	}

	public void onSave(Bundle save) {
		save.putString("path", fileUrl);
		save.putSerializable("poi", mPoi);
		save.putInt("mOneIndex", mOneIndex);
		save.putInt("mTwoIndex", mTwoIndex);
	}

	public void onReciver(Bundle save) {
		fileUrl = save.getString("path");
		mPoi = (RMPoi) save.getSerializable("poi");
		mOneIndex = save.getInt("mOneIndex");
		mTwoIndex = save.getInt("mTwoIndex");
	}

	@Override
	public void onClick(View v) {
		// POI采集图片命名：建筑物ID_楼层_poi_时间戳.jpg
		switch (v.getId()) {
		case R.id.camera:
			fileUrl = mPoi.getBuildId() + "_" + mPoi.getFloor() + "_poi_"
					+ System.currentTimeMillis() + ".jpg";
			File file1 = new File(DTFileUtils.getDataDir() + mPoi.getBuildId()
					+ File.separator + mPoi.getImage());
			if (!DTStringUtils.isEmpty(mPoi.getImage()) && file1.exists()) {
				LCImageActivity.interActivity(this, DTFileUtils.getDataDir()
						+ mPoi.getBuildId() + File.separator + mPoi.getImage());
			} else {
				Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent1.putExtra(
						MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(DTFileUtils.getDataDir()
								+ mPoi.getBuildId() + File.separator + fileUrl)));
				startActivityForResult(intent1, LCActivity.CAMERA);
			}
			break;
		case R.id.photo:
			fileUrl = mPoi.getBuildId() + "_" + mPoi.getFloor() + "_poi_"
					+ System.currentTimeMillis() + ".jpg";
			File file2 = new File(DTFileUtils.getDataDir() + mPoi.getBuildId()
					+ File.separator + fileUrl);
			if (file2.exists()) {
				LCImageActivity.interActivity(this, DTFileUtils.getDataDir()
						+ mPoi.getBuildId() + File.separator + fileUrl);
			} else {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
						DTFileUtils.getDataDir() + mPoi.getBuildId()
								+ File.separator + fileUrl)));
				startActivityForResult(i, LCActivity.PHOTO);
			}
			break;
		case R.id.poi_cate:
			mCateDialog.show();
			break;
		case R.id.ok:
			mPoi.setName(mPoiName.getText().toString());
			if (DTStringUtils.isEmpty(mPoi.getName())) {
				DTUIUtils.showToastSafe("请填写poi名称");
				return;
			}
			LCApplication.getInstance().getShare().edit()
					.putString("poi_desc", mPoi.getDesc()).commit();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("poi", mPoi);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		int sign = getIntent().getExtras().getInt("sign");
		if (DTStringUtils.isEmpty(mPoi.getImage()) || sign == 1)
			return;
		File file4 = new File(DTFileUtils.getDataDir() + mPoi.getBuildId()
				+ File.separator + mPoi.getImage());
		if (file4.exists()) {
			file4.delete();
			mCamera.setImageResource(R.drawable.d);
			mCamera.setVisibility(View.VISIBLE);
			mPhoto.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.camera:
			if (DTStringUtils.isEmpty(mPoi.getImage()))
				return true;
			File file4 = new File(DTFileUtils.getDataDir() + mPoi.getBuildId()
					+ File.separator + mPoi.getImage());
			if (file4.exists()) {
				file4.delete();
				mCamera.setImageResource(R.drawable.d);
				mCamera.setVisibility(View.VISIBLE);
				mPhoto.setVisibility(View.VISIBLE);
			}
			break;
		}
		return true;
	}

	class ImageCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			return DTIOUtils
					.rotaingImageView(
							DTFileUtils.getDataDir() + mPoi.getBuildId()
									+ File.separator + fileUrl,
							getApplicationContext());
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				File file = (File) obj;
				if (file.exists()) {
					mPoi.setImage(fileUrl);
					setImage();
				}
			} else {
				DTUIUtils.showToastSafe("无法读取图片地址");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		String url = getFilePath(DTFileUtils.getDataDir() + mPoi.getBuildId()
				+ File.separator + fileUrl, requestCode, data);// 得到文件路径
		if (requestCode == PHOTO)
			DTFileUtils.copy(url, DTFileUtils.getDataDir() + mPoi.getBuildId()
					+ File.separator + fileUrl, false);
		new LCAsyncTask(new ImageCall()).execute();
	}
}
