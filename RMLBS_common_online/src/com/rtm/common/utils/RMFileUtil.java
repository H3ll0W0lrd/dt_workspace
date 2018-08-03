/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.os.Environment;

/**
 * 文件工具类，包含复制，解压，文件路径，创建，删除等方法
 * @author dingtao
 *
 */
public class RMFileUtil {

	public static final String EXT_ZIP = "zip";
	public static final String WIFI_FINGER_FILE_EXT = "bfp3";
	public static final String BEACON_LOC_FILE_EXT = "bbp2";
	public static final String MAGNETIC_FILE_EXT = "mgn1";
	public static final String SQLITE = "RTLbsSql.sqlite";
	public static final String WHITE_LIST = "white.xml";

	public static final String CHINA_BUILD_CODE = "86";

	/**
	 * wifi建筑物判断文件zip压缩包的拼接名
	 */
	public static final String WIFI_BUILD_JUDGE_FILE_FLOOR_CODE = "300001";

	/**
	 * beacon建筑物压缩包的拼接名
	 */
	public static final String BEACON_BUILD_JUDGE_FILE_FLOOR_CODE = "400001";

	/**
	 * 地图匹配map文件夹下文件的的拼接名
	 */
	public static final String MAP_MATCH_FILE_FLOOR_CODE = "500001";

	/** 定位库SDK中 meta-data对应的KEY **/
	public static final String RTMAP_KEY = "RTMAP_KEY";

	/** 定位库SDK对应key的长度 **/
	public static final int RTMAP_LOCATE_KEY_LEN = 10;

	/**
	 * 定位的主目录
	 */
	public static String LOCATION_FILEROOT = "RtmapSDK";
	/**
	 * 地图的主目录
	 */
	public static String MAP_FILEROOT = "RtmapSDK";

	/**
	 * wifi建筑物判断文件名字：包含了众多wifi信息
	 */
	public static final String BUILD_JUDGE_FILE_NAME = "all_build_mac.bbs";

	public static final String PREFERENCE_NAME = "RTMAPLBS";

