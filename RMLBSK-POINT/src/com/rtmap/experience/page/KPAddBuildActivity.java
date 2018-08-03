package com.rtmap.experience.page;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.adapter.KPBuildCateAdapter;
import com.rtmap.experience.adapter.KPFloorAdapter;
import com.rtmap.experience.adapter.KPMapDialogAdapter;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.core.KPApplication;
import com.rtmap.experience.core.KPAsyncTask;
import com.rtmap.experience.core.KPCallBack;
import com.rtmap.experience.core.model.BuildInfo;
import com.rtmap.experience.core.model.CateInfo;
import com.rtmap.experience.core.model.CateList;
import com.rtmap.experience.core.model.Floor;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTImageUtil;
import com.rtmap.experience.util.DTLog;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;
import com.rtmap.experience.util.view.DTGridView;
import com.rtmap.experience.util.view.DTListView;
import com.rtmap.experience.util.view.DTWheelView;
import com.rtmap.experience.util.view.NumericWheelAdapter;

public class KPAddBuildActivity extends KPActivity implements OnClickListener,
		OnItemClickListener {

	private DTListView mFloorList;
	private EditText mBuildName;
	private EditText mAddress;
	private DTGridView mCateGrid;
	private TextView mAddFloor;
	private KPFloorAdapter mFloorAdapter;// 楼层列表适配器
	private KPBuildCateAdapter mCateAdapter;// 类别适配器
	private Gson mGson;
	public static BuildInfo mBuild;
	public Dialog mImageDialog, mFloorDialog;
	private TextView mSave;

	public Floor mFloorInfo;

	public static void interActivity(Context context, BuildInfo build) {
		mBuild = build;
		Intent intent = new Intent(context, KPAddBuildActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_add_build);
		mSave = (TextView) findViewById(R.id.save);
		mFloorList = (DTListView) findViewById(R.id.floor_list);
		initHeader();
		initFooter();
		mFloorAdapter = new KPFloorAdapter(this);
		mFloorAdapter.addList(mBuild.getFloorlist());
		mFloorList.setAdapter(mFloorAdapter);
		mFloorList.setPullLoadEnable(false);
		mFloorList.setPullRefreshEnable(false);
		initImageDialog();
		initFloorDialog();
		mSave.setOnClickListener(this);
	}

	private void initHeader() {
		View view = DTUIUtils.inflate(R.layout.add_build_header);
		mBuildName = (EditText) view.findViewById(R.id.build);
		mAddress = (EditText) view.findViewById(R.id.address);
		mCateGrid = (DTGridView) view.findViewById(R.id.cate_grid);
		mCateGrid.setOnItemClickListener(this);
		mCateAdapter = new KPBuildCateAdapter();
		mGson = new Gson();
		CateList catelist = mGson.fromJson(KPApplication.getInstance()
				.getShare().getString(DTFileUtils.BUILD_CATE, null),
				CateList.class);
		mCateAdapter.addList(catelist.getResults());
		mCateGrid.setAdapter(mCateAdapter);
		mCateGrid.setOnItemClickListener(this);
		mBuildName.setText(mBuild.getName());
		mAddress.setText(mBuild.getAddress());
		mFloorList.addHeaderView(view);
	}

	private void initFooter() {
		View view = DTUIUtils.inflate(R.layout.add_build_footer);
		mAddFloor = (TextView) view.findViewById(R.id.add_floor);
		mFloorList.addFooterView(view);
		mAddFloor.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String path = DTFileUtils.getDataDir() + mBuild.getBuildId()
				+ File.separator;
		switch (v.getId()) {
		case R.id.add_floor:// 添加楼层图片点击
			mFloorDialog.show();
			break;
		case R.id.save:
			mBuild.setName(mBuildName.getText().toString());
			mBuild.setAddress(mAddress.getText().toString());
			DTFileUtils.writeFile(mGson.toJson(mBuild),
					path + mBuild.getBuildId() + ".build", false);
			finish();
			break;
		case R.id.ok:
			mFloorDialog.cancel();
			DTLog.i("fllor : "
					+ NumericWheelAdapter.FLOOR_ARRAY[mFloorWheel
							.getCurrentItem()]);
			Floor floor = new Floor();
			floor.setFloor(NumericWheelAdapter.FLOOR_ARRAY[mFloorWheel
					.getCurrentItem()]);
			floor.setBuildId(mBuild.getBuildId());
			mFloorAdapter.addItemLast(floor);
			mFloorAdapter.notifyDataSetChanged();
			if (mBuild.getFloorlist() == null) {
				mBuild.setFloorlist(new ArrayList<Floor>());
			}
			mBuild.getFloorlist().add(floor);
			DTFileUtils.writeFile(mGson.toJson(mBuild),
					path + mBuild.getBuildId() + ".build", false);
			break;
		}
	}

	private DTWheelView mFloorWheel;

	/**
	 * show弹出框
	 */
	private void initFloorDialog() {
		mFloorDialog = new Dialog(this, R.style.dialog);
		mFloorDialog.setContentView(R.layout.choose_floor);
		mFloorDialog.setCanceledOnTouchOutside(true);
		WindowManager.LayoutParams lp = mFloorDialog.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.MATCH_PARENT; //设置宽度
		lp.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
		mFloorDialog.getWindow().setAttributes(lp);
		mFloorWheel = (DTWheelView) mFloorDialog.findViewById(R.id.floor_wheel);
		mFloorWheel.setCyclic(true);// 可循环滚动
		mFloorWheel.setAdapter(new NumericWheelAdapter());

		(mFloorDialog.findViewById(R.id.ok)).setOnClickListener(this);

	}

	/**
	 * show弹出框
	 */
	private void initImageDialog() {
		mImageDialog = new Dialog(this, R.style.dialog);
		mImageDialog.setContentView(R.layout.dialog_map_layout);
		mImageDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mImageDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources()
				.getStringArray(R.array.image_dialog);
		mInterList.setAdapter(new KPMapDialogAdapter(this, interDate));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mImageDialog.cancel();
				if (position == 0) {
					mImageDialog.cancel();
					// 将图片保存至SDcard，相机返回后直接在SDcard读取图片，这样可以解决获取的图片太小的问题
					String path = DTFileUtils.getDataDir()
							+ mBuild.getBuildId() + File.separator
							+ mBuild.getBuildId() + "-" + mFloorInfo.getFloor()
							+ ".jpg";
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(new File(path)));
					startActivityForResult(intent, CAMERA);
				} else {
					mImageDialog.cancel();
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, PHOTO);
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (arg0.getId() == R.id.cate_grid) {
			if (position == mCateAdapter.getCount() - 1) {// 添加分类
				Intent intent = new Intent(this, KPAddCateActivity.class);
				startActivityForResult(intent, 20);
			} else {
				for (CateInfo info : mCateAdapter.getList()) {
					info.setClick(false);
				}
				CateInfo info = mCateAdapter.getItem(position);
				info.setClick(true);
				mCateAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == 20) {
			Bundle bundle = data.getExtras();
			CateInfo cate = (CateInfo) bundle.getSerializable("cate");
			CateList list = new CateList();
			mCateAdapter.addItemLast(cate);
			list.setResults(mCateAdapter.getList());
			KPApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.BUILD_CATE, mGson.toJson(list))
					.commit();
			mCateAdapter.notifyDataSetChanged();
		} else {
			if (requestCode == PHOTO) {
				Uri uri = data.getData();
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor actualimagecursor = managedQuery(uri, proj, null, null,
						null);
				int actual_image_column_index = actualimagecursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				actualimagecursor.moveToFirst();
				String path = actualimagecursor
						.getString(actual_image_column_index);
				// 4.0以上平台会自动关闭cursor,所以加上版本判断,OK
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					actualimagecursor.close();
				if (!DTStringUtils.isEmpty(path)) {
					DTFileUtils.copy(path,
							DTFileUtils.getDataDir() + mBuild.getBuildId()
									+ File.separator + mBuild.getBuildId()
									+ "-" + mFloorInfo.getFloor() + ".jpg",
							false);
				}
			}
			new KPAsyncTask(new HandleImageCall()).run(requestCode);
		}
	}

	/**
	 * 处理图片
	 * 
	 * @author dingtao
	 *
	 */
	class HandleImageCall implements KPCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			int requestCode = (Integer) obj[0];
			String filePath = DTFileUtils.getDataDir() + mBuild.getBuildId()
					+ File.separator + mBuild.getBuildId() + "-"
					+ mFloorInfo.getFloor() + ".jpg";
			if (requestCode == CAMERA) {
				Bitmap bitmap = DTImageUtil.getBitmap(filePath);
				int height = getWindowManager().getDefaultDisplay().getHeight();
				bitmap = ThumbnailUtils
						.extractThumbnail(bitmap, bitmap.getWidth() * height
								/ bitmap.getHeight(), height);
				mFloorAdapter.mImageCache.put(filePath, bitmap);
				DTImageUtil.saveFile(bitmap, filePath);
			}
			return filePath;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			String path = (String) obj;
			File file = new File(path);
			if (file.exists()) {
				mFloorAdapter.notifyDataSetChanged();// 添加楼层成功或者替换楼层
			} else {
				mLoadDialog.cancel();
				DTUIUtils.showToastSafe("图片路径无效");
			}
		}

	}
}
