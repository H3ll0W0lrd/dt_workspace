package com.hwasmart.glhwatch;

import com.hwasmart.glhwatch.service.LocationUploadService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class HelpActivity extends Activity {
	
	private Button okBtn1;
	private Button okBtn2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		okBtn1 = (Button)findViewById(R.id.ok_btn1);
		okBtn2 = (Button)findViewById(R.id.ok_btn2);
		
		okBtn1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				alertDialog();
			}
			
		});
		
		okBtn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HelpActivity.this, LocationUploadService.class);
				intent.putExtra("operation", "call_for_help");
				startService(intent);
			}
			
		});
	}
	
	@Override
	public void onBackPressed() {
		alertDialog();
	}
	
	private void alertDialog(){
		new AlertDialog.Builder(this).setTitle("确认解除求助吗？") 
        .setPositiveButton("确定", new DialogInterface.OnClickListener() { 
     
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
            	// 点击“确认”后的操作 
            	HelpActivity.this.finish(); 
     
            } 
        }) 
        .setNegativeButton("返回", new DialogInterface.OnClickListener() { 
     
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
            // 点击“返回”后的操作,这里不设置没有任何操作 
            } 
        }).show(); 
	}
}
