package com.rtm.common.model;

import java.io.Serializable;

import com.rtm.common.style.DrawStyle;
import com.rtm.common.style.TextStyle;
import com.rtm.common.utils.RMStringUtils;

/**
 * POI信息类，在每个楼层上标记店铺、工作台等点
 * 
 * @author dingtao
 *
 */
public class POI implements Serializable {
	private static final long serialVersionUID = 1L;
	private String buildId;//建筑物Id
	private String classid;//类别ID
	private String classname;//类别名字
	private int type;//poi类型
	private int poiNo;//每层的number编号

	/**
	 * 建筑物名字
	 */
	private String buildName;
	/**
	 * 是否在室内
	 */
	private String isInside;
	/**
	 * poi名字
	 */
	protected String name;
	private String floor;
	/**
	 * poi中心点x坐标
	 */
	protected float x;
	/**
	 * poi中心点y坐标
	 */
	protected float y;
	/**
	 * poi样式
	 */
	protected int style;//样式
	private String name_en;
	private String name_qp;
	private String name_jp;
	
	private String address;//地址
	private String phone;//电话
	private String currecy;//货币方式
	private String hours;//营业时间
	private String desc;//描述
	private String logoImage;//logo的图片
	private String poiImage;//poi的图片
	
	
	/**
	 * POI绘制样式
	 */
	private DrawStyle drawStyle;
	/**
	 * POI文字绘制样式
	 */
	private TextStyle textStyle;

	/**
	 * 得到name简称
	 * 
	 * @return name简称
	 */
	public String getName_jp() {
		return name_jp;
	}

	/**
	 * 设置name简称
	 * 
	 * @param name_jp
	 *            name简称
	 */
	public void setName_jp(String name_jp) {
		this.name_jp = name_jp;
	}

	/**
	 * 得到poi英文名
	 * 
	 * @return poi英文名
	 */
	public String getName_en() {
		return name_en;
	}

	/**
	 * 设置poi英文名
	 * 
	 * @param name_en
	 *            poi英文名
	 */
	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	/**
	 * 得到poi全称
	 * 
	 * @return poi全称
	 */
	public String getName_qp() {
		return name_qp;
	}

	/**
	 * 设置poi全称
	 * 
	 * @param name_qp
	 *            poi全称
	 */
	public void setName_qp(String name_qp) {
		this.name_qp = name_qp;
	}

	/**
	 * 得到poi样式
	 * 
	 * @return poi样式
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * 设置poi样式
	 * 
	 * @param style
	 *            poi样式
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * 得到poi编号或者id
	 * 
	 * @return poi编号或者id
	 */
	public int getPoiNo() {
		return poiNo;
	}

	/**
	 * 设置poi编号或者id
	 * 
	 * @param poiNo
	 *            poi编号或者id
	 */
	public void setPoiNo(int poiNo) {
		this.poiNo = poiNo;
	}

	/**
	 * 得到poi名字
	 * 
	 * @return poi名字
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置poi名字
	 * 
	 * @param name
	 *            poi名字
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 得到楼层
	 * 
	 * @return
	 */
	public String getFloor() {
		return floor;
	}

	/**
	 * 设置楼层
	 * 
	 * @param floor
	 */
	public void setFloor(String floor) {
		this.floor = floor;
	}

	/**
	 * 得到poi的x坐标
	 * 
	 * @return
	 */
	public float getX() {
		return x;
	}

	/**
	 * 设置poi的x坐标
	 * 
	 * @param x
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * 得到poi的y坐标绝对值
	 * 
	 * @return
	 */
	public float getY_abs() {
		return Math.abs(y);
	}

	/**
	 * 得到poi的y坐标
	 * 
	 * @return
	 */
	public float getY() {
		return -Math.abs(y);
	}

	/**
	 * 设置poi的y坐标
	 * 
	 * @param y
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * 得到类别ID
	 * 
	 * @return
	 */
	public String getClassid() {
		return classid;
	}

	/**
	 * 设置类别ID
	 * 
	 * @param classid
	 */
	public void setClassid(String classid) {
		this.classid = classid;
	}

	/**
	 * 得到类别名称
	 * 
	 * @return
	 */
	public String getClassname() {
		return classname;
	}

	/**
	 * 设置类别名称
	 * 
	 * @param classname
	 */
	public void setClassname(String classname) {
		this.classname = classname;
	}

	/**
	 * 得到POI类型
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 设置poi类型
	 * 
	 * @param mtype
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * 无参构造方法，其他参数可通过set方法补充
	 */
	public POI() {
	}

	/**
	 * 包含POI必备参数的构造方法
	 * 
	 * @param id
	 *            poi编号
	 * @param name
	 *            名字
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            楼层，例：F2
	 * @param x
	 *            x坐标，单位：米
	 * @param y
	 *            y坐标，单位：米
	 */
	public POI(int id, String name, String buildId, String floor, float x,
			float y) {
		setPoiNo(id);
		this.x = x;
		this.y = y;
		this.name = name;
		this.buildId = buildId;
		setFloor(floor);
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return 建筑物ID
	 */
	public String getBuildId() {
		return buildId;
	}

	/**
	 * 设置建筑物ID
	 * 
	 * @param buildId
	 *            建筑物ID
	 */
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	/**
	 * 得到建筑物名字
	 * 
	 * @return 建筑物名字
	 */
	public String getBuildName() {
		return buildName;
	}

	/**
	 * 设置建筑物名字
	 * 
	 * @param build_name
	 *            建筑物名字
	 */
	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	/**
	 * 得到室内外标记
	 * 
	 * @return 室内外标记
	 */
	public String getIsInside() {
		return isInside;
	}

	/**
	 * 设置室内外标记
	 * 
	 * @param is_inside
	 *            室内外标记
	 */
	public void setIsInside(String isInside) {
		this.isInside = isInside;
	}

	/**
	 * 得到POI绘制样式
	 * @return POI图形样式，参见{@link DrawStyle}
	 */
	public DrawStyle getDrawStyle() {
		return drawStyle;
	}

	/**
	 * 设置POI绘制样式，支持定义POI图形块颜色，边线颜色，边线宽度
	 * @param drawStyle POI图形样式，参见{@link DrawStyle}
	 */
	public void setDrawStyle(DrawStyle drawStyle) {
		this.drawStyle = drawStyle;
	}

	/**
	 * 得到文字绘制样式
	 * @return 文字样式，参见{@link TextStyle}
	 */
	public TextStyle getTextStyle() {
		return textStyle;
	}

	/**
	 * 设置文字样式，支持自定义文字颜色，文字大小
	 * @param textStyle 文字样式，参见{@link TextStyle}
	 */
	public void setTextStyle(TextStyle textStyle) {
		this.textStyle = textStyle;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCurrecy() {
		return currecy;
	}

	public void setCurrecy(String currecy) {
		this.currecy = currecy;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(String logoImage) {
		this.logoImage = logoImage;
	}

	public String getPoiImage() {
		return poiImage;
	}

	public void setPoiImage(String poiImage) {
		this.poiImage = poiImage;
	}

}
