package com.rtm.frm.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.rtm.frm.model.FavorablePoiDbModel;

/**
 * @author liyan
 * @explain 建筑物列表xml字符串解析工具类
 */
public class FavorablePoiParseUtil {

	public List<FavorablePoiDbModel> mPois = new ArrayList<FavorablePoiDbModel>();

	/**
	 * 将xml字符串转换成建筑物对象list
	 * 
	 * @param data
	 */
	public FavorablePoiParseUtil(String data) {
		parse(data);
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-15 上午11:19:18
	 * @explain 针对http://open2.rtmap.net/api_buildlist_all_4.php接口的xml数据格式进行解析
	 * @param data
	 */
	private void parse(String data) {
		Element element = XmlUtil.getRootElement(data);
		NodeList poiNodeList = element.getElementsByTagName("poi");
		int lengthOfPois = poiNodeList.getLength();

		for (int i = 0; i < lengthOfPois; ++i) {
			Node poiNode = poiNodeList.item(i);
			FavorablePoiDbModel poi = new FavorablePoiDbModel();

			poi.cityName = XmlUtil.getValueByTag((Element) poiNode, "name_city");
			poi.buildId = XmlUtil.getValueByTag((Element) poiNode, "id_build");
			poi.poiId = XmlUtil.getValueByTag((Element) poiNode, "id_poi");
			poi.poiName = XmlUtil.getValueByTag((Element) poiNode, "name_poi");
			poi.floor = XmlUtil.getValueByTag((Element) poiNode, "floor");
			poi.poiX = XmlUtil.getValueByTag((Element) poiNode, "x_coord");
			poi.poiY = XmlUtil.getValueByTag((Element) poiNode, "y_coord");
			poi.number = XmlHelper.getValueByTag((Element)poiNode,"poi_no");
			poi.noCardPay = XmlHelper.getValueByTag((Element)poiNode,"no_card");

			NodeList tuanList = ((Element) poiNode).getElementsByTagName("tuans");
			int lengthOfTuans = tuanList.getLength();
			try{
				for (int j = 0; j < lengthOfTuans; ++j) {
					Node tuanNode = tuanList.item(j);
					//如果type_info=3，说明是宝藏，则不添加
					int bz = XmlUtil.getIntValueByTag((Element) tuanNode,"type_info");
					if(bz == 3){
						continue;
					}
					poi.categoryCode = XmlUtil.getValueByTag((Element) tuanNode,"class_two_tuan");
					poi.discription = XmlUtil.getValueByTag((Element) tuanNode,"description");
					poi.idBridge = XmlHelper.getValueByTag((Element) tuanNode, "id_bridge");
					poi.idSite = XmlHelper.getValueByTag((Element) tuanNode, "id_site");
					poi.startTime = XmlUtil.getValueByTag((Element) tuanNode,"starttime");
					poi.endTime = XmlUtil.getValueByTag((Element) tuanNode,"endtime");
					poi.adUrl = XmlUtil.getValueByTag((Element) tuanNode,"image_big");
					poi.adBigUrl = XmlUtil.getValueByTag((Element) tuanNode,"image_big_2");
					mPois.add(poi);
				}
			} catch (Exception e) {
				Log.e("poiname", poi.poiName);
				e.printStackTrace();
				continue;
			}
		}
	}
}
