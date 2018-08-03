package com.hwasmart.glhwatch;

import com.google.zxing.BarcodeFormat;
import com.hwasmart.glhwatch.service.LocationUploadService;
import com.hwasmart.utils.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class IDActivity extends Activity {
	
	private ImageView code128Image;
	private TextView idView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_id);
		
		code128Image = (ImageView)findViewById(R.id.code128);
		idView = (TextView)findViewById(R.id.idtitle_text);
		
		int screenWidth = 0;
		Point screenSize = new Point();
		Display mDisplay = getWindowManager().getDefaultDisplay();
		mDisplay.getSize(screenSize);
		screenWidth = screenSize.x;
		
		Bitmap code128 = Utils.generateQRCode(LocationUploadService.DeviceID, BarcodeFormat.CODE_128, (int) (screenWidth), 150);
		code128Image.setImageBitmap(code128);
		idView.setText("ID:" + LocationUploadService.DeviceID);
	}
}
