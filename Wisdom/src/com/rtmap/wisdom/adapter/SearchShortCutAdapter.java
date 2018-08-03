package com.rtmap.wisdom.adapter;

import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.util.DTUIUtil;

public class SearchShortCutAdapter extends
		RecyclerView.Adapter<RecyclerView.ViewHolder>{

	public static final int TYPE_LIST = 0, TYPE_GRID = 1;
	private OnClickListener listener;
	
	public SearchShortCutAdapter(OnClickListener listener) {
		this.listener = listener;
	}

	private ArrayList<POI> mList = new ArrayList<POI>();

	public void addList(ArrayList<POI> list) {
		if (list != null && list.size() > 0)
			mList.addAll(list);
	}
	
	public void clear() {
		mList.clear();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
		if (viewType == TYPE_LIST) {
			View view = DTUIUtil.inflate(R.layout.search_shortcut_item_title);
//			view.setOnClickListener(this);
			ViewHolderList viewHolder = new ViewHolderList(view);
			viewHolder.floor = (TextView) view.findViewById(R.id.floor);
			return viewHolder;
		} else {
			View view = DTUIUtil.inflate(R.layout.search_shortcut_item);
			view.setOnClickListener(listener);
			ViewHolderGrid viewHolder = new ViewHolderGrid(view);
			viewHolder.name1 = (TextView) view.findViewById(R.id.name1);
			return viewHolder;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof ViewHolderList) {
			ViewHolderList viewHolder = (ViewHolderList) holder;
			viewHolder.floor.setText(mList.get(position).getFloor());
//			viewHolder.itemView.setTag(position);
		} else {
			ViewHolderGrid viewHolder = (ViewHolderGrid) holder;
			viewHolder.name1.setText(mList.get(position).getName());
			viewHolder.itemView.setTag(position);
		}
	}

	@Override
	public int getItemCount() {
		return mList.size();
	}

	@Override
	public int getItemViewType(int position) {

		return mList.get(position).getType() == 1 ? TYPE_LIST : TYPE_GRID;
	}

	class ViewHolderList extends RecyclerView.ViewHolder {

		private TextView floor;

		public ViewHolderList(View itemView) {
			super(itemView);
		}
	}

	class ViewHolderGrid extends RecyclerView.ViewHolder {

		private TextView name1;

		public ViewHolderGrid(View itemView) {
			super(itemView);
		}
	}

	public POI getItem(int position) {
		return mList.get(position);
	}

}
