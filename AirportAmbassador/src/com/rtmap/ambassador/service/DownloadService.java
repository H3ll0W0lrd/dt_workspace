package com.rtmap.ambassador.service;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.rtmap.ambassador.R;
import com.rtmap.ambassador.core.DTAsyncTask;
import com.rtmap.ambassador.core.DTCallBack;
import com.rtmap.ambassador.http.DTHttpUtil;
import com.rtmap.ambassador.http.DTHttpUtil.ProgressListener;
import com.rtmap.ambassador.util.DTFileUtil;
import com.rtmap.ambassador.util.DTUIUtil;

public class DownloadService extends Service implements ProgressListener {

	private NotificationManager nm;

	private Handler mHander = new Handler() {
		public void handleMessage(Message msg) {
			builder.setProgress(100, msg.what, false);
			Notification n = builder.build();
			n.flags = Notification.FLAG_NO_CLEAR;
			nm.notify(2, n);
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	class DownloadCall implements DTCallBack {

		private String path;
		private String url;

		public DownloadCall(String url, int code) {
			this.url = url;
			path = DTFileUtil.getDownloadDir() + "am" + code + ".apk";
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			if (DTFileUtil.checkFile(path)) {
				return true;
			}
			return DTHttpUtil.downloadFile(path, url, DownloadService.this);
		}

		@Override
		public void onCallBackFinish(Object obj) {
			int l = (Integer) obj;
			nm.cancel(2);
			if (l==DTHttpUtil.FILE_DOWNLOAD_OK) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(new File(path)),
						"application/vnd.android.package-archive");
				startActivity(intent);
			} else {
				DTUIUtil.showToastSafe("更新失败");
			}
		}
	}

	NotificationCompat.Builder builder;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String msg = getString(R.string.app_name);

		String version = intent.getStringExtra("version");
		builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("数据采集" + version).setContentText(msg)
				.setTicker(msg).setProgress(100, 0, false);
		Notification n = builder.build();
		n.flags = Notification.FLAG_NO_CLEAR;
		nm.notify(2, n);

		new DTAsyncTask(new DownloadCall(intent.getStringExtra("url"),
				intent.getIntExtra("code", 0))).run();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public long time;

	public void transferred(long transferedBytes, long fileSize) {
		if (System.currentTimeMillis() - time > 1000) {
			time = System.currentTimeMillis();
			mHander.sendEmptyMessage((int) (100 * transferedBytes / fileSize));
		}
	}

}
