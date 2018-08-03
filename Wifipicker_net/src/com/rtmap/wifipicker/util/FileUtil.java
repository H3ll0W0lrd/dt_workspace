package com.rtmap.wifipicker.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

import com.rtm.frm.model.NavigatePoint;

public class FileUtil {
	private static final String TAG = "FilesUtil";

	/**
	 * 得到文件列表
	 */
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

	public static void deleteFilesList(String filePath, String suffix) {
		ArrayList<String> filesArrayList = getList(filePath);
		if (filesArrayList == null) {
			return;
		}

		for (String file : filesArrayList) {
			if (file.endsWith(suffix)) {
				File f = new File(filePath + file);
				f.delete();
			}
		}
	}

	/**
	 * 得到文件列表
	 */
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
	 * 拷贝文件夹
	 * 
	 * @param src
	 * @param dst
	 * @return 返回拷贝文件的个数
	 */
	public static int copyDir(String src, String dst) {
		int ret = -1;

		ArrayList<String> srcList = getList(src);
		if (srcList.size() == 0) {
			return 0;
		}

		ret = 0;
		for (String file : srcList) {
			if (copyFile(file, dst)) {
				ret++;
			}
		}
		return ret;
	}

	/**
	 * 复制单个文件
	 * 
	 * @param src
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param dst
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static boolean copyFile(String src, String dst) {
		boolean ret = true;
		try {
			int byteread = 0;
			File oldfile = new File(src);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(src); // 读入原文件
				FileOutputStream fs = new FileOutputStream(dst);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	/**
	 * 保存流信息至文件
	 * 
	 * @param istr
	 *            InputStream 输入文件流
	 * @param dst
	 *            String 文件路径
	 * @return boolean
	 */
	public static boolean saveFile(InputStream istr, String dst) {
		boolean ret = true;
		if (istr == null) {
			return false;
		}

		try {
			int byteread = 0;
			FileOutputStream fs = new FileOutputStream(dst);
			byte[] buffer = new byte[1444];
			while ((byteread = istr.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			istr.close();
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	/**
	 * 创建目录
	 */
	public static void createPath(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	/**
	 * 删除文件操作
	 * 
	 * @param filename
	 *            文件名
	 */
	public static void deleteFile(String filename) {
		File delFile = new File(filename);
		if (delFile.exists()) {
			delFile.delete();
		}
	}

	/**
	 * 删除文件或文件夹
	 * 
	 * @param filename
	 */
	public static void deleteDir(String filename) {
		File delFile = new File(filename);
		delete(delFile);
	}

	// 递归删除文件及文件夹
	private static void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
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

				// 创建一个文件
				if (!file.exists()) {
					file.createNewFile();
				}

				FileOutputStream fLogFile = new FileOutputStream(file, true);
				fLogFile.write(item.getBytes());
				fLogFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param fileName
	 *            {@link String} 文件名
	 * @return true 文件存在
	 */
	public static boolean exist(String fileName) {
		File file = new File(fileName);
		return file.exists() && (file.length() > 0);
	}

	/**
	 * 保存资源文件到文件夹中
	 * 
	 * @param dir
	 * @param name
	 * @param c
	 */
	public static void copyAssets(String dir, String name, Context c) {
		String path = String.format("%s%s", dir, name);
		if (exist(path)) {
			return;
		}

		try {
			InputStream in = c.getAssets().open(name);
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

	public static boolean checkExternalStorageState() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除文件后缀名
	 * 
	 * @param fileName
	 * @param ext
	 * @return
	 */
	public static String removeFileExt(String fileName, String ext) {
		String ret = "";
		int index = fileName.indexOf(ext);
		if (index != -1) {
			ret = fileName.substring(0, index);
		}
		return ret;
	}

	public static String getFileNameByPoint(String root, final String mapName,
			NavigatePoint p, final String fileExt) {
		// mapName: 860100010040500002-F1
		// database name: 860100010040500002-F2-0_518_470_1410428628600.db
		final String xy = (int) (p.getX() * 1000) + "_"
				+ (int) (p.getY() * 1000);
		String[] files = FileHelper.listFiles(root, new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.startsWith(mapName) && filename.endsWith(fileExt)
						&& filename.contains(xy)) {
					return true;
				}
				return false;
			}
		});
		if (files != null && files.length > 0) {
			return files[0];
		}
		return null;
	}

}
