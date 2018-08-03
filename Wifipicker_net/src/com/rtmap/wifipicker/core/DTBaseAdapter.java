package com.rtmap.wifipicker.core;

import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;

public abstract class DTBaseAdapter<Data> extends BaseAdapter {
	public ArrayList<Data> mList;
	public long mTime;//更新时间

	public DTBaseAdapter() {
		mList = new ArrayList<Data>();
	}
	public long getmTime() {
		return mTime;
	}

	public void setmTime(long mTime) {
		this.mTime = mTime;
	}

	/**
	 * 清空列表
	 */
	public void clearList() {
		if (mList != null)
			mList.clear();
	}

	/**
	 * 得到List
	 * 
	 * @return
	 */
	public ArrayList<Data> getList() {
		return mList;
	}
	
	public void removeItem(int index){
		mList.remove(index);
	}
	public void removeItem(Data data){
		mList.remove(data);
	}

	/**
	 * 添加List
	 */
	public void addList(List<Data> list) {
		if (list != null && list.size() > 0)
			mList.addAll(list);
	}

	/**
	 * 添加以列表数据到尾部
	 * 
	 * @param datas
	 */
	public void addItemLast(Data info) {
		if (info != null) {
			mList.add(info);
		}
	}

	/**
	 * 添加数据在某一位置
	 * 
	 * @param datas
	 */
	public void addItem(Data info, int index) {
		if (info != null) {
			mList.add(index, info);
		}
	}
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Data getItem(int position) {
		return mList.get(position);
	}
	

	@Override
	public long getItemId(int position) {
		return position;
	}
}
