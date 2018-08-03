package com.airport.test.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.model.AirData;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.util.DTLog;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.POI;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtm.frm.utils.RMPoiDetailUtil.OnGetPoiDescListener;

public class PoiInfoActivity extends DTActivity implements OnClickListener,
		OnGetPoiDescListener {
	private ImageView imgBack;
	private TextView mPoiName, mAddress, mPhone, mCate, mTime, mMoney;
	private RelativeLayout mCome, mGo;
	private ImageView mPhoto;
	private Gson mGson = new Gson();
	private BuildInfo mBuild;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this)
				.memoryCacheExtraOptions(480, 800)
				// max width, max height，即保存的每个缓存文件的最大长宽
				.threadPoolSize(3)
				// 线程池内加载的数量
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
				// You can pass your own memory cache
				// implementation/你可以通过自己的内存缓存实现
				.memoryCacheSize(2 * 1024 * 1024)
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.imageDownloader(
						new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout
																			// (5
																			// s),
																			// readTimeout
																			// (30
																			// s)超时时间
				.writeDebugLogs() // Remove for release app
				.build();// 开始构建
		ImageLoader.getInstance().init(config);// 全局初始化此配置
		setContentView(R.layout.poi_info);

		imgBack = (ImageView) findViewById(R.id.img_back);
		imgBack.setOnClickListener(this);
		mPoiName = (TextView) findViewById(R.id.poi_name);
		mAddress = (TextView) findViewById(R.id.poi_address);
		mPhone = (TextView) findViewById(R.id.poi_phone);
		mCate = (TextView) findViewById(R.id.poi_cate);
		mTime = (TextView) findViewById(R.id.poi_time);
		mMoney = (TextView) findViewById(R.id.poi_money);
		mCome = (RelativeLayout) findViewById(R.id.come);
		mGo = (RelativeLayout) findViewById(R.id.go);
		mPhoto = (ImageView) findViewById(R.id.photo);
		POI poi = (POI) getIntent().getExtras().getSerializable("poi");
		mBuild = mGson.fromJson(AirData.AIR_DATA, BuildInfo.class);
		RMPoiDetailUtil.requestPoiDesc("mX0FE6AO7f", poi.getBuildId(),
				poi.getFloor(), "" + poi.getPoiNo(), this);
		mCome.setOnClickListener(this);
		mGo.setOnClickListener(this);

	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			finish();
			break;
		case R.id.come:
			setResult(Activity.RESULT_OK, getIntent().putExtra("type", 1));
			finish();
			break;
		case R.id.go:
			setResult(Activity.RESULT_OK, getIntent().putExtra("type", 2));
			finish();
			break;
		}
	}

	@Override
	public void onGetPoiDesc(String result) {
		DTLog.e(result);
		try {
			if (result != null && !RMHttpUtil.NET_ERROR.equals(result)) {
				JSONObject resultobj;
				resultobj = new JSONObject(result);

				JSONObject errorobj = resultobj.getJSONObject("result");
				if (Integer.parseInt(errorobj.getString("error_code")) == 0) {
					JSONObject poiobj = resultobj.getJSONObject("poiinfo");
					if (poiobj.has("poi_name")) {
						mPoiName.setText(poiobj.getString("poi_name"));
					}
					if (poiobj.has("poi_address")) {
						mAddress.setText("地址："
								+ poiobj.getString("poi_address"));
					}
					if (poiobj.has("business_type")) {
						mCate.setText("分类:" + poiobj.getString("business_type"));
					}
					if (poiobj.has("business_time")) {
						mTime.setText("营业时间："
								+ poiobj.getString("business_time"));
					}
					if (poiobj.has("support_currecy")) {
						mMoney.setText("支持货币："
								+ poiobj.getString("support_currecy"));
					}
					if (poiobj.has("poi_image")) {
						ImageLoader.getInstance().loadImage(
								poiobj.getString("poi_image"),
								new ImageLoadingListener() {

									@Override
									public void onLoadingStarted(String arg0,
											View arg1) {

									}

									@Override
									public void onLoadingFailed(String arg0,
											View arg1, FailReason arg2) {

									}

									@Override
									public void onLoadingComplete(String arg0,
											View arg1, Bitmap arg2) {
										mPhoto.setImageBitmap(arg2);
									}

									@Override
									public void onLoadingCancelled(String arg0,
											View arg1) {

									}
								});
					}
				} else {
					DTUIUtil.showToastSafe(errorobj.getString("error_msg"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
