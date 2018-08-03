package com.rtmap.locationcheck.util;

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
import android.util.Log;
import android.widget.RemoteViews;

import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpClient.ProgressListener;

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

	class DownloadCall implements LCCallBack {

		private String path;
		private String url;

		public DownloadCall(String url, int code) {
			this.url = url;
			path = DTFileUtils.getDownloadDir() + "RtmapData" + code + ".apk";
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			if (DTFileUtils.checkFile(path)) {
				return true;
			}
			return LCHttpClient.downloadFile(path, url, DownloadService.this);
		}

		@Override
		public void onCallBackFinish(Object obj) {
			boolean l = (Boolean) obj;
			nm.cancel(2);
			if (l) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(new File(path)),
						"application/vnd.android.package-archive");
				startActivity(intent);
			} else {
				DTUIUtils.showToastSafe("更新失败");
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

		new LCAsyncTask(new DownloadCall(intent.getStringExtra("url"),
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
