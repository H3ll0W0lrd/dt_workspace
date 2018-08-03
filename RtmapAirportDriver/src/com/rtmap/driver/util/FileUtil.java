package com.rtmap.driver.util;

import com.rtmap.driver.App;
import com.rtmap.driver.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class FileUtil {
    /***
     * 文件路径
     ****/
    public static final String DIR = "driver";
    /***
     * 乘客照片文件夹
     ****/
    public static final String DIR_PASSENGER = "passengerpic";
    /***
     * Log
     ****/
    public static final String LOG_NAME = "crash.txt";
    /***
     * 定位 文件名
     ***/
    public static final String LOCATION_NAME = "location.txt";
    /***
     * 乘客信息 文件名
     ***/
    public static final String PASSENGER_NAME = "passenger.txt";
    /***
     * 特殊事件 文件名
     ***/
    public static final String SPECIAL_EVENT_NAME = "specialevent.txt";

    /**
     * 将crash log存储到本地
     *
     * @param content
     * @param aFileName
     */
    public static void saveLogToFile(String content, String aFileName) {

        String fileName = MyUtil.getAppSDCardFileRootDir(DIR);
        if (fileName == null)
            return;
        fileName = fileName + "/" + aFileName;

        try {
            new File(fileName).createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAppDiverRootDir() {
        return MyUtil.getAppSDCardFileRootDir(DIR);
    }

    public static void saveFtpUploadToFile(String content) {
        String fileName = MyUtil.getAppSDCardFileRootDir(DIR);
        if (fileName == null)
            return;
        fileName = fileName + "/" + TimeUtil.getFormatNowDate("yyyyMMdd") + "_ftp.txt";

        try {
            new File(fileName).createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 将位置坐标存储到本地
     *
     * @param content
     */
    public static void saveLocationToFile(String content) {

        String fileName = MyUtil.getAppSDCardFileDir(DIR);
        if (fileName == null)
            return;
        String childPath = MyUtil.getChildPathName();
        fileName = fileName + "/" + childPath + "_" + LOCATION_NAME;

        try {
            new File(fileName).createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(TimeUtil.getFormatNowDate() + "_" + content + "\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 将乘客信息存储到本地
     *
     * @param xy     xy坐标，用_分割开
     * @param type   0:qr 1:camera 2:手动加一
     * @param picDir 扫码后的内容或者是图片路径
     */
    public static void savePassengerToFile(String type, String xy, String picDir) {

        String fileName = MyUtil.getAppSDCardFileDir(DIR);
        if (fileName == null)
            return;
        String childPath = MyUtil.getChildPathName();

        fileName = fileName + "/" + childPath + "_" + PASSENGER_NAME;

        try {
            new File(fileName).createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(fileName, true);
            String nowDate = TimeUtil.getFormatNowDate();
            String date = nowDate.split(" ")[0];
            String time = nowDate.split(" ")[1];
            String content = nowDate + "_" + type + "_" + picDir + "_" + xy + "_" + App.getInstance().batteryScale;
            if (type.equals(MainActivity.TYPE_QR) || type.equals(MainActivity.TYPE_QR_ERR)) {
                String searchStr = nowDate + "_" + type + "_" + picDir;
                if (isSaved(searchStr, fileName)) {
                    content += "_U";
                } else {
                    content += "_A";
                }
                writer.write(content + "\r\n");
                writer.close();
            } else {
//                String content = nowDate + "_" + type + "_" + xy + "_" + picDir+"_"+App.getInstance().batteryScale;
                content += "_N";
                writer.write(content + "\r\n");
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 将特殊事件存入log文件
     *
     * @param msg
     */
    public static void saveSpecialEventToFile(String msg) {

        String fileName = MyUtil.getAppSDCardFileDir(DIR);
        if (fileName == null)
            return;
        String childPath = MyUtil.getChildPathName();

        fileName = fileName + "/" + childPath + "_" + SPECIAL_EVENT_NAME;

        try {
            new File(fileName).createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(fileName, true);
            String nowDate = TimeUtil.getFormatNowDate();

            writer.write(nowDate + "_" + msg + "_" + App.getInstance().coordX + "_" + App.getInstance().coordY + "_" + App.getInstance().floor + "\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * 获取当前用户log文件列表
     *
     * @return
     */
    public static LinkedList<File> getLogFilesByDir(String dir) {

        String fileDir = MyUtil.getAppSDCardFileRootDir(DIR) + "/" + dir;

        LinkedList<File> files = new LinkedList<File>();

        File file = new File(fileDir);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; ++i) {
            if (tempList[i].isFile()) {
                files.add(tempList[i]);
            }
        }
        return files;
    }

    public static List<String> getLogDirs() {
        String fileName = MyUtil.getAppSDCardFileRootDir(DIR);

        List<String> logDirs = new ArrayList<String>();
        try {
            File file = new File(fileName);
            File[] tempList = file.listFiles();
            for (int i = 0; i < tempList.length; ++i) {
                if (tempList[i].isDirectory()) {
                    String dirName = tempList[i].getName();
                    String[] dirParts = dirName.split("_");
                    if (dirParts.length > 2) {
                        double dirDate = Double.valueOf(dirParts[0].substring(0, 8));
                        double nowDate = Double.valueOf(TimeUtil.getFormatNowDate("yyyyMMdd"));
                        if (nowDate - dirDate == 0) {//获取当天log目录
//                            deleteDir(tempList[i]);
                            logDirs.add(dirName);
                        }
                    }
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return logDirs;
    }

    /***
     * 清除过期log目录
     */
    public static void cleanLogDir() {
        String fileDir = MyUtil.getAppSDCardFileRootDir(DIR);

        File file = new File(fileDir);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; ++i) {
            if (!tempList[i].isFile()) {
                String dirName = tempList[i].getName();
                String[] dirParts = dirName.split("_");
                if (dirParts.length > 2) {
                    double dirDate = Double.valueOf(dirParts[0].substring(0, 8));

                    Calendar a = Calendar.getInstance();
                    a.add(Calendar.DAY_OF_MONTH, -7);//找到前七天的日期
                    SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMdd");
                    String afterDateStr = sdFormatter.format(a.getTime());
                    double afterDateD = Double.valueOf(afterDateStr);
                    if (dirDate < afterDateD) {//小于7天前的日期
                        deleteDir(tempList[i]);
                    }
//                    double nowDate = Double.valueOf(TimeUtil.getFormatNowDate("yyyyMMdd"));
//
//
//                    if (nowDate - dirDate > 7) {//大于两天
//                        deleteDir(tempList[i]);
//                    }

                }
            } else {
                String name = tempList[i].getName();
                String[] dirParts = name.split("_");
                if (dirParts.length >= 2) {
                    if (name.indexOf("ftp") != -1) {
                        double dirDate = Double.valueOf(dirParts[0].substring(0, 8));
                        Calendar a = Calendar.getInstance();
                        a.add(Calendar.DAY_OF_MONTH, -7);//找到前七天的日期
                        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMdd");
                        String afterDateStr = sdFormatter.format(a.getTime());
                        double afterDateD = Double.valueOf(afterDateStr);
                        if (dirDate < afterDateD) {//小于7天前的日期
                            deleteDir(tempList[i]);
                        }

//                        double nowDate = Double.valueOf(TimeUtil.getFormatNowDate("yyyyMMdd"));
//                        if (nowDate - dirDate > 7) {//大于两天
//                            tempList[i].delete();
//                        }
                    }
                }
            }
        }
    }

    /***
     * 获取当前用户日志文件夹名称
     *
     * @return
     */
    public static String getUserLogDirName() {
        String fileDir = MyUtil.getChildPathName();
        return fileDir;
    }

    /***
     * 判断当前是否为debug模式
     *
     * @return
     */
    public static boolean isDebug() {
        String fileName = MyUtil.getAppSDCardFileRootDir(DIR);
        if (fileName == null)
            return false;
        fileName = fileName + "/debug";

        try {
            if (new File(fileName).exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
    }

    /***
     * 判断内容是否重复
     *
     * @param content
     * @param filePath
     * @return
     */
    private static boolean isSaved(String content, String filePath) {


        File file = new File(filePath);
        if (file.exists()) {

            InputStream instream = null;
            String fileContent = ""; //文件内容字符串
            try {
                instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        fileContent += line + "\n";
                    }
                    instream.close();

                    //nowDate + "_" + type + "_" + picDir
//2015-10-13 15:57:05_1_xxxxx
                    String nowDate = content.split("_")[0].split(" ")[0];
                    String type = content.split("_")[1];
                    String picDir = content.split("_")[2];

                    int index = fileContent.lastIndexOf("_" + type + "_" + picDir);
                    if (index == -1) {
                        return false;
                    } else {
                        String fileDate = fileContent.substring(index - 19, index - 9);
                        if (fileDate.equals(nowDate)) {
                            return true;
                        }
                        return false;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;

    }

    /**
     * 删除空目录
     *
     * @param dir 将要删除的目录路径
     */
    private static void doDeleteEmptyDir(String dir) {
        boolean success = (new File(dir)).delete();
        if (success) {
            System.out.println("Successfully deleted empty directory: " + dir);
        } else {
            System.out.println("Failed to delete empty directory: " + dir);
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


}
