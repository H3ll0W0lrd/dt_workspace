package com.rtmap.locationdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.Floor;
import com.rtmap.mapdemo.R;

/**
 * 打开地图页面-导航适配器
 * @author dingtao
 *
 */
public class BuildListAdapter extends BaseExpandableListAdapter {

	private Activity mActivity;
	private ArrayList<BuildInfo> mFloorList;

	public BuildListAdapter(Activity activity) {
		mActivity = activity;
		mFloorList = new ArrayList<BuildInfo>();
	}

	static class ViewHolder {
		TextView mTextMap;
	}


	/**
	 * 添加分组list
	 * 
	 * @param list
	 */
	public void addItem(BuildInfo info) {
		mFloorList.add(info);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public Floor getChild(int groupPosition, int childPosition) {
		return mFloorList.get(groupPosition).getFloorlist().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mActivity).inflate(
					R.layout.map_list_item, null);
			holder = new ViewHolder();
			holder.mTextMap = (TextView) convertView
					.findViewById(R.id.text_map);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Floor info = mFloorList.get(groupPosition).getFloorlist()
				.get(childPosition);
		holder.mTextMap.setText(info.getFloor());

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mFloorList.get(groupPosition).getFloorlist().size();
	}

	@Override
	public BuildInfo getGroup(int groupPosition) {
		return mFloorList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mFloorList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mActivity).inflate(
					R.layout.map_list_title_item, null);
			holder = new ViewHolder();
			holder.mTextMap = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextMap.setText(mFloorList.get(groupPosition).getBuildName());
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
