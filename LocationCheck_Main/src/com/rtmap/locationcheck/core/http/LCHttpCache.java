package com.rtmap.locationcheck.core.http;


/**
 * Created by mwqi on 2014/6/7.
 */
public class LCHttpCache {

	/** 从本地加载协议 */
	public static String loadFromLocal(String url) {
//		SystemClock.sleep(1000);// 休息1秒，防止加载过快，看不到界面变化效果
//		String path = MTFileUtils.getCacheDir();
//		BufferedReader reader = null;
//		try {
//			reader = new BufferedReader(new FileReader(path + "_" + MTMD5.md5(url)));
//			String line = reader.readLine();// 第一行是时间
//			Long time = Long.valueOf(line);
//			if (time > System.currentTimeMillis()) {// 如果时间未过期
//				StringBuilder sb = new StringBuilder();
//				String result;
//				while ((result = reader.readLine()) != null) {
//					sb.append(result);
//				}
//				return sb.toString();
//			}
//		} catch (Exception e) {
//			MTLog.e(e);
//		} finally {
//			MTIOUtils.close(reader);
//		}
		return null;
	}

	/** 保存到本地 */
	public static void saveToLocal(String str,String url) {
//		String path = MTFileUtils.getCacheDir();
//		BufferedWriter writer = null;
//		try {
//			writer = new BufferedWriter(new FileWriter(path + "_" + MTMD5.md5(url)));
//			long time = System.currentTimeMillis() + 1000 * 60;// 先计算出过期时间，写入第一行
//			writer.write(time + "\r\n");
//			writer.write(str.toCharArray());
//			writer.flush();
//		} catch (Exception e) {
//			MTLog.e(e);
//		} finally {
//			MTIOUtils.close(writer);
//		}
	}
}
