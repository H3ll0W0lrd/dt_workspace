package com.rtmap.wifipicker.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import com.rtmap.wifipicker.core.WPApplication;

import android.content.res.AssetManager;
import android.os.Environment;

public class FileHelper {
	
	public static File createFileIfNotFound(String path) {
		File file = new File(path);
		try {
			if(file.exists()) {
				return file;
			} else {
				if(file.createNewFile()) {
					return file;
				} else {
					return null;
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static boolean checkFile(String path){
		if(!checkExternalStorageState()) {
			return false;
		}
		try {
			File f = new File(path);
			if(f.exists()) {
				return true;
			} else {
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static String[] listFiles(String path, FilenameFilter filter) {
		if(!checkExternalStorageState()) {
			return null;
		}
		File f = new File(path);
		if(!f.exists()) {
			try {
				f.mkdir();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			return f.list(filter);
		}
		
		return null;
	}
	
	public static boolean checkDir(String path) {
		if(!checkExternalStorageState()) {
			return false;
		}
		File f = new File(path);
		if(!f.exists()) {
			try {
				return f.mkdirs();
			} catch(Exception e) {
				e.printStackTrace();
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
		if(!checkExternalStorageState()) {
			return false;
		}
		File tf = new File(path);
		if(!tf.exists()) {
			try {
				return tf.mkdir();
			} catch(Exception ex) {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public static void deleteFile(String path) {
		File file = new File(path);
		if(file.exists()) {
			file.delete();
		}
	}
	
	public static InputStream getStreamFromFile(String path) {
		File file = getFile(path);
		if(file != null) {
			try {
				return new FileInputStream(file);
			} catch(Exception e) {
				return null;
			}
		}
		
		return null;
	}
	
	public static File getFile(String path) {
		if(!checkExternalStorageState()) {
			return null;
		}
		
		File file = new File(path);
		try {
			if(file.exists()) {
				return file;
			}
			
			return null;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getTextFromAssets(String file) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		try {
			AssetManager assetManager = WPApplication.getInstance().getAssets();
			InputStream inputStream = assetManager.open(file);
			while((length = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
			outputStream.close();
			inputStream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return outputStream.toString();
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
