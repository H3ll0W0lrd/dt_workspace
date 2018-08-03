package com.rtm.frm.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.util.Log;

import com.rtm.frm.XunluApplication;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.XunluUtil;

/**
 * @author liYan 服务器请求接口封装，通过handler将数据返回
 */
public class PostData {

	private static final String KEY_CURRENT_FLOOR = "current_floor";
	private static final String KEY_X = "coordinate_x";
	private static final String KEY_Y = "coordinate_y";

	/**
	 * @param handler
	 * @param what
	 * @explain 初始化数据请求 若网络有问题，msg.arg1
	 *          会有网络STATE_NET_ERR_UNUSED标志,服务器返回的数据在msg.obj中。
	 */
	@SuppressWarnings("unchecked")
	public static void postInitData(Handler handler, int what) {
		//http://open2.rtmap.net/api_buildlist_all_4.php?id_phone=1&id_apk=8&apk_release_no=1
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(
				ConstantsUtil.KEY_DEVICE_ID,
				XunluApplication.mApp.getDeviceId());
		params.add(param);

		param = new BasicNameValuePair(ConstantsUtil.KEY_ID_APK, "8");//线上8，上海南京路，6
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_RELEASE, "1");//线上1，上海南京路，11
		params.add(param);

		String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
				ConstantsUtil.URL_BUILD_LIST);
		PostAsyncTask task = new PostAsyncTask(url, handler,
				ConstantsUtil.HANDLER_POST_BUILD_LIST);
		task.execute(params);
	}

	/**
	 * @param handler
	 * @param what
	 * @explain 检查更新请求 若网络有问题，msg.arg1
	 *          会有网络STATE_NET_ERR_UNUSED标志,服务器返回的数据在msg.obj中。
	 */
	@SuppressWarnings("unchecked")
	public static void postCheckUpdate(Handler handler, int what) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(
				ConstantsUtil.KEY_DEVICE_ID,
				XunluApplication.mApp.getDeviceId());
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_PHONE_MODEL,
				android.os.Build.MODEL);
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_VERSION,
				XunluApplication.mApp.getCurrentVersion());
		params.add(param);

		String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
				ConstantsUtil.URL_VERSION);
		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
	}

	/**
	 * @param handler
	 * @param what
	 * @param buildId
	 * @param floor
	 * @exception 请求当前楼层共有几个优惠店铺
	 */
