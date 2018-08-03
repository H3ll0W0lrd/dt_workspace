package com.rtm.frm.map;

import java.io.File;

import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.frm.drawmap.DrawMap;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.OnMapDownLoadFinishListener;
import com.rtm.frm.utils.RMDownLoadMapUtil;

public class MapConfig implements OnMapDownLoadFinishListener {
	public static final int DPI = 96;
	private MapView mMapView;
	// in cm
	private float mBuildWidth;
	private float mBuildHeight;

	private String mFloor;
	private String mBuildId;
	private boolean isFirstMap = true;// 是不是第一张地图

	private boolean mIsNewMap = true;

	/* private static MapConfig instance; */
	private DrawMap mDrawMap;
	@SuppressWarnings("unused")
	private OnFloorChangedListener mOnFloorChangedListener;

	public MapConfig(MapView mapView) {
		mMapView = mapView;
		mDrawMap = new DrawMap(mapView);

	}

	public float getBuildWidth() {
		return mBuildWidth;
	}

	public float getBuildHeight() {
		return mBuildHeight;
	}

	public void setFloor(String floor) {
		mFloor = floor;
	}

	public String getFloor() {
		return mFloor;
	}

	public void setBuildId(String id) {
		mBuildId = id;
	}

	public String getBuildId() {
		return mBuildId;
	}

	public float getScaleOfPxByScale(int scale) {
		return scale;
	}

	public void setNewMap(boolean isNew) {
		mIsNewMap = isNew;
	}

	public boolean isNewMap() {
		return mIsNewMap;
	}

	public DrawMap getDrawMap() {
		return mDrawMap;
	}

	public void reloadmap() {
		Handlerlist.getInstance().notifications(Constants.RTMAP_MAP,
				Constants.MAP_LOAD_START, null);
		mDrawMap = new DrawMap(mMapView);
		// String mfilename=mBuildId+"_"+getFloor()+".imap";
		String vector_path = RMFileUtil.getMapDataDir()
				+ RMD5Util.md5(mBuildId + "_" + getFloor() + ".imap");
		File map = new File(vector_path);
		if (map.exists()) {// 更新
			OnMapDownLoadFinish();
			if (mMapView.isUpdateMap())
				RMDownLoadMapUtil.downLoadMap(XunluMap.getInstance()
						.getApiKey(), mBuildId, getFloor(), null);// 开始下载
		} else {// 下载
			if (mMapView.isUpdateMap())
				RMDownLoadMapUtil.downLoadMap(XunluMap.getInstance()
						.getApiKey(), mBuildId, getFloor(), this);// 开始下载
		}
	}

	public void initMapConfig(String id, String floor) {

		mIsNewMap = true;

		if (mBuildId != null && mFloor != null && mBuildId.equals(id)
				&& mFloor.equals(floor)) {
			mIsNewMap = false;
		}

		mBuildId = id;
		mFloor = floor;
		if (mMapView.getTapPOILayer() != null) {
			mMapView.getTapPOILayer().setPOI(null);
		}
		reloadmap();
	}

	/**
	 * 楼层改变回调器
	 * 
	 * @author dingtao
	 *
	 */
	public static interface OnFloorChangedListener {
		public void onFloorChanged(String floorString);
	}

	public static interface OnMapChangedListener {
		public void onMapChanged(String build, String floorString);
	}

	@Override
	public void OnMapDownLoadFinish() {
		String vector_path = RMFileUtil.getMapDataDir()
				+ RMD5Util.md5(mBuildId + "_" + getFloor() + ".imap");
		File map = new File(vector_path);
		if (map.exists()) {
			mDrawMap.openMap(vector_path, mBuildId, getFloor());
		}
		if (mDrawMap.getLayer() != null) {
			mBuildHeight = mDrawMap.getBuildheight();
			mBuildWidth = mDrawMap.getBuildwidth();
			mDrawMap.setSelectPoi(null);
		}

		if (map.exists()) {
			if (isFirstMap) {
				isFirstMap = false;
				mMapView.reset();
				mMapView.resetscale();
			}else{
				if(mMapView.isResetMapCenter()){
					mMapView.reset();
				}
				if(mMapView.isResetMapScale()){
					mMapView.resetscale();
				}
				mMapView.refreshMap();
			}
			Handlerlist.getInstance().notifications(Constants.RTMAP_MAP,
					Constants.MAP_LOAD_END, null);
		}
	}
}
