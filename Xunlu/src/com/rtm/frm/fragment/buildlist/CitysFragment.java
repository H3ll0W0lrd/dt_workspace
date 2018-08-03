/**
 * @author liyan
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.fragment.buildlist;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.rtm.frm.R;
import com.rtm.frm.adapter.CitysAdapter;
import com.rtm.frm.dialogfragment.BaseDialogFragment;
import com.rtm.frm.fragment.controller.BuildListManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;


public class CitysFragment extends BaseDialogFragment implements OnItemClickListener {
	private ListView mCityListView;
	private CitysAdapter mCitysAdapter;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_citys,
			container, false);
		return contentView;
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	initView(view);
		initData();
    }

    private void initView(View contentView) {
		
    	mCityListView = (ListView)contentView. findViewById(R.id.city_list);
		mCitysAdapter = new CitysAdapter();
		mCityListView.setAdapter(mCitysAdapter);
		mCityListView.setOnItemClickListener(this);
		
    }
	
    private void initData() {
		
		List<String> airportCitys = BuildListManager.getInstance().queryCitysByBuildType(BuildListManager.BUILD_TYPE);
		mCitysAdapter.setData(airportCitys);
		mCitysAdapter.notifyDataSetChanged();
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		//更新buildList
		String cityName = adapterView.getAdapter().getItem(position).toString();
		BuildListFragment buildListFragment = (BuildListFragment) MyFragmentManager
				.getFragmentByFlag(MyFragmentManager.PROCESS_BUILDLIST,
						MyFragmentManager.FRAGMENT_BUILDLIST);
		buildListFragment.upadteBuilds(cityName);
		this.dismiss();
	}
    
}