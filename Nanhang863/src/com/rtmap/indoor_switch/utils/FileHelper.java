package com.rtmap.indoor_switch.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileHelper {

    public static boolean checkFile(String path) {
        if (!checkExternalStorageState()) {
            return false;
        }
        try {
            File f = new File(path);
            if (f.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String[] listFiles(String path, FilenameFilter filter) {
        if (!checkExternalStorageState()) {
            return null;
        }
        File f = new File(path);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
            }
        } else {
            return f.list(filter);
        }

        return null;
    }
    /**
	 * 将指定内容保存到sd卡目录下指定文件中
	 * 
	 * @param content
	 *            指定内容
	 * @param aFileName
	 *            指定文件名
	 */
	public static void saveLogToFile(String content, String aFileName) {

		String fileName = getAppSDCardFileDir();
		if (fileName == null)
			return;
		fileName = fileName + "/" + aFileName;
		
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, true);
			writer.write("\r\n-------------------"+getFormatNowDate()+"-------------------\r\n");
			writer.write(content);
			writer.write("\r\n-------------------"+getFormatNowDate()+"-------------------\r\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getAppSDCardFileDir() {
		// 得到存储卡路径
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡
		// 或可存储空间是否存在
		if (sdCardExist) {
			File f = Environment.getExternalStorageDirectory();
			sdDir = new File(f, "Dianxin"); // 错误日志存储到SD卡autonavi目录下
			if (!sdDir.exists()) {
				sdDir.mkdir();
			}
		}
		if (sdDir == null)
			return null;

		return sdDir.toString();
	}
	public static String getFormatNowDate() {
		  Date nowTime = new Date(System.currentTimeMillis());
		  SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  String retStrFormatNowDate = sdFormatter.format(nowTime);
		  return retStrFormatNowDate;
		}

    public static boolean checkDir(String path) {
        if (!checkExternalStorageState()) {
            return false;
        }
        File f = new File(path);
        if (!f.exists()) {
            try {
                return f.mkdir();
            } catch (Exception e) {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean checkExternalStorageState() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkTempDir(String path) {
        if (!checkExternalStorageState()) {
            return false;
        }
        File tf = new File(path);
        if (!tf.exists()) {
            try {
                return tf.mkdir();
            } catch (Exception ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static InputStream getStreamFromFile(String path) {
        File file = getFile(path);
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    public static File getFile(String path) {
        if (!checkExternalStorageState()) {
            return null;
        }

        File file = new File(path);
        try {
            if (file.exists()) {
                return file;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTextFromAssets(String file, Context context) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
        }

        return outputStream.toString();
    }

    public static void copyAssetsFile2Sdcard(String assetsFileName, String targetFile, Context context) {

        try {
            // 新建文件输入流并对它进行缓冲
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(assetsFileName);
            BufferedInputStream inBuff = new BufferedInputStream(inputStream);

            // 新建文件输出流并对它进行缓冲
            FileOutputStream output = new FileOutputStream(targetFile);
            BufferedOutputStream outBuff = new BufferedOutputStream(output);
            
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }

            // 刷新此缓冲的输出流
            outBuff.flush();

            // 关闭流
            inBuff.close();
            outBuff.close();
            output.close();
            inputStream.close();
        } catch (IOException e) {
        }

    }

    public static void copyFile(String source, String target) throws IOException {
        File sourceFile = new File(source);
        File targetFile = new File(target);

        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(input);

        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(output);

        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }

        // 刷新此缓冲的输出流
        outBuff.flush();

        // 关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }
}
