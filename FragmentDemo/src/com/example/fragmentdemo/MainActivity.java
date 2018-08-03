package com.example.fragmentdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.fragmentdemo.core.MTActivity;
import com.example.fragmentdemo.core.MTBaseFragment;
import com.example.fragmentdemo.core.MTFragmentFactory;

public class MainActivity extends MTActivity implements OnClickListener{

	private Button one,two;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mt_main);
		one = (Button) findViewById(R.id.one);
		two = (Button) findViewById(R.id.two);
		one.setOnClickListener(this);
		two.setOnClickListener(this);
		
		setPage(0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.one:
			setPage(0);
			break;
		case R.id.two:
			setPage(1);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 改变页面
	 * 
	 * @param position
	 */
	private void setPage(int position) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		// ViewPager页面被选中的回调
		MTBaseFragment fragment = MTFragmentFactory.createFragment(position);
		// 当页面被选中 再显示要加载的页面....防止ViewPager提前加载(ViewPager一般加载三个，自己，左一个，右一个)
		fragmentManager.beginTransaction().replace(R.id.main_layout, fragment)
				.commit();
	}

	@Override
	public String getPageName() {
		return "首页";
	}
}
