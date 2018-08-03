package com.rtm.common.style;

/**
 * 绘图样式
 * 
 * @author dingtao
 *
 */
public class DrawStyle {

	private int colorfill;
	private int colorborder;
	private float widthborder;

	/**
	 * 
	 * @param fill
	 *            填充色
	 * @param border
	 *            边线颜色
	 * @param width
	 *            宽度
	 */
	public DrawStyle(int fill, int border, float width) {
		colorfill = fill;
		colorborder = border;
		widthborder = width;
	}

	/**
	 * 得到填充的颜色值
	 * 
	 * @return
	 */
	public int getColorfill() {
		return colorfill;
	}

	/**
	 * 设置填充的颜色值
	 * 
	 * @param colorfill
	 *            颜色值，例：0xffffffff
	 */
	public void setColorfill(int colorfill) {
		this.colorfill = colorfill;
	}

	/**
	 * 得到边线的颜色值
	 * 
	 * @return
	 */
	public int getColorborder() {
		return colorborder;
	}

	/**
	 * 设置边线的颜色值
	 * 
	 * @param colorborder
	 *            颜色值，例：0xffffffff
	 */
	public void setColorborder(int colorborder) {
		this.colorborder = colorborder;
	}

	/**
	 * 得到边线的宽度，单位（像素）
	 * 
	 * @return
	 */
	public float getWidthborder() {
		return widthborder;
	}

	/**
	 * 设置边线的宽度，单位（像素）
	 * @param widthborder
	 */
	public void setWidthborder(float widthborder) {
		this.widthborder = widthborder;
	}
}