//	@SuppressWarnings("unchecked") 
//	public static void postFetchCoupons(Handler handler, int what,
//			String buildId, String floor) {
//		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		BasicNameValuePair param = new BasicNameValuePair(
//				ConstantsUtil.KEY_BUILD_ID, buildId);
//		params.add(param);
//		param = new BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID,
//				XunluApplication.mApp.getDeviceId());
//		params.add(param);
//		param = new BasicNameValuePair(ConstantsUtil.KEY_FLOOR, floor);
//		params.add(param);
//		param = new BasicNameValuePair(ConstantsUtil.KEY_CURRENT_FLOOR,
//				MapActivity.getInstance().getMapShowFloor());
//		params.add(param);
//		params.add(param);
//
//		String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
//				ConstantsUtil.URL_FLOOR_COUPON);
//		PostAsyncTask task = new PostAsyncTask(url, handler, what);
//		task.execute(params);
//	}

	/**
	 * 登录请求
	 * 
	 * @param handler
	 * @param what
	 *            若网络有问题，msg.arg1 会有网络STATE_NET_ERR_UNUSED标志,服务器返回的数据在msg.obj中。
	 */
	@SuppressWarnings("unchecked")
	public static void postLogin(Handler handler, int what, String username,
			String password) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(
				ConstantsUtil.KEY_USER, username);
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID,
				XunluApplication.mApp.getDeviceId());
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_PASSWORD, password);
		params.add(param);

		String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
				ConstantsUtil.URL_USER_LOGIN);

		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
	}

	/**
	 * 根据关键字搜索poi，若floor为空，则x，y值无效 若网络有问题，msg.arg1
	 * 会有网络STATE_NET_ERR_UNUSED标志,服务器返回的数据在msg.obj中。
	 * 
	 * @param handler
	 * @param what
	 * @param buildId
	 * @param keyword
	 * @param currentFloor
	 *            可为空
	 * @param currentX
	 *            可为空
	 * @param currentY
	 *            可为空
	 */
	@SuppressWarnings("unchecked")
	public static void postSearchPoiByKeyword(Handler handler, int what,
			String buildId, String keyword, String currentFloor,
			float currentX, float currentY) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(
				ConstantsUtil.KEY_BUILD_ID, buildId);
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID,
				XunluApplication.mApp.getDeviceId());
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_PLACE_NAME, keyword);
		params.add(param);

		if (currentFloor != null) {
			param = new BasicNameValuePair(KEY_CURRENT_FLOOR, currentFloor);
			params.add(param);
			param = new BasicNameValuePair(KEY_X, String.valueOf(currentX));
			params.add(param);
			param = new BasicNameValuePair(KEY_Y, String.valueOf(currentY));
			params.add(param);
		}
		String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
				ConstantsUtil.URL_SEARCH_KEY);
		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
	}

	
	
	/**
	 * 
	 * 方法描述：将mac地址和绑定的id上传至服务器
	 * @param handler
	 * @param what
	 * @param userId
	 * @param channelId
	 * @param mac
	 */
	@SuppressWarnings("unchecked")
	public static void postPushUserId(Handler handler,int what,String userId,String channelId,String mac) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
      
        BasicNameValuePair param=new  BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID, "1");
        params.add(param);
        
        param = new BasicNameValuePair(
                ConstantsUtil.KEY_PUSH_CHANNELID, channelId);
        params.add(param);
        
        param = new BasicNameValuePair(
                ConstantsUtil.KEY_PUSH_USERID, userId);
        params.add(param);
        
        param = new BasicNameValuePair(
                ConstantsUtil.KEY_PUSH_MAC, mac);
        params.add(param);
        
        String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
        		ConstantsUtil.URL_PUSH_BIND);
