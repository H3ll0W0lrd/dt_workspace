package com.rtm.frm.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

import com.rtm.frm.XunluApplication;

public class FileUtil {

	// 用于判断是否进行检查版本
	public static boolean check = true;

	public static int localVersion = 0;// 本地安装版本

	public static String downloadDir = "ajpx/";// 安装目录

	public static File updateDir = null;

	public static File updateFile = null;

	/***
	 * 创建文件
	 */
	public static void createFile(String name) {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			updateDir = new File(Environment.getExternalStorageDirectory() + "/" + downloadDir);
			updateFile = new File(updateDir + "/" + name + ".apk");
			Log.i("TAG", "创建文件地址：" + updateDir + "/" + name + ".apk");
			if (!updateDir.exists()) {
				updateDir.mkdirs();
			}
			if (!updateFile.exists()) {
				try {
					updateFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static File createFileIfNotFound(String path) {
		File file = new File(path);
		try {
			if (file.exists()) {
				return file;
			} else {
				if (file.createNewFile()) {
					return file;
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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
			e.printStackTrace();
			return false;
		}
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
				e.printStackTrace();
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * 
	 * 方法描述 : 检测SD卡存储状态 创建者：veekenwong 版本： v1.0 创建时间： 2014-1-21 上午11:08:54
	 * 
	 * @return boolean
	 */
	public static boolean checkExternalStorageState() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkTempDir() {
		return checkTempDir(ConstantsUtil.DIR_NAME);
	}

	public static boolean checkTempDir(String path) {
		if (!checkExternalStorageState()) {
			return false;
		}
		File tf = new File(path);
		if (!tf.exists()) {
			try {
				return tf.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
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

	public static void copyAssets(String source, String dest, String name) {
		String path = String.format("%s%s", dest, name);
		if (checkFile(path)) {
			return;
		}

		try {
			InputStream in = XunluApplication.mApp.getAssets().open(String.format("%s/%s", source, name));
			OutputStream out = new FileOutputStream(path);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File getFile(String path) {
		if (!checkTempDir()) {
			return null;
		}
		File file = new File(path);

		try {
			if (file.exists()) {
				return file;
			} else {
				return null;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File fileObject(String path) {
		if (checkTempDir() == false) {
			return null;
		}
		File file = new File(path);
		return file;
	}

	public static String getTextFromAssets(String file) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		try {
			AssetManager assetManager = XunluApplication.mApp.getAssets();
			InputStream inputStream = assetManager.open(file);
			while ((length = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outputStream.toString();
	}

	@SuppressWarnings("resource")
	public static byte[] readFileData(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[fis.available()];
			fis.read(data);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean saveImage(Bitmap bitmap, String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			if (fos != null) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
				fos.flush();
				fos.close();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean cachePhoto(String url, Bitmap bitmap, CompressFormat format, String folder) {
		if (checkDir(folder)) {
			String fileName = XunluUtil.toMd5(url);
			// 写文件到文件中
			try {
				File file = new File(fileName);
				if (file.exists()) {
					file.delete();
				}

				FileOutputStream out = new FileOutputStream(new File(folder + fileName));
				bitmap.compress(format, 100, out);
				out.flush();
				out.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	public static Bitmap getSdCachePhoto(String url, String folder) {
		String fileName = XunluUtil.toMd5(url);
		if (checkFile(folder + fileName)) {
			File file = new File(folder + fileName);
			if (file.length() == 0) {
				file.delete();
				return null;
			}

			Bitmap bitmap = XunluUtil.decodeBitmap(folder + fileName);
			return bitmap;
		}

		return null;
	}

	public static InputStream getStreamFromFile(String path) {
		if (XunluUtil.isEmpty(path)) {
			return null;
		}

		try {
			File file = new File(path);
			return new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public static String getFormatNowDate() {
		  Date nowTime = new Date(System.currentTimeMillis());
		  SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  String retStrFormatNowDate = sdFormatter.format(nowTime);
		  return retStrFormatNowDate;
		}

	public static String getAppSDCardFileDir() {
		// 得到存储卡路径
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡
		// 或可存储空间是否存在
		if (sdCardExist) {
			File f = Environment.getExternalStorageDirectory();
			sdDir = new File(f, "XunluApp"); // 错误日志存储到SD卡autonavi目录下
			if (!sdDir.exists()) {
				sdDir.mkdir();
			}
		}
		if (sdDir == null)
			return null;

		return sdDir.toString();
	}
}
