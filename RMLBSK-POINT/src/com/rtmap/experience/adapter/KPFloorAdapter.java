package com.rtmap.experience.adapter;

import java.io.File;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseAdapter;
import com.rtmap.experience.core.model.Floor;
import com.rtmap.experience.page.KPAddBuildActivity;
import com.rtmap.experience.page.KPMapActivity;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTImageUtil;
import com.rtmap.experience.util.DTUIUtils;

public class KPFloorAdapter extends KPBaseAdapter<Floor> implements
		OnClickListener {

	private KPAddBuildActivity mActivity;
	public LruCache<String, Bitmap> mImageCache;

	public KPFloorAdapter(KPAddBuildActivity activity) {
		mActivity = activity;
		mImageCache = new LruCache<String, Bitmap>(10 * 1024 * 1024);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtils.inflate(R.layout.floor_item);
			holder = new ViewHodler();
			holder.floor = (TextView) view.findViewById(R.id.floor);
			holder.image = (ImageView) view.findViewById(R.id.floor_image);
			holder.addScale = (TextView) view.findViewById(R.id.add_scale);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		Floor floor = mList.get(position);
		holder.floor.setText(floor.getFloor());
		String filePath = DTFileUtils.getDataDir() + floor.getBuildId()
				+ File.separator + floor.getBuildId() + "-" + floor.getFloor()
				+ ".jpg";
		File file = new File(filePath);
		if (file.exists()) {
			if (mImageCache.get(filePath) == null) {
				mImageCache.put(filePath, DTImageUtil.getBitmap(filePath));
			}
			holder.image.setImageBitmap(mImageCache.get(filePath));
			if (floor.getScale() == 0) {
				holder.addScale.setVisibility(View.VISIBLE);
			} else {
				holder.addScale.setVisibility(View.GONE);
			}
		} else {
			holder.image.setImageResource(R.drawable.add_floor_image);
			holder.addScale.setVisibility(View.GONE);
		}
		holder.addScale.setTag(position);
		holder.addScale.setOnClickListener(this);
		holder.image.setTag(position);
		holder.image.setOnClickListener(this);
		return view;
	}

	class ViewHodler {
		public TextView floor;
		public ImageView image;
		public TextView addScale;
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		switch (v.getId()) {
		case R.id.floor_image:
			mActivity.mFloorInfo = getItem(position);
			String filePath = DTFileUtils.getDataDir()
					+ mActivity.mFloorInfo.getBuildId() + File.separator
					+ mActivity.mFloorInfo.getBuildId() + "-"
					+ mActivity.mFloorInfo.getFloor() + ".jpg";
			File file = new File(filePath);
			if (file.exists()) {
				if (mActivity.mFloorInfo.getScale() == 0) {
					mActivity.mImageDialog.show();
				} else {
					KPMapActivity.interActivity(mActivity, getItem(position),
							mActivity.mBuild);
				}
			} else {
				mActivity.mImageDialog.show();
			}
			break;

		case R.id.add_scale:
			KPMapActivity.interActivity(mActivity, getItem(position),
					mActivity.mBuild);
			break;
		}
	}
}
