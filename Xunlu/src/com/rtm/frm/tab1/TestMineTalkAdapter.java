package com.rtm.frm.tab1;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.model.Build;
import com.rtm.frm.utils.XunluUtil;

/**
 * @author liYan
 * @explain 全局搜索adapter
 */
@SuppressLint("InflateParams")
public class TestMineTalkAdapter extends BaseAdapter {

	private List<String> mPoisData = new ArrayList<String>();
	
	public TestMineTalkAdapter() {
		for(int i = 0 ;i < 4;++i) {
			mPoisData.add("test");
		}
	}

	@Override
	public int getCount() {
		return mPoisData.size();
	}

	@Override
	public Object getItem(int position) {
		return mPoisData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = LayoutInflater.from(XunluApplication.mApp).inflate(
					R.layout.fragment_test_mine_talk_item, null);
			ViewHolder holder = new ViewHolder();
			holder.nickName = (TextView) view.findViewById(R.id.friend_nickname);
			holder.friendMsg = (TextView) view.findViewById(R.id.friend_msg);
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		switch (position) {
		case 0:
			h.nickName.setText("疯狂石头");
			h.friendMsg.setText("去哪玩儿啊？");
			break;
		case 1:
			h.nickName.setText("一路狂奔");
			h.friendMsg.setText("今天天气不错的");
			break;
		case 2:
			h.nickName.setText("诺曼底");
			h.friendMsg.setText("寻宝去。。。");
			break;
		case 3:
			h.nickName.setText("说谎");
			h.friendMsg.setText("哇咔咔");
			break;

		default:
			break;
		}
		return view;
	}

	private class ViewHolder {
		public TextView nickName;
		public TextView friendMsg;
	}

}
