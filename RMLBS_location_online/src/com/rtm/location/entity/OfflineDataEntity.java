package com.rtm.location.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.rtm.common.utils.RMFileUtil;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.UtilLoc;

/**
 * 定位离线数据，用于离线运行
 * 
 * @author hotstar
 */
public class OfflineDataEntity {
	private BufferedReader mFileReader;
	private static volatile OfflineDataEntity instance = null;
	private ArrayList<String> offlineDatArrayList = null;
	private int rd_index = 0;
	private String wr_buildName = "";
	private String wr_fileName = "";
	private boolean write_enable = true;

	/** 离线数据后缀名 */
	private static String FILE_OFF_DATA = ".off";

	private OfflineDataEntity() {
		offlineDatArrayList = new ArrayList<String>();
	}

	/** 得到该类对象的一个单例 **/
	public static OfflineDataEntity getInstance() {
		if (instance == null) {
			synchronized (LocationApp.class) {
				if (instance == null) {
					instance = new OfflineDataEntity();
				}
			}
		}
		return instance;
	}

	/**
	 * 一行一行读取离线数据，当读取完后返回一个空，再循环播放第一条
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 读取到的字符串或空字符(当读取到文件尾或者文件读取失败)
	 */
	public String readLine(String filePath) {
		if (!loadFile(filePath)) {
			return "";
		}

		if (rd_index < offlineDatArrayList.size()) {
			String retStr = offlineDatArrayList.get(rd_index++);
			return retStr;
		} else {
			rd_index = 0;
			return "";
		}
	}

	/**
	 * 写文件
	 * 
	 * @param inf
	 *            写入的字符串信息
	 * @param file
	 *            文件名
	 */
	public int write(String inf, String file) {
		int ret = 0;
		if (write_enable && (!file.equals(""))) {
			if (!wr_buildName.equals(file)) {
				wr_fileName = file + UtilLoc.getCurrDay() + "_"
						+ UtilLoc.getCurTimeMillis() + FILE_OFF_DATA;
				wr_buildName = file;
			}
		} else {
			ret = -1;
		}

		RMFileUtil.fstream(wr_fileName, inf + "\n");

		return ret;
	}

	/**
	 * 离线文件是否存在
	 * 
	 * @param filePath
	 *            指定的文件夹
	 * @return true 离线文件存在
	 */
	public boolean isOffFileExist(String filePath) {
		if (RMFileUtil.getFilesList(filePath, FILE_OFF_DATA) != null) {
			return RMFileUtil.getFilesList(filePath, FILE_OFF_DATA).size() > 0 ? true
					: false;
		} else {
			return false;
		}
	}

	/**
	 * 装载离线数据
	 * 
	 * @param filePath
	 *            文件路径
	 * @return true--装载数据成功</br>false--装载失败，文件存在或读取异常
	 */
	private boolean loadFile(String filePath) {
		if ((mFileReader == null) || (offlineDatArrayList.size() <= 0)) {
			// 遍历文件夹
			offlineDatArrayList.clear();
			ArrayList<String> judgeBuild = RMFileUtil.getFilesList(filePath,
					FILE_OFF_DATA);
			for (String str : judgeBuild) {
				String file = filePath + str;
				getFileReader(file);
				try {
					String line = mFileReader.readLine();
					while (line != null) {
						offlineDatArrayList.add(line);
						line = mFileReader.readLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		} else {
			return true;
		}
	}

	/**
	 * 从文件中获取离线数据，用于离线调试
	 * 
	 * @param filePath
	 *            文件路径
	 */
	private void getFileReader(String filePath) {
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			InputStreamReader fr = new InputStreamReader(fis);
			mFileReader = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			mFileReader = null;
			e.printStackTrace();
		}
	}
}
