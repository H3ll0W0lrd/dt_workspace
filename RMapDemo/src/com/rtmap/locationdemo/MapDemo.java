package com.rtmap.locationdemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rtmap.mapdemo.R;

/**
 * 地图demo
 * 
 * @author dingtao
 *
 */
public class MapDemo extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_demo);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.map_list)));
	}

	/**
	 * 按List对应项进入不同的页面，集成想要的功能
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch (position) {
		case 0:
			Intent openmap = new Intent(this, BuildListActivity.class);
			startActivity(openmap);
			break;
		case 1:
			Intent map = new Intent(this, MapActivity.class);
			startActivity(map);
			break;
		case 2:
			Intent search = new Intent(this, SearchActivity.class);
			startActivity(search);
			break;
		case 3:
			Intent cate = new Intent(this, CateActivity.class);
			startActivity(cate);
			break;
		case 4:
			Intent navigation = new Intent(this, NavigationActivity.class);
			startActivity(navigation);
			break;
		case 5:
			Intent animNavigation = new Intent(this, AnimNavigationActivity.class);
			startActivity(animNavigation);
			break;
		}
	}
}
