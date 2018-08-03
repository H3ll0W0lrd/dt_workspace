package com.rtm.frm.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.rtm.frm.model.Build;
import com.rtm.frm.model.Floor;

/**
 * @author liyan
 * @explain 建筑物列表xml字符串解析工具类
 */
public class BuildsParseUtil {
	
	public List<Build> mBuilds;
	
	public List<Floor> mFloors;
	
	private boolean isPrivate;
	
	/**
	 * 将xml字符串转换成建筑物对象list
	 * @param data
	 */
	public BuildsParseUtil(String data , boolean isPrivateBuild){
		mBuilds = new ArrayList<Build>();
		mFloors = new ArrayList<Floor>();
		isPrivate = isPrivateBuild;
		if(isPrivateBuild){
			parsePrivateBuilds(data);
		} else {
			parse(data);
		}
	}
	
	/**
	 * @author liYan
	 * @version  创建时间：2014-8-15 上午11:19:18
	 * @explain  针对http://open2.rtmap.net/api_buildlist_all_4.php接口的xml数据格式进行解析
	 * @param data
	 */
	private void parse(String data){
		Element element = XmlUtil.getRootElement(data);
		NodeList cityNodeList = element.getElementsByTagName("city");
		int lengthOfCitys = cityNodeList.getLength();
		
		// 百度坐标转换工具
		CoordinateConverter coord = new CoordinateConverter();
		coord.from(CoordinateConverter.CoordType.COMMON);
		
		for(int i = 0;i < lengthOfCitys; ++i) {
			Node cityNode = cityNodeList.item(i);
			
			NodeList buildList = ((Element) cityNode).getElementsByTagName("build");
			int lengthOfBuilds = buildList.getLength();
			for(int j = 0;j < lengthOfBuilds;++j) {
				Node buildNode = buildList.item(j);				
				Build buildObj = new Build();
				
				buildObj.cityName = XmlUtil.getValueByTag((Element) cityNode,"city_name");//在cityNode里面获取cityName
				
				buildObj.id = XmlUtil.getValueByTag((Element) buildNode,"id");
				buildObj.name = XmlUtil.getValueByTag((Element) buildNode,"name");
				buildObj.size = XmlUtil.getValueByTag((Element) buildNode,"size");
				buildObj.nameJp2 = XmlUtil.getValueByTag((Element) buildNode,"name_jp_2");
				buildObj.googleLat = XmlUtil.getValueByTag((Element) buildNode,"lat");
				buildObj.googleLng = XmlUtil.getValueByTag((Element) buildNode,"long");
				buildObj.floors = XmlUtil.getValueByTag((Element) buildNode,"floors");
				buildObj.versionData = XmlUtil.getValueByTag((Element) buildNode,"version_data");
				buildObj.versionMap = XmlUtil.getValueByTag((Element) buildNode,"version_map");
				buildObj.isPrivate = isPrivate?1:0;
				
				LatLng latLng = new LatLng(Float.valueOf(buildObj.googleLat),// 原始Google坐标
						Float.valueOf(buildObj.googleLng));
				latLng = coord.coord(latLng).convert();// 转换为百度坐标
				buildObj.lat = latLng.latitude + "";
				buildObj.lng = latLng.longitude + "";
				
				NodeList floorList = ((Element) buildNode).getElementsByTagName("floors_maps_tile");
				int lengthOfFloors = floorList.getLength();
				for(int k = 0;k < lengthOfFloors;++k) {
					Node floorNode = floorList.item(k);
					Floor floorObj = new Floor();
					
					floorObj.buildId = buildObj.id;
					floorObj.buildName = buildObj.name;
					floorObj.floor = XmlUtil.getValueByTag((Element) floorNode,"floor");
					floorObj.description = XmlUtil.getValueByTag((Element) floorNode,"description");
					floorObj.description_1 = XmlUtil.getValueByTag((Element) floorNode,"description_1");
					floorObj.description_ = XmlUtil.getValueByTag((Element) floorNode,"description_");
					floorObj.width = XmlUtil.getValueByTag((Element) floorNode,"width");
					floorObj.height = XmlUtil.getValueByTag((Element) floorNode,"height");
					floorObj.levelTile = XmlUtil.getValueByTag((Element) floorNode,"level_tile");
					floorObj.isPrivate = isPrivate?1:0;
					mFloors.add(floorObj);
				}
				mBuilds.add(buildObj);
			}
		}
	}
	
	/**
	 * @author liYan
	 * @version  创建时间：2014-8-15 上午11:19:18
	 * @explain  针对http://open2.rtmap.net/api_ext_login.php接口的xml数据格式进行解析
	 * @param data
	 */
	private void parsePrivateBuilds(String data){
		Element element = XmlUtil.getRootElement(data);
		
		// 百度坐标转换工具
		CoordinateConverter coord = new CoordinateConverter();
		coord.from(CoordinateConverter.CoordType.COMMON);
			
		NodeList buildList = element.getElementsByTagName("build");
		int lengthOfBuilds = buildList.getLength();
		for(int j = 0;j < lengthOfBuilds;++j) {
			Node buildNode = buildList.item(j);				
			Build buildObj = new Build();
			
			buildObj.id = XmlUtil.getValueByTag((Element) buildNode,"id");
			buildObj.name = XmlUtil.getValueByTag((Element) buildNode,"name");
			buildObj.size = XmlUtil.getValueByTag((Element) buildNode,"size");
			buildObj.nameJp2 = XmlUtil.getValueByTag((Element) buildNode,"name_jp_2");
			buildObj.googleLat = XmlUtil.getValueByTag((Element) buildNode,"lat");
			buildObj.googleLng = XmlUtil.getValueByTag((Element) buildNode,"long");
			
			LatLng latLng = new LatLng(Float.valueOf(buildObj.googleLat),// 原始Google坐标
					Float.valueOf(buildObj.googleLng));
			latLng = coord.coord(latLng).convert();// 转换为百度坐标
			buildObj.lat = latLng.latitude + "";
			buildObj.lng = latLng.longitude + "";
			
			buildObj.floors = XmlUtil.getValueByTag((Element) buildNode,"floors");
			buildObj.versionData = XmlUtil.getValueByTag((Element) buildNode,"version_data");
			buildObj.versionMap = XmlUtil.getValueByTag((Element) buildNode,"version_map");
			buildObj.isPrivate = isPrivate?1:0;
			
			NodeList floorList = ((Element) buildNode).getElementsByTagName("floors_maps_tile");
			int lengthOfFloors = floorList.getLength();
			for(int k = 0;k < lengthOfFloors;++k) {
				Node floorNode = floorList.item(k);
				Floor floorObj = new Floor();
				
				floorObj.buildId = buildObj.id;
				floorObj.buildName = buildObj.name;
				floorObj.floor = XmlUtil.getValueByTag((Element) floorNode,"floor");
				floorObj.description = XmlUtil.getValueByTag((Element) floorNode,"description");
				floorObj.description_1 = XmlUtil.getValueByTag((Element) floorNode,"description_1");
				floorObj.description_ = XmlUtil.getValueByTag((Element) floorNode,"description_");
				floorObj.width = XmlUtil.getValueByTag((Element) floorNode,"width");
				floorObj.height = XmlUtil.getValueByTag((Element) floorNode,"height");
				floorObj.levelTile = XmlUtil.getValueByTag((Element) floorNode,"level_tile");
				floorObj.isPrivate = isPrivate?1:0;
				mFloors.add(floorObj);
			}
			mBuilds.add(buildObj);
		}
	}
	
}


