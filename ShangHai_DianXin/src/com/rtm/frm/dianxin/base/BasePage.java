package com.rtm.frm.dianxin.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;


/**
 * Created by fushenghua on 15/3/18.
 */
public abstract class BasePage {
	protected Context context;
	protected View contentView;

	protected BasePage(Context context) {
		this.context = context;
		contentView = initView((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

	}
	
	private final int TAB_ONE = 0x01;
	private final int TAB_TWO = 0x02;
	private final int TAB_THREE = 0x03;
	private final int TAB_FOUR = 0x04;

	protected void initTitleBar(int titleTag) {
//		TextView tv_title=(TextView) contentView.findViewById(R.id.tv_title);
//		switch (titleTag) {
//		case TAB_ONE:
//			//tv_title.setText()
//			break;
//		case TAB_TWO:
//
//			break;
//		case TAB_THREE:
//
//			break;
//		case TAB_FOUR:
//
//			break;
//
//		default:
//			break;
//		}

	}

	protected abstract View initView(LayoutInflater inflater);

	public abstract void initData();

	public View getContentView() {
		return contentView;
	}

}
