package com.airport.test.activity;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.airport.test.R;
import com.airport.test.ar.ArManager;
import com.airport.test.ar.ArUtils;
import com.dingtao.libs.DTActivity;

public class ARTestActivity extends DTActivity implements
		SurfaceHolder.Callback {

	// 摄像头相关---------
	private Camera camera;
	private Camera.Parameters parameters = null;
	private SurfaceView mSurface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar_show);
		mSurface = (SurfaceView) findViewById(R.id.surfaceview_ar);
		try {
			mSurface.getHolder().setType(
					SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mSurface.getHolder().setFixedSize(1280, 780); // 设置Surface分辨率
			mSurface.getHolder().setKeepScreenOn(true);// 屏幕常亮
			mSurface.getHolder().addCallback(this);// 为SurfaceView的句柄添加一个回调函数
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			parameters = camera.getParameters(); // 获取各项参数
			parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
			parameters.setPreviewSize(width, height); // 设置预览大小
			parameters.setPreviewFrameRate(5); // 设置每秒显示4帧
			parameters.setPictureSize(width, height); // 设置保存的图片尺寸
			parameters.setJpegQuality(80); // 设置照片质量
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open(); // 打开摄像头
			camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
			// 获得手机的方向
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			camera.setDisplayOrientation(ArUtils.getPreviewDegree(rotation));
			camera.startPreview(); // 开始预览
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			camera.stopPreview();
			camera.release(); // 释放照相机
			camera = null;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
