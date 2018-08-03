package com.rtm.frm.utils;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;

/**
 * ui通信工具类 com.rtm.location.util
 * 
 * @author lixinxin <br/>
 *         create at 2012-8-1 下午21:11:57
 */
public class Handlerlist {
	@SuppressWarnings("unused")
	private static final String TAG = "UIEvent";

	/** handler列表，所有需要接收其消息的窗口需要注册到该列表中 **/
	private List<Handler> needToReflashList = null;
	/** UIEvent的静态对象，用于该类的单例实现 **/
	private static Handlerlist uiEvent = null;

	/**
	 * 得到UIEvent的一个单例
	 * 
	 * @return UIEvent 单例对象
	 */
	public static Handlerlist getInstance() {
		if (uiEvent == null) {
			uiEvent = new Handlerlist();
		}
		return uiEvent;
	}

	private Handlerlist() {
		needToReflashList = new ArrayList<Handler>();
	}

	/**
	 * 注册handler对象
	 * 
	 * @param item
	 *            handler对象
	 */
	public void register(Handler item) {
		if (!needToReflashList.contains(item))
			needToReflashList.add(item);
	}

	public int getlistsize() {
		return needToReflashList.size();
	}

	/**
	 * 注销handler对象
	 * 
	 * @param item
	 *            handler对象
	 */
	public void remove(Handler item) {
		if (needToReflashList.contains(item))
			needToReflashList.remove(item);
	}

	public void notifications(int what, int arg1, Object obj) {
		if (needToReflashList == null || needToReflashList.isEmpty()) {
			return;
		} else {
			for (Handler item : needToReflashList) {
				Message msg = item.obtainMessage(what);
				if (msg != null) {
					msg.arg1 = arg1;
					msg.obj = obj;
				} else {
					msg = new Message();
					msg.what = what;
					msg.arg1 = arg1;
					msg.obj = obj;
				}
				item.sendMessage(msg);
			}
		}
	}
}