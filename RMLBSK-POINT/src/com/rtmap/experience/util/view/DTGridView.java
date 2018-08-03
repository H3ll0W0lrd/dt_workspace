package com.rtmap.experience.util.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class DTGridView extends GridView {
	public DTGridView(Context context) {
		super(context);
	}

	public DTGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 设置不滚动
	 */
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
