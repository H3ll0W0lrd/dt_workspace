package com.rtm.common.style;


/**
 * 地图文字样式
 * 
 * @author dingtao
 *
 */
public class TextStyle {
	private int textcolor = 0xff331004;
	private int textsize = 30;

	/**
	 * 得到地图文字颜色
	 * 
	 * @return
	 */
	public int getTextcolor() {
		return textcolor;
	}

	/**
	 * 设置地图字体颜色，默认0xff331004
	 * 
	 * @param textcolor
	 *            颜色值，例：0xffffffff
	 */
	public void setTextcolor(int textcolor) {
		this.textcolor = textcolor;
	}

	/**
	 * 得到地图文字大小
	 * 
	 * @return 返回字体大小，单位：像素
	 */
	public int getTextsize() {
		return textsize;
	}

	/**
	 * 设置地图文字大小，默认30px
	 * 
	 * @param textsize
	 *            字体大小，单位：px
	 */
	public void setTextsize(int textsize) {
		this.textsize = textsize;
	}

}
