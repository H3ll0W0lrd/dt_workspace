
package com.rtm.frm.map;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * @ClassName: BaseMapLayer
 * 图层基类，自定义图层，请implements它
 * @author dingtao
 * @date 2013-5-14 上午10:14:10
 *
 */
public interface BaseMapLayer {
	/**
	 * 初始化图层，这个方法仅仅是定义一种开发的书写方式，不强制开发者必须再此初始化数据，因为你完全可以在构造方法中写完你的初始化过程，
	 * 所以这个方法你可用可不用
	 * 
	 * @param view MapView类型
	 */
	public void initLayer(MapView view);

	/**
	 * 点击，如果自定义点击事件，请在此方法中处理
	 * 
	 * @param event
	 *            点击事件
	 * @return boolean值
	 */
	public boolean onTap(MotionEvent event);

	/**
	 * 销毁图层，取代clearLayer()，此方法会在MapView调用clearLayer()时被调用，所以可以将自己的数据再此方法中清除
	 */
	public void destroyLayer();
	
	/**
	 * 清除图层数据，自定义图层请将清除数据操作写在destroyLayer()方法中，此方法在后续几个版本中将去掉
	 */
	@Deprecated
	public void clearLayer();

	/**
	 * 图层内是否有数据，建议开发者将数据判断写入此方法
	 * 
	 * @return boolean值
	 */
	public boolean hasData();

	/**
	 * 图层上的touch事件处理，继续传递MapView的onTouchEvent()方法中MotionEvent
	 * 
	 * @param event
	 *            触摸事件，MotionEvent类型
	 * @return true：事件不再向下传递；否则为false
	 */
	public boolean onTouchEvent(MotionEvent event);

	/**
	 * 图层绘制
	 * 
	 * @param canvas
	 *            绘图
	 */
	public void onDraw(Canvas canvas);
}
