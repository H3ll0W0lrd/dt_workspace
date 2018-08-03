// 输入到定位动态库中的xml数据
// xml格式：
// <Locating vision = 1>
// <userid> 1234 </userid>
// <time> 12314 </time>
// <phoneType> 23413122 </phoneType>
// <mpfilepath fileType=1> "4564247"</mpfilepath>
// <gatherCount>1</gatherCount>
//
// <gatherAp count=1 gatherTime=12312564>
// <ap mac=54545FF34BAC rssi=24/>
// </gatherAp>
// <gatherSensor type= 1>
// <opt>362.1</opt>
// <ax>21.1</ax>
// <ay>53</ay>
// <az>12.58</az>
// </gatherSensor>
// <gatherSensor type= 2>
// <compass>362.1</compass>
// <cx>21.1</cx>
// <cy>53</cy>
// <cz>12.58</cz>
// </gatherSensor>
// </Locating>
package com.rtmap.wifipicker.wifi;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.rtmap.wifipicker.util.ConfigLoc;
import com.rtmap.wifipicker.util.ConstantLoc;
import com.rtmap.wifipicker.util.ConstantLoc.ErrCodeLoc;
import com.rtmap.wifipicker.util.ConstantLoc.ModeLoc;
import com.rtmap.wifipicker.util.UtilLoc;

public class LocationIndoorInput {
    private String TAG = "LocInputEntity";
    private static LocationIndoorInput instance = null;

    /** 离线定位输入数据 **/
    public static String FILE_OFF_DATA = ".off";
    /** 版本号 **/
    public int vision;
    /** 用户id **/
    public int userid;
    /** 时间戳 **/
    public long time;
    /** 手机型号 **/
    public String phoneType;
    /** 指纹库文件路径 **/
    public String mpfilepath1;
    /** 指纹库文件类型 **/
    public int mpfileType1;
    /** 指纹库文件路径 **/
    public String mpfilepath2;
    /** 指纹库文件类型 **/
    public int mpfileType2;
    /** 采集的次数 **/
    public int gatherCount;
    /** 是否启动建筑物判断 **/
    public int resureBuild;

    /** 加速度传感器与电子罗盘的信息buffer,数据存储需要和{@link lockObject}一起使用 **/
    private LocSensorDt sensorsVal;
    /** 采集到的wifi数据包 **/
    // private List<LocWifiGather> wifiGatherBuffer;
    /** 采集到的wifi数据统计信息 **/
    private Map<String, ApRssValue> wifiAps = new HashMap<String, ApRssValue>();

    /** 同步锁，用于pdr数据的一致性 **/
    private static Object lockSensors = new Object();
    /** 同步锁，用于wifi数据的一致性 **/
    private static Object lockWifi = new Object();

    // /** 是否保存实时采集到到信息 **/
    // private boolean isSaveWifi = false;

    public class ApRssValue {
        public String mac;
        public int rss;
        public int count;

        public ApRssValue(String m, int r, int c) {
            mac = m;
            rss = r;
            count = c;
        }
    }

    private LocationIndoorInput() {
        mpfilepath1 = "860100010020300001/";
        mpfilepath2 = ConstantLoc.LOCATE_FLOOR_NULL;
        userid = 2012;
        time = UtilLoc.getCurTimeMillis();
        phoneType = "MI-ONE_Plus";
        sensorsVal = new LocSensorDt();
    }

    /**
     * 设置输入参数
     * 
     * @param build
     *        {@link String} 建筑物 eg:"860100010020300001"
     * @param floor
     *        {@link int} 楼层 eg:20
     */
    public void setData(String build, int floor) {
        mpfilepath1 = build;
        setData(floor);
    }

