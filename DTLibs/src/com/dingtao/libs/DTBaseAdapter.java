package com.dingtao.libs;

import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;

public abstract class DTBaseAdapter<T> extends BaseAdapter {
	public ArrayList<T> mList;
	public long mTime;// 更新时间

	public DTBaseAdapter() {
		mList = new ArrayList<T>();
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
	public ArrayList<T> getList() {
		return mList;
	}

	public void removeItem(int index) {
		mList.remove(index);
	}

	public void removeItem(T info) {
		if (mList.contains(info))
			mList.remove(info);
	}

	/**
	 * 添加List
	 */
	public void addList(List<T> list) {
		if (list != null && list.size() > 0)
			mList.addAll(list);
	}

	/**
	 * 添加以列表数据到尾部
	 * 
	 * @param Ts
	 */
	public void addItem(T info) {
		if (info != null) {
			mList.add(info);
		}
	}
	
	/**
	 * 添加数据在某一位置
	 * 
	 * @param Ts
	 */
	public void addItem(T info, int index) {
		if (info != null) {
			mList.add(index, info);
		}
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public T getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
