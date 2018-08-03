/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;


public class Constants {
	public static final int buffer = 1;
	public static final boolean ROTATE = true;
	public static final boolean LOG = false;
	public static final int NET_MSG_GET_LENGTH = 900002;
	public static final int RTMAP_MAP = 100;
	public static final int MAP_LOAD_START = 1;
	public static final int MAP_REFRESH = 401;
	public static final int MAP_PARTIAL_REFRESH = 402;
	public static final int MAP_LOAD_END = 2;
	/**
	 * Message的what参数，标记License验证结果
	 */
	public static final int MAP_LICENSE = 301;
	public static final int POI_STYLE_PARK = 201;
	public static final int MAP_NOExistSdCard = 901;
	public static final int MAP_FailCheckNet = 902;
	public static final int MAP_FailNetResult = 903;
	public static final int MAP_Down_Success = 904;
	public static final int MAP_Down_Fail = 905;
	public static final int MAP_Update_Success = 906;
	public static final int MAP_Update_Fail = 907;

	public static final int POINTPT = 4;
	public static final double RATIO = 1.6;
	public static final int ICONWIDTH = 25;

	/**
	 * 一英寸代表实际多少米，此参数用于位图显示时候计算，矢量图舍弃
	 */
	// public static final float MAP_IC_M = 0.0254F;
	/**
	 * 一英寸代表多少像素，由于屏幕的大小不一致，一般情况下，普通和2K高清屏之间的像素差还是很大的，此参数用于位图显示计算，矢量图舍弃
	 */
	// public static final float MAP_DPI = 96f;
	
	public static final int ACTION_MASK = 255;
	public static final int ACTION_POINTER_ID_SHIFT = 8;
	public static final int ACTION_POINTER_DOWN = 5;
	public static final int ACTION_POINTER_UP = 6;
	public static final int ACTION_DOWN = 0;
	public static final int ACTION_UP = 1;
	public static final int ACTION_MOVE = 2;

	public static final int LOCATION_ERROR = 10;

	public static final String URL_PATH = "map_2/";
	public static final String WEB_UPDATA = "api_imap_version_2.php";
	public static final String CHECK_MD5 = "api_imap_check.php";
	public static final float POI_DENSITY = 1.2f;// poi瀵嗗害璋冭妭锛屾暟鍊艰秺澶oi瓒婄枏锛屼笉鑳戒负0.榛樿涓�
	public static final int DATA_VERSION = 20;
	public static final int MIN_PIXEL = 20;// 灏忎簬璇ュぇ灏忕殑闈笉鏄剧ず
	public static final int LOCATIONS_TIMES = 30;

	public static final boolean COUPON_PUSH = true;
	public static final float COUPON_DISTANCE = 20;

	public static final int POPUINDEX_TAP = 1;
	public static final int POPUINDEX_NAVI = 2;
	public static final int POPUINDEX_COUPON = 3;

	public static final int ICON_LOGO = 101;
	public static final int DRAW_LOGO = 101;

	public static final int TOP_RIGHT = 0;
	public static final int TOP_LEFT = 1;
	public static final int BOTTOM_RIGHT = 2;
	public static final int BOTTOM_LEFT = 3;
	public static final int COMAPASS_CUSTOM = 4;

	public static final float DEFAULT_SCALE = 350F;
	public static final float DISTANCE = 10f;
	public static final float density = 1f;
	public static float VIEWHIGHT = 0;
	public static float VIEWWIDTH = 0;

	public static final float SCALE_INFINITY = 2983;
	public static final int TAP_STATE_NORMAL = 101;
	public static final int TAP_STATE_PIN_KEEP = 102;
	public static final int TAP_STATE_POPUP_KEEP = 103;

	// public static int MAP_BACKGROUND_COLOR = 0xffffffff;
	public static float MAP_SKEW_ANGLE = 30;

}