	/**
	 * 获取文件夹大小
	 * 
	 * @param file
	 *            File实例
	 * @return long 单位为M
	 * @throws Exception
	 */
	public static float getFolderSize(File file) throws Exception {
		float size = 0f;
		File[] fileList = file.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				size = size + getFolderSize(fileList[i]);
			} else {
				size = size + fileList[i].length();
			}
		}
		return size / 1048576f;
	}

	/**
	 * 删除指定目录下文件及目录
	 * 
	 * @param deleteThisPath
	 * @param filepath
	 * @return
	 */
	public static void deleteFolderFile(String filePath, boolean deleteThisPath)
			throws IOException {
		if (!RMStringUtils.isEmpty(filePath)) {
			File file = new File(filePath);

			if (file.isDirectory()) {// 处理目录
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFolderFile(files[i].getAbsolutePath(), true);
				}
			}
			if (deleteThisPath) {
				if (!file.isDirectory()) {// 如果是文件，删除
					file.delete();
				} else {// 目录
					if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
						file.delete();
					}
				}
			}
		}
	}

	public static void deleteOldestFile(String filePath) {
		if (!RMStringUtils.isEmpty(filePath)) {
			File file = new File(filePath);
			if (file.isDirectory()) {
				int index = -1;
				long oldesttime = Long.MAX_VALUE;
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					long time = files[i].lastModified();
					if (!(time > oldesttime)) {
						oldesttime = time;
						index = i;
					}
				}
				files[index].delete();
			}
		}

	}

	/** 创建目录 */
	public static boolean createPath(String path) {
		boolean ret = true;
		File file = new File(path);

		if (!file.exists()) {
			ret = file.mkdir();
		} else {
			ret = true;
		}

		return ret;
	}

	/**
	 * 得到文件列表
	 * 
	 * @param path
	 * @param filter
	 * @return
	 */
	public static String[] listFiles(String path, FilenameFilter filter) {
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

	/** 得到文件列表 */
	public static ArrayList<String> getFilesList(String filePath, String suffix) {
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> filesArrayList = getList(filePath);
		if (filesArrayList == null) {
			return null;
		}
		for (String file : filesArrayList) {
			if (file.endsWith(suffix)) {
				names.add(file);
			}
		}
		return names;
	}

	/** 得到文件列表 */
	public static ArrayList<String> getList(String filePath) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = new File(filePath);
			File[] files = path.listFiles();// 读取
			return getFileName(files);
		} else {
			return null;
		}
	}

	/**
	 * 检查文件是否存在
	 * 
	 * @param path
	 *            路径
	 * @return 是否存在
	 */
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
			// Log.log(e);
			return false;
		}
	}

	/**
	 * 得到文件列表
	 * 
	 * @param vecFile
	 * @param fileAbsolutePath
	 * @param fileNameFilter
	 */
	public static void getFileList(ArrayList<String> vecFile,
			String fileAbsolutePath, String fileNameFilter) {
		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();

		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// 判断是否为文件夹
			if (!subFile[iFileLength].isDirectory()) {
				String filename = subFile[iFileLength].getName();
				// 判断是否为fileNameFilter结尾
				if (filename.trim().endsWith(fileNameFilter)) {
					vecFile.add(fileAbsolutePath + "\\" + filename);
				}
			} else {
				getFileList(vecFile, subFile[iFileLength].getAbsolutePath(),
						fileNameFilter);

			}
		}
	}

	private static ArrayList<String> getFileName(File[] files) {
		ArrayList<String> name = new ArrayList<String>();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					getFileName(file.listFiles());
				} else {
					name.add(file.getName());
				}
			}
		}
		return name;
	}

	/**
	 * 删除文件
	 * 
	 * @param filename
	 *            传入文件路径
	 */
	public static void deleteFile(String filepath) {
		File delFile = new File(filepath);
		if (delFile.exists()) {
			delFile.delete();
		}
	}

	/**
	 * 复制文件，可以选择是否删除源文件
	 * 
	 * @param srcPath
	 *            源文件地址
	 * @param destPath
	 *            生成文件地址
	 * @param deleteSrc
	 *            是否删除源文件
	 * @return 是否拷贝成功
	 */
	public static boolean copyFile(String srcPath, String destPath,
			boolean deleteSrc) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		return copyFile(srcFile, destFile, deleteSrc);
	}

	/**
	 * 复制文件，可以选择是否删除源文件
	 * 
	 * @param srcPath
	 *            源文件地址
	 * @param destPath
	 *            生成文件地址
	 * @param deleteSrc
	 *            是否删除源文件
	 * @return 是否拷贝成功
	 */
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
			out.close();
			in.close();
			if (deleteSrc) {
				srcFile.delete();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static final int BUFF_SIZE = 1024 * 1024;

	/**
	 * 压缩文件
	 * 
	 * @param resFileList
	 * @param zipFile
	 * @throws IOException
	 */
	public static void zipFile(File resFileList, File zipFile)
			throws IOException {
		ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(zipFile), BUFF_SIZE));
		zipFile(resFileList, zipout, "");
		zipout.close();
	}

	private static void zipFile(File resFile, ZipOutputStream zipout,
			String rootpath) throws FileNotFoundException, IOException {
		rootpath = rootpath
				+ (rootpath.trim().length() == 0 ? "" : File.separator)
				+ resFile.getName();
		rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");
		if (resFile.isDirectory()) {
			File[] fileList = resFile.listFiles();
			for (File file : fileList) {
				zipFile(file, zipout, rootpath);
			}
		} else {
			byte buffer[] = new byte[BUFF_SIZE];
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(resFile), BUFF_SIZE);
			zipout.putNextEntry(new ZipEntry(rootpath));
			int realLength;
			while ((realLength = in.read(buffer)) != -1) {
				zipout.write(buffer, 0, realLength);
			}
			in.close();
			zipout.flush();
			zipout.closeEntry();
		}
	}

	public static void deleteAllFile(String path, final String ext) {
		String[] files = listFiles(path, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(ext)) {
					return true;
				}
				return false;
			}
		});
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				deleteFile(path + "/" + files[i]);
			}
		}
	}

	public static void deleteAllFile(String path, final String partName,
			final String ext) {
		String[] files = listFiles(path, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(ext) && filename.contains(partName)) {
					return true;
				}
				return false;
			}
		});
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				deleteFile(path + "/" + files[i]);
			}
		}
	}

	/**
	 * 检查是否有SD卡
	 * 
	 * @return true/false
	 */
	public static boolean checkExternalStorageState() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 强制写文件
	 * 
	 * @param fileName
	 *            {@link String} 文件名
	 * @param item
	 *            {@link String} 需要保存的信息
	 */
	public static void fstream(String fileName, String item) {
		try {
			if (Environment.getExternalStorageState() != null) {
				File file = null;
				file = new File(fileName);

				if (!file.exists()) {
					File parentDir = new File(file.getParent());
					if (!parentDir.exists()) {
						parentDir.mkdirs();
						file.createNewFile();
					}
				}

				FileOutputStream fLogFile = new FileOutputStream(file, true);
				fLogFile.write(item.getBytes());
				fLogFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void fstream(String fileName, byte[] item, int len) {
		try {
			if (Environment.getExternalStorageState() != null) {
				File file = null;
				file = new File(fileName);

				if (!file.exists()) {
					File parentDir = new File(file.getParent());
					if (!parentDir.exists()) {
						parentDir.mkdirs();
						file.createNewFile();
					}
				}
				FileOutputStream fLogFile = new FileOutputStream(file, true);
				fLogFile.write(item, 0, len);
				fLogFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFile(String file) {
		String ret = "";
		try {
			if (new File(file).exists()) {
				InputStreamReader sr = new InputStreamReader(
						new FileInputStream(file), "utf-8");
				BufferedReader reader = new BufferedReader(sr);
				String tmp;
				while ((tmp = reader.readLine()) != null) {
					ret += tmp;
				}
				sr.close();
				reader.close();
			}
		} catch (IOException e) {
			return "";
		}
		return ret;
	}

	public static String getMd5ByFile(File file) throws FileNotFoundException {
		if (!file.exists()) {
			return null;
		}
		String value = null;
		try {
			FileInputStream in = new FileInputStream(file);
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
			String difStr = "";
			int valueLen = value.length();
			if (valueLen < 32) {
				int dif = 32 - valueLen;
				for (int i = 0; i < dif; i++) {
					difStr += "0";
				}
				value = difStr + value;
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 得到定位文件主目录
	 * 
	 * @return
	 */
	private static String getLocationFileRootDir() {
		return createDir(Environment.getExternalStorageDirectory()
				+ File.separator + LOCATION_FILEROOT + File.separator);
	}

	/**
	 * 得到地图文件主目录
	 * 
	 * @return
	 */
	private static String getMapFileRootDir() {
		return createDir(Environment.getExternalStorageDirectory()
				+ File.separator + MAP_FILEROOT + File.separator);
	}

	public static String getLibsDir() {
		return createDir(getLocationFileRootDir() + "libs" + File.separator);
	}

	public static String getLogDir() {
		return createDir(getLocationFileRootDir() + "log" + File.separator);
	}

	public static String getFingerDir() {
		return createDir(getLocationFileRootDir() + "finger" + File.separator);
	}

	public static String getBuildJudgeDir() {
		return createDir(getFingerDir() + "BuildJudgeDatas" + File.separator);
	}

	public static String getRecordDir() {
		return createDir(getLocationFileRootDir() + "record" + File.separator);
	}

	public static String getMapDir() {
		return createDir(getLocationFileRootDir() + "map" + File.separator);
	}

	public static String getMapDataDir() {
		return createDir(getMapFileRootDir() + "mdata" + File.separator);
	}

	/** 创建文件夹 */
	private static String createDir(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists() || !file.isDirectory()) {
			file.mkdirs();
		}
		return dirPath;
	}
}