//        String url = String.format("%s%s", "http://10.100.56.229/",
//        		ConstantsUtil.URL_PUSH_BIND);

        PostAsyncTask task = new PostAsyncTask(url, handler, what);
        task.execute(params);
    }

	/***
	 * //把当前位置坐标发送给服务器，
	 * @param handler 
	 * @param what 
	 * @param buildId
	 * @param floor 
	 * @param x 当前位置的坐标
	 * @param y 当前位置的坐标
	 * **/
	@SuppressWarnings("unchecked")
	public static void postLocationToService(Handler handler, int what,String buildId,String floor,float x,float y,double lng,double lat){
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param=new  BasicNameValuePair(ConstantsUtil.KEY_MAC, XunluUtil.getMac());
		params.add(param);
		
		param = new BasicNameValuePair(
				ConstantsUtil.KEY_TOSERVICE_BUILDID, buildId);
		params.add(param);
		
		param = new BasicNameValuePair(
				ConstantsUtil.KEY_TOSERVICE_FLOOR, floor);
		params.add(param);
		
		param = new BasicNameValuePair(
				ConstantsUtil.KEY_TOSERVICE_X, x+"");
		params.add(param);
		
		param = new BasicNameValuePair(
				ConstantsUtil.KEY_TOSERVICE_Y, y+"");
		params.add(param);
		
		param = new BasicNameValuePair(
				ConstantsUtil.KEY_TOSERVICE_LNG, lng+"");
		params.add(param);
		
		param = new BasicNameValuePair(
				ConstantsUtil.KEY_TOSERVICE_LAT, lat+"");
		params.add(param);
		
		String url = String.format("%s%s", "http://open2.rtmap.net:6004/",
				"open/api.php");

		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
	}
	
	/**
	 * @explain 获取指定城市的优惠信息
	 * @param handler
	 * @param what
	 * @param cityName
	 * @param page 如果page为0，则返回所有优惠，pageSize参数则失效
	 * @param pageSize
	 * @param important
	 */
	@SuppressWarnings("deprecation")
	public static void postFavorableByCity(Handler handler,int what,String cityName,int page,int pageSize,int important) {
		//http://open2.rtmap.net/shopping/api_poi_tuans_city.php?id_phone=1&id_apk=8&apk_release_no=1&name_city=%E6%B7%B1%E5%9C%B3&pagesize=0&important=1
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(ConstantsUtil.KEY_PAGE,String.valueOf(page));
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID,XunluApplication.mApp.getDeviceId());
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_ID_APK, "8");//线上8，上海南京路，6
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_RELEASE, "1");//线上1，上海南京路，11
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_PAGESIZE, String.valueOf(pageSize));
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_IMPORTANT, String.valueOf(important));
		params.add(param);
		
		try {
//			param = new BasicNameValuePair(ConstantsUtil.KEY_NAME_CITY, URLEncoder.encode(cityName, "GB2312"));
			param = new BasicNameValuePair(ConstantsUtil.KEY_NAME_CITY, cityName);
			params.add(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.e("url param", params.toString());
		
		String url = String.format("%s%s",XunluApplication.mApp.getRootUrl(), ConstantsUtil.URL_FAVORABLE_BY_CITY) ;
		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
		
	}
	
	/**
	 * @explain 根据建筑物ID，楼层获取优惠信息
	 * @param buildId
	 * @param floor 如果floor不传，则获取整个建筑物的优惠信息
	 * */
	public static void postFavorableByBuildIdAndFloor(Handler handler,int what,String buildId,String floor) {
		//http://open2.rtmap.net/shopping/api_poi_tuans_floor.php?id_phone=1&id_build=860100010040300005&floor=B1 
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(ConstantsUtil.KEY_BUILD_ID, buildId);
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID,XunluApplication.mApp.getDeviceId());
		params.add(param);
		
		if(!XunluUtil.isEmpty(floor)) {
			param = new BasicNameValuePair(ConstantsUtil.KEY_FLOOR, floor);
			params.add(param);
		}
		
		String myUrl = XunluApplication.mApp.getRootUrl();
		String myUrl_ = ConstantsUtil.URL_FLOOR_COUPON;
		if ("860100010080300003".equals(buildId)) {
			myUrl = "http://10.100.56.229/";
			myUrl_ = "shopping/api_poi_tuans_floor.php";
		}

		String url = String.format("%s%s",myUrl, myUrl_) ;
		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
	}
	/**
	 * @exception 上传用户位置数据，每分钟传一次
	 * @param buildId
	 * @param floor
	 * @param x
	 * @param y
	 * **/
	public static void postUserPosition(Handler handler,int what,String buildId,String floor,float x,float y) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(ConstantsUtil.KEY_DEVICE_ID,XunluApplication.mApp.getDeviceId());
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_FLOOR, floor);
		params.add(param);
		param = new BasicNameValuePair(KEY_X, String.valueOf(x));
		params.add(param);
		param = new BasicNameValuePair(KEY_Y, String.valueOf(y));
		params.add(param);
		param = new BasicNameValuePair(ConstantsUtil.KEY_BUILD_ID, buildId);
		params.add(param);
		
//		广发的接口
//		String url = String.format("%s%s","http://115.29.44.49:30001/", 
//				ConstantsUtil.URL_USER_LOCATION);
		
//		老版寻鹿的接口
		String url = String.format("%s%s",XunluApplication.mApp.getRootUrl(), 
				ConstantsUtil.URL_USER_LOCATION);
		PostAsyncTask task = new PostAsyncTask(url, handler, what);
		task.execute(params);
	}
	
	@SuppressWarnings("unchecked")
	public static void postSuccBZList(Handler mHandler,int what,String buildId){
//		http://open2.rtmap.net//shopping/api_prize_list.php?userid=2C:54:CF:E4:B7:13
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair param = new BasicNameValuePair(ConstantsUtil.KEY_BZ_SUCC_USER_ID,XunluUtil.getMac());
		params.add(param);
		
		String url = String.format("%s%s", XunluApplication.mApp.getRootUrl(),
				ConstantsUtil.URL_PRIZE_LIST);

		PostAsyncTask task = new PostAsyncTask(url, mHandler, what);
		task.execute(params);
	}
}
