package com.rtm.frm.stack;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
/** 
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */
public class BackStackActivity extends FragmentActivity {

    final BackStackManagerImpl mBackStackManager = new BackStackManagerImpl(
	    this);

    final Handler mHandler = new Handler(){};
    
    public BackStackManager getBackStackMananger() {
	return mBackStackManager;
    }

}
