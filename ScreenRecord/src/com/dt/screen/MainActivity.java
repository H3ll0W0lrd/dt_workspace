package com.dt.screen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay.Callback;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.dingtao.libs.util.DTLog;

public class MainActivity extends Activity implements OnClickListener,
		SurfaceTextureListener {

	Button mStart, mStop;
	MediaProjectionManager mScreenManager;
	private static final int REQUEST = 100;
	MediaProjection.Callback mCallBack;
	MediaProjection mediaData;
	Handler mHandler = new Handler();
	int height, width, dpi;
	MediaRecorder mMediaRecorder;
	TextureView mSurfaceView;
	Surface mSurface;
	WindowManager mWindowManager;
	LayoutParams mLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);
		mStart.setOnClickListener(this);
		mStop.setOnClickListener(this);
		DisplayMetrics metric = new DisplayMetrics();
		mSurfaceView = (TextureView) findViewById(R.id.surfaceView1);
		mSurfaceView.setSurfaceTextureListener(this);
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		height = metric.heightPixels;
		width = metric.widthPixels;
		dpi = metric.densityDpi;
		mCallBack = new MediaProjection.Callback() {
			@Override
			public void onStop() {
				super.onStop();
				if (mMediaRecorder != null) {
					// 停止录制
					mMediaRecorder.stop();
					// 释放资源
					mMediaRecorder.release();
				}
				Toast.makeText(getApplicationContext(), "结束录制屏幕", 3000).show();
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			mediaData = mScreenManager.getMediaProjection(resultCode, data);
			mediaData.createVirtualDisplay("dingtao", width, height, dpi,
					DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mSurface,
					new Callback() {

					}, mHandler);
			// mMediaRecorder = new MediaRecorder();
			// mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			// mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			// // 设置录制的视频编码h263 h264
			// mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			// mMediaRecorder.setVideoSize(width, height);
			// mMediaRecorder.setVideoFrameRate(48);
			// mMediaRecorder.setPreviewDisplay(mSurface);
			// mMediaRecorder.setOutputFile(DTFileUtil.getDataDir() + "movie-"
			// + System.currentTimeMillis() + ".mp4");
			// try {
			// mMediaRecorder.prepare();
			// } catch (IllegalStateException e) {
			// e.printStackTrace();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// // 开始录制
			// mMediaRecorder.start();
			mediaData.registerCallback(mCallBack, mHandler);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start:
			startActivityForResult(mScreenManager.createScreenCaptureIntent(),
					REQUEST);
			break;
		case R.id.stop:
			// mMediaRecorder.stop();
			mediaData.stop();
			break;
		}
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		mSurface = new Surface(surface);

	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		DTLog.e("surface: 销毁了");
		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		DTLog.e("尺寸改变：" + width + "   " + height);

	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		DTLog.e("更新中");
	}
}