    /**
     * 转换得到楼层编号
     * 
     * @param floor
     *        {@link String} 楼层<br/>
     *        "10" "f1" "F1"----代表1层<br/>
     *        "20" "f2" "F2"----代表2层<br/>
     *        "25" "f2.5" "F2.5"----代表2层与三层间的夹层<br/>
     *        "-10" "b1" "B1"----代表地下1层<br/>
     *        ""空字符串----代表自动切换模式
     */
    public int convertFloor(String floor) {
        int ret = 0;
        String tmp = "";

        if (floor.equals("")) {
            return ErrCodeLoc.LOCATE_FLOOR_ERR;
        }

        if (UtilLoc.isNumeric(floor))
            tmp = floor;
        else {
            // 去除包含小数点的问题
            if (floor.contains(".")) {
                int index = floor.indexOf(".");
                floor = floor.substring(0, index) + floor.substring(index);
            } else {
                floor = floor.substring(0) + "0";
            }

            // 判断前面是否有F/B字符，保持与1.0版本兼容
            String firstChar = floor.substring(0, 1);
            if (firstChar.equals("f") || firstChar.equals("F")) {
                tmp = floor.substring(1);
            } else if (firstChar.equals("b") || firstChar.equals("B")) {
                tmp = "-" + floor.substring(1);
            }
        }

        try {
            ret = Integer.parseInt(tmp);
        } catch (Exception e) {
            ret = ErrCodeLoc.LOCATE_FLOOR_ERR;
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 
     * @param floor
     *        {@link int}<br/>
     *        10 ----代表1层<br/>
     *        20 ----代表2层<br/>
     *        25 ----代表2层与三层间的夹层<br/>
     *        -10----代表地下1层<br/>
     *        0 ----代表自动切换模式
     */
    public void setData(int floor) {
        mpfilepath2 = "" + floor;
    }

    /**
     * 设置输入参数
     * 
     * @param build
     *        {@link String} 建筑物
     * @param floor
     *        {@link int} 楼层
     * @param phonetype
     *        {@link String} 手机型号
     * @param userId
     *        {@link int} 用户id
     */
    public void setData(String build, int floor, String phonetype, int userId) {
        setData(build, floor);
        phoneType = phonetype;
        userid = userId;
    }

    public void putWifiData(LocWifiGather wifiGather) {
        synchronized (lockWifi) {
            // 把数据存放到hash表中
            for (LocWifiGatherAp ap : wifiGather.apList) {
                if (wifiAps.containsKey(ap.mac)) {
                    wifiAps.get(ap.mac).rss += ap.rss;
                    wifiAps.get(ap.mac).count++;
                } else {
                    wifiAps.put(ap.mac, new ApRssValue(ap.mac, ap.rss, 1));
                }
            }
        }
    }

    /**
     * 对apr采集得到数据进行限定，得到它的步长长度
     * 
     * @param timeBefore
     *        前一次采集的时间
     * @param timeCurrent
     *        当前时间
     * @param count
     *        每秒PDR采集次数
     */
    private double filterSensorInfo(long timeBefore, long timeCurrent, int inSize, int count) {
        double rato = (1000.0 * inSize) / (count * (timeCurrent - timeBefore));
        return rato > 1 ? rato : 1;
    }

    /** 得到该类对象的一个单例 **/
    public static LocationIndoorInput getInstance() {
        synchronized (lockSensors) {
            if (instance == null) {
                instance = new LocationIndoorInput();
            }
        }
        return instance;
    }

    public String getXml() {
        // 采集数据无效则返回空字符串，此时不应该传入定位库进行运算
        if (wifiAps.size() <= 0) {
            return "";
        }

        long timeBefore = time; // 前一次的时间
        time = UtilLoc.getCurTimeMillis();

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startTag(null, "Locating");
            serializer.attribute(null, "vision", "1");

            serializer.startTag(null, "userid");
            serializer.text(userid + "");
            serializer.endTag(null, "userid");

            serializer.startTag(null, "time");
            serializer.text(time + "");
            serializer.endTag(null, "time");

            serializer.startTag(null, "phoneType");
            serializer.text(phoneType + "");
            serializer.endTag(null, "phoneType");

            serializer.startTag(null, "root");
            serializer.text(ConfigLoc.getLibRoot());
            serializer.endTag(null, "root");

            if (!mpfilepath1.endsWith("0")) {
                serializer.startTag(null, "mpfilepath");
                serializer.attribute(null, "fileType", "1");
                serializer.text(mpfilepath1);
                serializer.endTag(null, "mpfilepath");
                mpfilepath1 = "0";
            }

            if (!mpfilepath2.endsWith("0")) {
                serializer.startTag(null, "mpfilepath");
                serializer.attribute(null, "fileType", "2");
                serializer.text(mpfilepath2 + "");
                serializer.endTag(null, "mpfilepath");
                mpfilepath2 = "0";
            }

            if (resureBuild == 1) {
                resureBuild = 0;
                serializer.startTag(null, "b");
                serializer.text("1");
                serializer.endTag(null, "b");
            }

            // ap信息包
            synchronized (lockWifi) {
                serializer.startTag(null, "gatherCount");
                serializer.text("1");
                serializer.endTag(null, "gatherCount");

                serializer.startTag(null, "gatherAp");
                serializer.attribute(null, "count", wifiAps.size() + "");
                serializer.attribute(null, "gatherTime", UtilLoc.getCurTimeMillis() + "");
                int count = 0;
                for (String m : wifiAps.keySet()) {
                    if (++count > ConstantLoc.INPUT_WIFI_BUFFER_MAX)
                        break;
                    serializer.startTag(null, "ap");
                    serializer.attribute(null, "mac", m + "");
                    serializer.attribute(null, "rssi", wifiAps.get(m).rss / wifiAps.get(m).count + "");
                    serializer.endTag(null, "ap");
                }
                serializer.endTag(null, "gatherAp");
                wifiAps.clear();
            }

            // 传感器数据包
            synchronized (lockSensors) {
                if (ConfigLoc.isMode(ModeLoc.MODE_LOCATE_WEBSERVICE)) {
                    sensorsVal.clear();
                }

                if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_ORIENTATION)) {
                    sensorsVal = LocSensors.sensorsVal;
                    int num = 0;

                    // acc
                    if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_ACCELEROMETER)) {
                        List<float[]> acc = sensorsVal.accelerometer_get();
                        num = acc.size();
                        if (num > 0) {
                            String dataString = "";
                            String dTmpx, dTmpy, dTmpz;
                            double ratoLen = filterSensorInfo(timeBefore, time, num, 50); // 传入固定条数的数据
                            serializer.startTag(null, "sensors");
                            serializer.attribute(null, "type", 1 + "");
                            float[] f;
                            for (int i = 0, j = 0; i < num; j++, i = (int) (j * ratoLen)) {
                                serializer.startTag(null, "acc");
                                f = acc.get(i);
                                dTmpx = new DecimalFormat("###.##").format(f[0]);
                                dTmpy = new DecimalFormat("###.##").format(f[1]);
                                dTmpz = new DecimalFormat("###.##").format(f[2]);
                                dataString = dTmpx + "," + dTmpy + "," + dTmpz;
                                serializer.text(dataString);
                                serializer.endTag(null, "acc");
                            }
                            serializer.endTag(null, "sensors");
                        }
                    }

                    // orientation
                    if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_ORIENTATION)) {
                        List<Float> ori = sensorsVal.orientation_get();
                        num = ori.size();
                        if (num > 0) {
                            String dataString = "";
                            double ratoLen = filterSensorInfo(timeBefore, time, num, 50); // 传入固定条数的数据
                            serializer.startTag(null, "sensors");
                            serializer.attribute(null, "type", 2 + "");
                            for (int i = 0, j = 0; i < num; j++, i = (int) (j * ratoLen)) {
                                serializer.startTag(null, "cp");
                                dataString = new DecimalFormat("###.##").format(ori.get(i));
                                serializer.text(dataString);
                                serializer.endTag(null, "cp");
                            }
                            serializer.endTag(null, "sensors");
                        }
                    }

                    // pressure
                    if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_PRESSURE)) {
                        List<Float> pre = sensorsVal.press_get();
                        num = pre.size();
                        if (num > 0) {
                            String dataString = "";
                            double ratoLen = filterSensorInfo(timeBefore, time, num, 50); // 传入固定条数的数据
                            serializer.startTag(null, "sensors");
                            serializer.attribute(null, "type", 3 + "");
                            for (int i = 0, j = 0; i < num; j++, i = (int) (j * ratoLen)) {
                                serializer.startTag(null, "press");
                                dataString = new DecimalFormat("####.##").format(pre.get(i));
                                serializer.text(dataString);
                                serializer.endTag(null, "press");
                            }
                            serializer.endTag(null, "sensors");
                        }
                    }
                }
            }
            serializer.endTag(null, "Locating");
            serializer.endDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }
}
