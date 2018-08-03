package com.rtmap.wifipicker.util;

/**
 * 定位接口常量，用于配置动态库和保存相关的静态常量
 * 
 * @author lixinxin
 */

public interface ConstantLoc {

    /** 地理所服务器下载指纹数据的地址 **/
    public static final String WEB_LOAD_FINGER_ROOT = "http://wang.rtlbs.com/wifi/version_3/";
    /** 地理所服务器定位结果上传 **/
    public static final String WEB_COMMIT_LOCATE = "http://wang.rtlbs.com/api_cir_q_2.php";
    /** 地理所服务器定位结果上传 **/
    public static final String WEB_STATUS = "http://wang.rtlbs.com/connect.php";
    /** 地理所服务器定更新指纹库 **/
    public static final String WEB_UPDATA = "http://wang.rtlbs.com/api_wifi_version.php";

    // /** 公司服务器下载指纹数据的地址 **/
    // public static final String WEB_LOAD_FINGER_ROOT = "http://192.168.1.126/wifi/version_3/";
    // /** 公司服务器定位结果上传 **/
    // public static final String WEB_COMMIT_LOCATE = "http://192.168.1.126/api_cir_q_2.php";

    /** 定位输入的最大长度（字符数） **/
    public static final int LOCATE_INPUT_LEN = 4000;
    /** 定位定位楼层信息为空 **/
    public static final String LOCATE_FLOOR_NULL = "0";
    /** 定位建筑物信息为空 **/
    public static final String LOCATE_BUILD_NULL = "0";

    /** 加速度计缓存大小 **/
    public static final int SENSOR_ACC_BUFFER_MAX = 100;
    /** 方向传感器 **/
    public static final int SENSOR_ORI_BUFFER_MAX = 10;
    /** 压力传感器 **/
    public static final int SENSOR_PRESS_BUFFER_MAX = 10;
    /** 实时定位时，wifi数据缓存的最大值 **/
    public static final int INPUT_WIFI_BUFFER_MAX = 100;

    /**
     * 通过UIEvent中handler机制进行消息通信的消息id
     * @author hotstar
     * 
     */
    public static class UIEventCode {
        /** 定位结果信息 **/
        public static final int WIFI_SCAN = 0x00020;
        public static final int WIFI_SCAN_END = 0x00021;
        public static final int NO_WIFI_SIGNAL_REMINDER = 0x00039;//WIFI扫描信息为空提示
        public static final int NO_NET_SIGNAL_REMINDER = 0x00040; //无连接
    }

    /**
     * 错误码
     * @author hotstar
     * 
     */
    public static class ErrCodeLoc {
        /** 正常 **/
        public static final int ERR_SUCCESS = 0x000;
        /** wifi无效 **/
        public static final int ERR_WIFI_ENABLE = 0x001;
        /** sd卡无效 **/
        public static final int ERR_SD_ENABLE = 0x002;
        /** 定位输出无效 **/
        public static final int LOCATE_FLOOR_ERR = 0x003;
        /** 当前建筑物指纹数据不存在 **/
        public static final int ERR_BUILD_DATA_NULL = 204;
        /** 输出信息为空 **/
        public static final int ERR_DATA_NULL = 04;
    }

    /**
     * 定位模式配置选择项 </br>
     * 说明：模式位一共有32位，每一位对应一个bit位
     * @author hotstar
     * 
     */
    public static class ModeLoc {
        /**
         * 正常模式,功能如下：</br> 1、不显示log信息；</br>2、不保存任何文件记录；</br>
         * 3、不启用webservice定位；</br>4、不启用离线调试模式；</br>5、不启用连续定位模式；</br>
         * 6、不用用平滑输出模式；</br>7、所有传感器都有效
         **/
        public static final int MODE_NORMAL = 0x000080f0;
        /** 加速度传感器是否有效,传感器无效时其它传感器都无效（除wifi外） **/
        public static final int MODE_SERNSOR_ACCELEROMETER = 0x00000001;
        /** 方向传感器是否有效 **/
        public static final int MODE_SERNSOR_ORIENTATION = 0x00000002;
        /** 气压传感器是否有效 **/
        public static final int MODE_SERNSOR_PRESSURE = 0x00000004;
        /** 磁力传感器是否有效 **/
        public static final int MODE_SERNSOR_MAGNETIC = 0x00000008;
        /** wifi是否有效 **/
        public static final int MODE_SERNSOR_WIFI = 0x00000020;
        /** 是否使用webservice进行定位运算 **/
        public static final int MODE_LOCATE_WEBSERVICE = 0x00000100;
        /** 是否进行手动楼层切换 **/
        public static final int MODE_LOCATE_SWITCH_FLOOR = 0x00000200;
        /** 是否进入连续定位模式 **/
        public static final int MODE_LOCATE_CONTINUOUS = 0x00000400;
        /** 离线数据调试模式 **/
        public static final int MODE_OFFLINE_DEBUG = 0x00000800;
        /** 保存实时定位数据至文件中，以便离线调试 **/
        public static final int MODE_FILE_LOCATE_INPUT = 0x00001000;
        /** 是否打log **/
        public static final int MODE_LOG_LOCATE = 0x00002000;
        /** 定位输出平滑显示模式 **/
        public static final int MODE_POINT_SMOOTH = 0x00004000;
        /** 下载指纹点数据 **/
        public static final int MODE_LOAD_DATA = 0x00008000;
    }
}