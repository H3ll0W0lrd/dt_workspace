package com.rtmap.indoor_switch.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.indoor_switch.utils.RMlbsUtils;

import java.util.Stack;

/**
 * Created by liyan on 15/10/26.
 */
public class RtMapLocManager implements RMLocationListener {

	public boolean isOnKeyHomeTouch = true;

	public interface RtMapLocManagerListener {
		public void onRtMapLocListenerReceiver(RMLocation rmLocation,
				boolean isFollowing);
	}

	private static RtMapLocManager instance = null;

	private Stack<RtMapLocManagerListener> mListenerStack = new Stack<>();

	private boolean mIsFollowing = false;

	private BroadcastReceiver mHomeKeyEventReceiver = null;

	public static RtMapLocManager instance() {
		if (instance == null) {
			instance = new RtMapLocManager();
			LocationApp.getInstance().init(AppContext.instance());// 初始化定位
			// LocationApp.getInstance().setTestStatus(true);//定位使用测试地址
			LocationApp.getInstance().registerLocationListener(instance);
		}
		return instance;
	}

	public void startLoc() {
		  LocationApp.getInstance().start();// 开始定位
	}

	public void stopLoc() {
		LocationApp.getInstance().stop();// 开始定位
	}

	public void addReceiver(RtMapLocManagerListener listener) {
		registerHomeKeyReceiver();
		mListenerStack.add(listener);
	}

	public void removeReceiver(RtMapLocManagerListener listener) {
		mListenerStack.remove(listener);
	}

	public void destroy() {
		mListenerStack.removeAllElements();
		 LocationApp.getInstance().unRegisterLocationListener(this);
		unRegisterHomeKeyReceiver();
		instance = null;
	}

	public void setFollowMode(boolean isFollow) {
		mIsFollowing = isFollow;
	}

	public boolean isFollowing() {
		return mIsFollowing;
	}

	@Override
	public void onReceiveLocation(RMLocation rmLocation) {
		synchronized (mListenerStack) {
			if (mListenerStack.size() > 0) {
				int listeners = mListenerStack.size();

				RtMapLocManagerListener listener = mListenerStack
						.get(listeners - 1);
				if (listener != null) {
					listener.onRtMapLocListenerReceiver(rmLocation,
							mIsFollowing);
				}
			}
		}
	}

	private void registerHomeKeyReceiver() {
		if (mHomeKeyEventReceiver == null) {
			mHomeKeyEventReceiver = new BroadcastReceiver() {
				String SYSTEM_REASON = "reason";
				String SYSTEM_HOME_KEY = "homekey";
				String SYSTEM_HOME_KEY_LONG = "recentapps";

				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
						String reason = intent.getStringExtra(SYSTEM_REASON);
						if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
							// 表示按了home键,程序到了后台
							isOnKeyHomeTouch = true;
							stopLoc();
						} else if (TextUtils.equals(reason,
								SYSTEM_HOME_KEY_LONG)) {
							// 表示长按home键,显示最近使用的程序列表
						}
					}
				}
			};
			AppContext.instance().registerReceiver(mHomeKeyEventReceiver,
					new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
	}

	private void unRegisterHomeKeyReceiver() {
		if (mHomeKeyEventReceiver != null) {
			AppContext.instance().unregisterReceiver(mHomeKeyEventReceiver);
			mHomeKeyEventReceiver = null;
		}
	}
}
