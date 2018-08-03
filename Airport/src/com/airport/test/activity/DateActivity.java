package com.airport.test.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ImageView;

import com.airport.test.R;
import com.dingtao.libs.DTActivity;

public class DateActivity extends DTActivity implements OnClickListener {

	private ImageView imgBack;
	private CalendarView mDate;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, DateActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan_date);

		imgBack = (ImageView) findViewById(R.id.img_back);
		imgBack.setOnClickListener(this);
		mDate = (CalendarView) findViewById(R.id.calendarView1);
		mDate.setOnDateChangeListener(new OnDateChangeListener() {
			
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month,
					int dayOfMonth) {
				Intent intent = getIntent();
				intent.putExtra("date", year+"-"+month+"-"+dayOfMonth);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			finish();
			break;
		}
	}

}
