package com.rtmap.wifipicker.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;

import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.model.RMPoint;

/**
 * Created by mwqi on 2014/6/7.
 */
public class DTFileUtils {

	public static final String MT_INFO = "mt_info.xml";// 应用一些基本信息或者每个页面需要的信息
	public static final String MT_SET = "mt_set.xml";// 应用的设置信息

	public static final String ROOT_DIR = "rtmapData0";
	public static final String DOWNLOAD_DIR = "download";
	public static final String CACHE_DIR = "cache";
	public static final String ICON_DIR = "icon";
	public static final String LOG_DIR = "log";
	private static final String DATA_DIR = "data";
	public static final String PREFS_USERNAME = "prefs_username";
	public static final String PREFS_PASSWORD = "prefs_password";
	public static final String PREFS_TOKEN = "prefs_token";

	/** 判断SD卡是否挂载 */
	public static boolean isSDCardAvailable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return true;
		} else {
			return false;
		}
	}

	public static void assetsDataToSD(Context context, String assetsfile,
			String datafile) throws IOException {
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(datafile);
		myInput = context.getAssets().open(assetsfile);
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}
		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

	/**
	 * 得到database文件名
	 * 
	 * @param mapName
	 *            地图名字860100010040500002-F2
	 * @param x
	 *            起始点坐标
	 * @param y
	 * @return
	 */
	public static String getDataBasePath(String mapName, float x, float y,
			String buildId) {
		String userName = WPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, "");
		int startX = (int) (x * 1000);
		int startY = (int) (y * 1000);
		// /sdCard/rtmap/WifiPicker/zizxs/860100010040500002-F2_518_470_1410428628600.db
		return String.format("%s%s/%s_%d_%d_%d.db", Constants.WIFI_PICKER_PATH,
				userName + "/" + buildId, mapName, startX, startY,
				System.currentTimeMillis());
	}

	/** 获取下载目录 */
	public static String getDownloadDir() {
		return getDir(DOWNLOAD_DIR);
	}

	/**
	 * 获取数据
	 * 
	 * @return
	 */
	public static String getDataDir() {
		return getDir(DATA_DIR);
	}

	/** 获取log目录 */
	public static String getLogDir() {
		return getDir(LOG_DIR);
	}

	/** 获取缓存目录 */
	public static String getCacheDir() {
		return getDir(CACHE_DIR);
	}

	/** 获取icon目录 */
	public static String getIconDir() {
		return getDir(ICON_DIR);
	}

	/** 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录 */
	public static String getDir(String name) {
		StringBuilder sb = new StringBuilder();
		if (isSDCardAvailable()) {
			sb.append(getExternalStoragePath());
		} else {
			sb.append(getCachePath());
		}
		sb.append(name);
		sb.append(File.separator);
		String path = sb.toString();
		if (createDirs(path)) {
			return path;
		} else {
			return null;
		}
	}

	/** 获取SD下的应用目录 */
	public static String getExternalStoragePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(ROOT_DIR);
		sb.append(File.separator);
		return sb.toString();
	}

	/**
	 * 得到图片文件
	 * 
	 * @return
	 */
	public static File getImageFile() {
		File file = new File(getIconDir() + File.separator
				+ System.currentTimeMillis() + ".jpg");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/** 获取应用的cache目录 */
	public static String getCachePath() {
		File f = DTUIUtils.getContext().getCacheDir();
		if (null == f) {
			return null;
		} else {
			return f.getAbsolutePath() + "/";
		}
	}

	/** 创建文件夹 */
	public static boolean createDirs(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists() || !file.isDirectory()) {
			return file.mkdirs();
		}
		return true;
	}

	/** 复制文件，可以选择是否删除源文件 */
	public static boolean copyFile(String srcPath, String destPath,
			boolean deleteSrc) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		return copyFile(srcFile, destFile, deleteSrc);
	}

	/** 复制文件，可以选择是否删除源文件 */
	public static boolean copyFile(File srcFile, File destFile,
			boolean deleteSrc) {
		if (!srcFile.exists() || !srcFile.isFile()) {
			return false;
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int i = -1;
			while ((i = in.read(buffer)) > 0) {
				out.write(buffer, 0, i);
				out.flush();
			}
			if (deleteSrc) {
				srcFile.delete();
			}
		} catch (Exception e) {
			DTLog.e(e);
			return false;
		} finally {
			DTIOUtils.close(out);
			DTIOUtils.close(in);
		}
		return true;
	}

	/** 判断文件是否可写 */
	public static boolean isWriteable(String path) {
		try {
			if (DTStringUtils.isEmpty(path)) {
				return false;
			}
			File f = new File(path);
			return f.exists() && f.canWrite();
		} catch (Exception e) {
			DTLog.e(e);
			return false;
		}
	}

	/** 修改文件的权限,例如"777"等 */
	public static void chmod(String path, String mode) {
		try {
			String command = "chmod " + mode + " " + path;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (Exception e) {
			DTLog.e(e);
		}
	}

	/**
	 * 把数据写入文件
	 * 
	 * @param is
	 *            数据流
	 * @param path
	 *            文件路径
	 * @param recreate
	 *            如果文件存在，是否需要删除重建
	 * @return 是否写入成功
	 */
	public static boolean writeFile(InputStream is, String path,
			boolean recreate) {
		boolean res = false;
		File f = new File(path);
		FileOutputStream fos = null;
		try {
			if (recreate && f.exists()) {
				f.delete();
			}
			if (!f.exists() && null != is) {
				File parentFile = new File(f.getParent());
				parentFile.mkdirs();
				int count = -1;
				byte[] buffer = new byte[1024];
				fos = new FileOutputStream(f);
				while ((count = is.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				res = true;
			}
		} catch (Exception e) {
			DTLog.e(e);
		} finally {
			DTIOUtils.close(fos);
			DTIOUtils.close(is);
		}
		return res;
	}

	/**
	 * 把字符串数据写入文件
	 * 
	 * @param content
	 *            需要写入的字符串
	 * @param path
	 *            文件路径名称
	 * @param append
	 *            是否以添加的模式写入
	 * @return 是否写入成功
	 */
	public static boolean writeFile(byte[] content, String path, boolean append) {
		boolean res = false;
		File f = new File(path);
		RandomAccessFile raf = null;
		try {
			if (f.exists()) {
				if (!append) {
					f.delete();
					f.createNewFile();
				}
			} else {
				f.createNewFile();
			}
			if (f.canWrite()) {
				raf = new RandomAccessFile(f, "rw");
				raf.seek(raf.length());
				raf.write(content);
				res = true;
			}
		} catch (Exception e) {
			DTLog.e(e);
		} finally {
			DTIOUtils.close(raf);
		}
		return res;
	}

	/**
	 * 把字符串数据写入文件
	 * 
	 * @param content
	 *            需要写入的字符串
	 * @param path
	 *            文件路径名称
	 * @param append
	 *            是否以添加的模式写入
	 * @return 是否写入成功
	 */
	public static boolean writeFile(String content, String path, boolean append) {
		return writeFile(content.getBytes(), path, append);
	}

	/**
	 * 把键值对写入文件
	 * 
	 * @param filePath
	 *            文件路径
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param comment
	 *            该键值对的注释
	 */
	public static void writeProperties(String filePath, String key,
			String value, String comment) {
		if (DTStringUtils.isEmpty(key) || DTStringUtils.isEmpty(filePath)) {
			return;
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File f = new File(filePath);
		try {
			if (!f.exists() || !f.isFile()) {
				f.createNewFile();
			}
			fis = new FileInputStream(f);
			Properties p = new Properties();
			p.load(fis);// 先读取文件，再把键值对追加到后面
			p.setProperty(key, value);
			fos = new FileOutputStream(f);
			p.store(fos, comment);
		} catch (Exception e) {
			DTLog.e(e);
		} finally {
			DTIOUtils.close(fis);
			DTIOUtils.close(fos);
		}
	}

	/** 根据值读取 */
	public static String readProperties(String filePath, String key,
			String defaultValue) {
		if (DTStringUtils.isEmpty(key) || DTStringUtils.isEmpty(filePath)) {
			return null;
		}
		String value = null;
		FileInputStream fis = null;
		File f = new File(filePath);
		try {
			if (!f.exists() || !f.isFile()) {
				f.createNewFile();
			}
			fis = new FileInputStream(f);
			Properties p = new Properties();
			p.load(fis);
			value = p.getProperty(key, defaultValue);
		} catch (IOException e) {
			DTLog.e(e);
		} finally {
			DTIOUtils.close(fis);
		}
		return value;
	}

	/** 把字符串键值对的map写入文件 */
	public static void writeMap(String filePath, Map<String, String> map,
			boolean append, String comment) {
		if (map == null || map.size() == 0 || DTStringUtils.isEmpty(filePath)) {
			return;
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File f = new File(filePath);
		try {
			if (!f.exists() || !f.isFile()) {
				f.createNewFile();
			}
			Properties p = new Properties();
			if (append) {
				fis = new FileInputStream(f);
				p.load(fis);// 先读取文件，再把键值对追加到后面
			}
			p.putAll(map);
			fos = new FileOutputStream(f);
			p.store(fos, comment);
		} catch (Exception e) {
			DTLog.e(e);
		} finally {
			DTIOUtils.close(fis);
			DTIOUtils.close(fos);
		}
	}

	/** 把字符串键值对的文件读入map */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, String> readMap(String filePath,
			String defaultValue) {
		if (DTStringUtils.isEmpty(filePath)) {
			return null;
		}
		Map<String, String> map = null;
		FileInputStream fis = null;
		File f = new File(filePath);
		try {
			if (!f.exists() || !f.isFile()) {
				f.createNewFile();
			}
			fis = new FileInputStream(f);
			Properties p = new Properties();
			p.load(fis);
			map = new HashMap<String, String>((Map) p);// 因为properties继承了map，所以直接通过p来构造一个map
		} catch (Exception e) {
			DTLog.e(e);
		} finally {
			DTIOUtils.close(fis);
		}
		return map;
	}

	/**
	 * 解压
	 * 
	 * @param path
	 * @return
	 */
	public static boolean zipToFile(String zippath, String filepath) {
		try {
			ZipInputStream Zin = new ZipInputStream(
					new FileInputStream(zippath));// 输入源zip路径
			BufferedInputStream Bin = new BufferedInputStream(Zin);
			File Fout = null;
			ZipEntry entry;
			try {
				while ((entry = Zin.getNextEntry()) != null
						&& !entry.isDirectory()) {
					Fout = new File(filepath);
					if (!Fout.exists()) {
						(new File(Fout.getParent())).mkdirs();
					}
					FileOutputStream out = new FileOutputStream(Fout);
					BufferedOutputStream Bout = new BufferedOutputStream(out);
					int b;
					while ((b = Bin.read()) != -1) {
						Bout.write(b);
					}
					Bout.close();
					out.close();
				}
				DTLog.e("解压成功");
				Bin.close();
				Zin.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/** 改名 */
	public static boolean copy(String src, String des, boolean delete) {
		File file = new File(src);
		if (!file.exists()) {
			return false;
		}
		File desFile = new File(des);
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(file);
			out = new FileOutputStream(desFile);
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
				out.flush();
			}
		} catch (Exception e) {
			DTLog.e(e);
			return false;
		} finally {
			DTIOUtils.close(in);
			DTIOUtils.close(out);
		}
		if (delete) {
			file.delete();
		}
		return true;
	}

	/**
	 * 从文件中读取点数据
	 * 
	 * @param path
	 * @return
	 */
	public static ArrayList<RMPoint> getPointsFromFile(String path, String type) {
		InputStream in = null;
		DTLog.e(".mc path : " + path);
		String mapName = path.substring(path.lastIndexOf("/"));
		mapName = mapName.substring(0, mapName.indexOf("_"));
		String build = mapName.substring(0, mapName.indexOf("-"));
		String floor = mapName.substring(mapName.indexOf("-") + 1);
		File file = new File(path);
		ArrayList<RMPoint> points = new ArrayList<RMPoint>();
		try {
			in = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String tmp = null;
		String poiName = "";
		try {
			int id = 0;
			long time = System.currentTimeMillis();
			while (((tmp = br.readLine()) != null) && (tmp.length() > 1)) {
				if (tmp.startsWith("POI")) {
					poiName = tmp.split("\t")[1];
				}
				if (!tmp.startsWith("category") && !tmp.startsWith("POI")) {
					DTLog.e("poiName : " + poiName + "  tem : " + tmp);
					String[] str = tmp.split("\t");
					RMPoint point = new RMPoint();
					point.set_id(id);
					point.setBuildId(build);
					point.setFloor(floor);
					point.setX(Float.parseFloat(str[0]));
					point.setY(Float.parseFloat(str[1]));
					point.setTime(time);
					point.setType(type);
					point.setMapPath(mapName);
					points.add(point);
				}
			}
			br.close();
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return points;
	}
}
