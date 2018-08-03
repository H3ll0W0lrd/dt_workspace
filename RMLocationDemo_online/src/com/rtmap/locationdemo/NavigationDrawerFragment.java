package com.rtmap.locationdemo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.Floor;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.CityInfo;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.model.RMBuildList;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.frm.utils.RMBuildListUtil;
import com.rtmap.locationdemo.beta.R;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/**
	 * Per the design guidelines, you should show the drawer on launch until the
	 * user manually expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private NavigationDrawerCallbacks mCallbacks;

	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerListView;
	private BuildListAdapter mAdapter;
	private HashMap<String, ArrayList<BuildInfo>> mBuildMap;
	private View mFragmentContainerView;
	private String mCityName;// 当前城市名称

	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;
	private Gson mGson = new Gson();

	public NavigationDrawerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated
		// awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState
					.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}

		// Select either the default item (0) or the last selected item.
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of
		// actions in the action bar.
		setHasOptionsMenu(true);
	}

	public Dialog mLoadDialog;// 加载框

	/**
	 * 初始化加载框
	 */
	private void initLoad() {
		mLoadDialog = new ProgressDialog(getActivity());// 加载框
		mLoadDialog.setCanceledOnTouchOutside(false);
		mLoadDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (mLoadDialog.isShowing() && keyCode == KeyEvent.KEYCODE_BACK) {
					mLoadDialog.cancel();
				}
				return false;
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initLoad();
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.build_list, container, false);
		mDrawerListView = (ExpandableListView) layout.findViewById(R.id.list);
		EditText text = (EditText) layout.findViewById(R.id.text);
		mAdapter = new BuildListAdapter(getActivity());
		mDrawerListView.setAdapter(mAdapter);
		mBuildMap = new HashMap<String, ArrayList<BuildInfo>>();
		text.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String str = s.toString();
				if (mCityName == null)
					return;
				ArrayList<BuildInfo> list = mBuildMap.get(mCityName);
				mAdapter.clearList();
				mAdapter.addList(list);
				for (int i = 0; i < list.size(); i++) {
					BuildInfo info = list.get(i);
					if (!info.getBuildName().contains(str)
							&& !info.getName_jp().toLowerCase()
									.contains(str.toLowerCase())
							&& !info.getName_qp().toLowerCase()
									.contains(str.toLowerCase())) {
						mAdapter.removeItem(info);
					}
				}
				mAdapter.notifyDataSetChanged();
			}
		});
		mDrawerListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					final int groupPosition, long id) {
				final BuildInfo info = mAdapter.getGroup(groupPosition);
				if (info.getFloorlist() == null) {
					mLoadDialog.show();
					RMBuildDetailUtil.requestBuildDetail(XunluMap.getInstance()
							.getApiKey(), info.getBuildId(),
							new RMBuildDetailUtil.OnGetBuildDetailListener() {

								@Override
								public void onFinished(RMBuildDetail result) {
									mLoadDialog.cancel();
									if (result.getError_code() == 0) {
										Log.i("rtmap", mGson.toJson(result.getBuild()));
										info.setFloorlist(result.getBuild()
												.getFloorlist());
										if (result.getBuild().getFloorlist()
												.size() > 0) {
											mAdapter.notifyDataSetChanged();
											mAdapter.onGroupExpanded(groupPosition);
										} else {
											Toast.makeText(getActivity(),
													"没有楼层", Toast.LENGTH_LONG)
													.show();
										}
									} else {
										Toast.makeText(getActivity(), "获取楼层失败",
												Toast.LENGTH_LONG).show();
									}
								}
							});
				}

				return false;
			}
		});
		mDrawerListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				selectItem(groupPosition, childPosition);
				return false;
			}
		});
		XunluMap.getInstance().init(getActivity());
		return layout;
	}

	public void findBuild(final String cityName) {
		if (mBuildMap.containsKey(cityName)) {
			mCityName = cityName;
			mAdapter.clearList();
			mAdapter.addList(mBuildMap.get(cityName));
			mAdapter.notifyDataSetChanged();
		} else {
			mLoadDialog.show();
			RMBuildListUtil.requestBuildList(
					XunluMap.getInstance().getApiKey(), cityName,
					new RMBuildListUtil.OnGetBuildListListener() {

						@Override
						public void onFinished(RMBuildList result) {
							mLoadDialog.cancel();
							if (result.getError_code() == 0) {
								mCityName = cityName;
								for (int i = 0; i < result.getCitylist().size(); i++) {
									CityInfo city = result.getCitylist().get(i);
									mAdapter.clearList();
									mAdapter.addList(city.getBuildlist());
									mBuildMap.put(cityName, city.getBuildlist());
									mAdapter.notifyDataSetChanged();
								}
							}else{
								Toast.makeText(getActivity(), result.getError_msg(), Toast.LENGTH_LONG).show();
							}
						}
					});
		}
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation
	 * drawer interactions.
	 *
	 * @param fragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.navigation_drawer_open, /*
										 * "open drawer" description for
										 * accessibility
										 */
		R.string.navigation_drawer_close /*
										 * "close drawer" description for
										 * accessibility
										 */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().invalidateOptionsMenu(); // calls
														// onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to
					// prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true)
							.apply();
				}

				getActivity().invalidateOptionsMenu(); // calls
														// onPrepareOptionsMenu()
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce
		// them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void selectItem(int groupPosition, int childPosition) {
		mCurrentSelectedPosition = childPosition;
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if (mCallbacks != null) {
			BuildInfo build = mAdapter.getGroup(groupPosition);
			Floor floor = mAdapter.getChild(groupPosition, childPosition);
			mCallbacks.onNavigationDrawerItemSelected(build.getBuildId(),
					floor.getFloor());
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar.
		// See also
		// showGlobalContextActionBar, which controls the top-left area of the
		// action bar.
//		if (mDrawerLayout != null && isDrawerOpen()) {
//			inflater.inflate(R.menu.global, menu);
//			showGlobalContextActionBar();
//		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to
	 * show the global app 'context', rather than just what's in the current
	 * screen.
	 */
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.app_name);
	}

	private ActionBar getActionBar() {
		return getActivity().getActionBar();
	}

	/**
	 * Callbacks interface that all activities using this fragment must
	 * implement.
	 */
	public static interface NavigationDrawerCallbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(String buildId, String floorId);
	}
}
