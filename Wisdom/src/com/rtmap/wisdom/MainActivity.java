package com.rtmap.wisdom;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.rtmap.wisdom.util.wheelview.DTHorizontalWheelView;

public class MainActivity extends Activity {

	private DTHorizontalWheelView mWheelView, mWheelView2, mWheelView3, mWheelView4, mWheelView5;
	private TextView mSelectedTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mWheelView = (DTHorizontalWheelView) findViewById(R.id.wheelview);
		mWheelView2 = (DTHorizontalWheelView) findViewById(R.id.wheelview2);
		mWheelView3 = (DTHorizontalWheelView) findViewById(R.id.wheelview3);
		mWheelView4 = (DTHorizontalWheelView) findViewById(R.id.wheelview4);
		mWheelView5 = (DTHorizontalWheelView) findViewById(R.id.wheelview5);
		mSelectedTv = (TextView) findViewById(R.id.selected_tv);

		final List<String> items = new ArrayList<String>();
		for (int i = 1; i <= 40; i++) {
			items.add(String.valueOf(i * 1000));
		}

		mWheelView.setItems(items);
		mWheelView.selectIndex(8);
		mWheelView.setAdditionCenterMark("元");

		List<String> items2 = new ArrayList<String>();
		items2.add("一月");
		items2.add("二月");
		items2.add("三月");
		items2.add("四月");
		items2.add("五月");
		items2.add("六月");
		items2.add("七月");
		items2.add("八月");
		items2.add("九月");
		items2.add("十月");
		items2.add("十一月");
		items2.add("十二月");

		mWheelView2.setItems(items2);

		List<String> items3 = new ArrayList<String>();
		items3.add("1");
		items3.add("2");
		items3.add("3");
		items3.add("5");
		items3.add("7");
		items3.add("11");
		items3.add("13");
		items3.add("17");
		items3.add("19");
		items3.add("23");
		items3.add("29");
		items3.add("31");

		mWheelView3.setItems(items3);
		mWheelView3.setAdditionCenterMark("m");

		mWheelView4.setItems(items);

		mWheelView5.setItems(items);
		mWheelView5.setOnWheelItemSelectedListener(new DTHorizontalWheelView.OnWheelItemSelectedListener() {
			@Override
			public void onWheelItemSelected(int position) {
				mSelectedTv.setText("选择：" + items.get(position) + "万");
			}
		});

	}
}
