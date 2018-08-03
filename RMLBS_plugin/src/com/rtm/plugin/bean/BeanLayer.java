package com.rtm.plugin.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.RMLocationMode;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.frm.utils.ResourceUtil;

public class BeanLayer implements BaseMapLayer {

	private ArrayList<Bean> beanList;
	private MapView mapview;
	private HashMap<String, Bitmap> mIconMap;
	private OnBeanEatenListener onBeanEatenListener;
	private float range = 2;// 默认5米
	private Bitmap mDefaultIcon;// 默认图标

	/**
	 * 构造方法
	 * 
	 * @param mapView
	 *            MapView对象
	 */
	public BeanLayer(MapView mapView) {
		initLayer(mapView);
	}

	/**
	 * 构造方法，传入默认豆子图片和地图
	 * 
	 * @param mapView
	 *            MapView对象
	 * @param beanDefaultIcon
	 *            当Bean的getName()属性没有设置的bitmap，我们会使用默认图标进行绘图
	 */
	public BeanLayer(MapView mapView, Bitmap beanDefaultIcon) {
		initLayer(mapView);
		if (beanDefaultIcon != null && !beanDefaultIcon.isRecycled())
			this.mDefaultIcon = beanDefaultIcon;
	}

	@Override
	public void initLayer(MapView view) {
		mapview = view;
		mIconMap = new HashMap<String, Bitmap>();
		beanList = new ArrayList<Bean>();
	}

	/**
	 * 得到阈值距离
	 * 
	 * @return 阈值，单位：米
	 */
	public float getRange() {
		return range;
	}
	
	/**
	 * 设置豆子被吃掉后的回调
	 * @param onBeanEatenListener
	 */
	public void setOnBeanEatenListener(OnBeanEatenListener onBeanEatenListener) {
		this.onBeanEatenListener = onBeanEatenListener;
	}

	/**
	 * 设置阈值距离，单位：米
	 * 
	 * @param range
	 */
	public void setRange(float range) {
		this.range = range;
	}

	/**
	 * 设置豆子的默认图片：当Bean的getName()属性没有设置的bitmap，我们会使用默认图标进行绘图
	 * 
	 * @param mDefaultIcon
	 *            豆子的默认图标
	 */
	public void setDefaultIcon(Bitmap mDefaultIcon) {
		this.mDefaultIcon = mDefaultIcon;
	}

	/**
	 * 添加豆子图标
	 * 
	 * @param name
	 *            名字，这个名字用来读取图标匹配Bean,根据Bean的getName()属性，可以得到对应的Bitmap
	 * @param icon
	 *            图标
	 */
	public void addBeanIcon(String name, Bitmap icon) {
		if (!RMStringUtils.isEmpty(name) && icon != null && !icon.isRecycled()) {
			mIconMap.put(name, icon);
		}
	}

	/**
	 * 添加豆子数据
	 * 
	 * @param bean
	 *            豆子对象
	 */
	public void addBean(Bean bean) {
		if (bean != null) {
			beanList.add(bean);
		}
	}

	/**
	 * 添加豆子集合
	 * 
	 * @param beanList
	 *            豆子集合
	 */
	public void addBeanList(ArrayList<Bean> beanList) {
		if (beanList != null && beanList.size() > 0) {
			this.beanList.addAll(beanList);
		}
	}

	public ArrayList<Bean> getBeanList() {
		return beanList;
	}

	@Override
	public boolean onTap(MotionEvent e) {
		return false;
	}

	@Override
	public void destroyLayer() {
		mIconMap.clear();
		if (mDefaultIcon != null && !mDefaultIcon.isRecycled()) {
			mDefaultIcon.recycle();
		}
	}

	@Override
	public void clearLayer() {

	}

	@Override
	public boolean hasData() {
		return beanList.size() > 0;
	}

	@Override
	public void onDraw(Canvas c) {
		if (beanList.size() == 0 || mIconMap.size() == 0) {
			return;
		}
		for (int i = 0; i < beanList.size(); i++) {
			Bean bean = beanList.get(i);
			if (RMStringUtils.isEmpty(bean.getBuildId())
					|| RMStringUtils.isEmpty(bean.getFloor())
					|| RMStringUtils.isEmpty(bean.getName())) {
				continue;
			}
			if (bean.getBuildId().equals(mapview.getBuildId())
					&& bean.getFloor().equals(mapview.getFloor())
					&& !bean.isEaten()) {
				PointInfo mPoint = mapview.fromLocation(new Location(bean
						.getX(), bean.getY()));
				Location currentLoc = null;
				if (mapview.getLocationMode()!=RMLocationMode.NORMAL) {// 如果不是自由模式
					currentLoc = mapview.getCenter();
					currentLoc.setBuildId(mapview.getBuildId());
					currentLoc.setFloor(mapview.getFloor());
				} else {
					currentLoc = mapview.getMyCurrentLocation();
				}
				if (currentLoc != null
						&& currentLoc.getBuildId().equals(bean.getBuildId())
						&& currentLoc.getFloor().equals(bean.getFloor())) {
					float dis = RMathUtils.distance(bean.getX(), bean.getY(),
							currentLoc.getX(), currentLoc.getY());
					if (dis < range) {
						bean.setEaten(true);
						if (onBeanEatenListener != null) {
							onBeanEatenListener.onBeanEaten(bean);
						}
					}
				}
				if (mPoint.getX() < 0 || mPoint.getX() > mapview.getWidth()
						|| mPoint.getY() < 0
						|| mPoint.getY() > mapview.getHeight()) {
					continue;
				}
				if (mIconMap.containsKey(bean.getName())) {
					Bitmap bitmap = mIconMap.get(bean.getName());
					if (!bitmap.isRecycled()) {
						c.drawBitmap(bitmap, mPoint.getX() - bitmap.getWidth()
								/ 2, mPoint.getY() - bitmap.getHeight() / 2,
								null);
					}
				} else {
					if (mDefaultIcon != null && !mDefaultIcon.isRecycled()) {
						c.drawBitmap(mDefaultIcon,
								mPoint.getX() - mDefaultIcon.getWidth() / 2,
								mPoint.getY() - mDefaultIcon.getHeight() / 2,
								null);
					}
				}

			}
		}
	}

	/**
	 * 监听豆子被吃掉的接口
	 * 
	 * @author dingtao
	 *
	 */
	public interface OnBeanEatenListener {
		/**
		 * 当定位点和豆子在阈值范围内，默认豆子会被吃掉，自动设置Bean的isEaten属性为true，并且回调此接口
		 * 
		 * @param bean
		 *            进入阈值范围被吃掉的豆子
		 */
		public void onBeanEaten(Bean bean);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
}
