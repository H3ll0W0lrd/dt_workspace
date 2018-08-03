package com.rtm.frm.stack;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.View;
import android.view.ViewGroup;

/**
 * 用于对Acivity包含的view、dialog的堆栈进行管理.
 * 
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */

public abstract class BackStackManager {

    /**
     * 包含了堆栈的Activity.
     */
    BackStackActivity mActivity;

    /**
     * 缓存了的堆栈实例.
     */
    HashMap<String, BackStack> mActive = new HashMap<String, BackStack>();

    /**
     * 被缓存的堆栈实例，在堆栈内的出现次数.用于判断是否要在实例的缓存里清除实例.
     */
    HashMap<String, Integer> mActiveCount = new HashMap<String, Integer>();

    /**
     * 堆栈的列表.
     */
    ArrayList<BackStackState> mBackStackList = new ArrayList<BackStackState>();

    public BackStackManager(BackStackActivity activity) {
	mActivity = activity;
    }

    /**
     * 显示一个页面，推入堆栈.
     * 
     * @param backStack
     */
    public abstract void add(BackStack backStack);

    /**
     * 显示一个页面，推入堆栈,但不保存，打开新页面或者回退后清除.
     * 
     * @param backStack
     */
    public abstract void show(BackStack backStack);

    /**
     * 关闭一个处于显示状态的页面.
     * 
     * @param backStack
     */
    public abstract BackStackManager dismiss(BackStack backStack);

    /**
     * 关闭当前显示的页面，并且回退打开上一个页面.
     * 
     * @return
     */
    public abstract boolean popBackStack();

    /**
     * 移除堆栈里所有包含参数名称的页面.
     * 
     * @param name
     */
    public abstract BackStackManager remove(String name);

    /**
     * 移除堆栈里所有包含模块名称的页面. 页面的名称定义为 modulename_pagename.
     * 
     * @param name
     */
    public abstract BackStackManager removeModule(String name);

    /**
     * 清除堆栈.
     */
    public abstract BackStackManager clearBackStack();

    public abstract int getBackStackSize();

    public abstract BackStack getLastBackStack();

    /**
     * 页面堆栈发生变化时的回调接口.
     * 
     * @author hua.wu
     * 
     */
    public interface OnBackStackChangedListener {

	/**
	 * 堆栈数据为空时调用.
	 */
	public void onBackStackCleared();
    }

}

final class BackStackManagerImpl extends BackStackManager {
    static boolean DEBUG = true;
    static final String TAG = "BackStackManager";

    OnBackStackChangedListener mOnBackStackChanged;

    public BackStackManagerImpl(BackStackActivity activity) {
	super(activity);
	try {
	    mOnBackStackChanged = (OnBackStackChangedListener) activity;
	} catch (ClassCastException e) {
	    throw new ClassCastException(activity.toString()
		    + " must implement OnBackStackChangedListener");
	}

    }

    @Override
    public void add(BackStack backStack) {
	backStack.mIsAdded = true;
	show(backStack);
    }

    @Override
    public void show(BackStack backStack) {
	moveToState(backStack, BackStack.RESUMED);
    }

    @Override
    public BackStackManager dismiss(BackStack backStack) {
	if (backStack == null) {
	    throw new IllegalArgumentException("BackStack is null");
	}

	if (backStack.mState < BackStack.STARTED) {
	    return this;
	}

	moveToState(backStack, BackStack.CREATED);

	return this;
    }

    @Override
    public boolean popBackStack() {

	int last = mBackStackList.size() - 1;
	if (last < 0) {
	    return false;
	}

	BackStackState backStackState = mBackStackList.get(last);
	BackStack backStack = mActive.get(backStackState.mName);

	// backstack showed.
	if (backStack.mState > BackStack.CREATED) {
	    moveToState(backStack, BackStack.INITIALIZING);
	    // 需重新计算堆栈.
	    last = mBackStackList.size() - 1;
	}

	if (last > -1) {
	    BackStackState bss = mBackStackList.get(last);
	    BackStack bs = mActive.get(bss.mName);
	    if (bs != null) {
		bs.mArgs = bss.mArguments;
		bs.mFromPopBack = true;
		moveToState(bs, BackStack.RESUMED);
	    }
	} else {
	    mOnBackStackChanged.onBackStackCleared();
	}
	return true;
    }

    @Override
    public BackStackManager remove(String name) {

	int last = mBackStackList.size() - 1;

	if (last < 0) {
	    return this;
	}

	BackStackState backStackState = mBackStackList.get(last);
	BackStack backStack = mActive.get(backStackState.mName);

	if (backStackState.mName.equals(name)
		&& backStack.mState > BackStack.CREATED) {
	    dismiss(backStack);
	}

	final ArrayList<BackStackState> states = new ArrayList<BackStackState>();
	for (BackStackState bss : mBackStackList) {
	    if (bss.mName.equals(name)) {
		states.add(bss);
		removeActive(name);
	    }
	}
	mBackStackList.removeAll(states);
	if (mBackStackList.size() == 0) {
	    mOnBackStackChanged.onBackStackCleared();
	}
	return this;
    }

    @Override
    public BackStackManager removeModule(String name) {
	int last = mBackStackList.size() - 1;

	if (last < 0) {
	    return this;
	}

	BackStackState backStackState = mBackStackList.get(last);
	BackStack backStack = mActive.get(backStackState.mName);

	if (backStackState.mName.contains(name)
		&& backStack.mState > BackStack.CREATED) {
	    dismiss(backStack);
	}

	final ArrayList<BackStackState> states = new ArrayList<BackStackState>();
	for (BackStackState bss : mBackStackList) {
	    if (bss.mName.contains(name)) {
		states.add(bss);
		removeActive(bss.mName);
	    }
	}
	mBackStackList.removeAll(states);

	if (mBackStackList.size() == 0) {
	    mOnBackStackChanged.onBackStackCleared();
	}
	return this;
    }

    @Override
    public BackStackManager clearBackStack() {
	BackStack bs = getLastBackStack();
	if (bs != null && bs.mState > BackStack.CREATED) {
	    dismiss(bs);
	}
	mBackStackList.clear();
	clearActive();
	if (mBackStackList.size() == 0) {
	    mOnBackStackChanged.onBackStackCleared();
	}

	return this;
    }

    private void addActive(String name, BackStack bs) {
	BackStack last = mActive.put(name, bs);
	if (last == null) {
	    mActiveCount.put(name, 1);
	} else {
	    int count = mActiveCount.get(name);
	    mActiveCount.put(name, ++count);
	}
    }

    private void removeActive(String name) {
	Object obj = mActiveCount.get(name);
	if (obj == null) {
	    mActive.remove(name);
	    return;
	}
	int count = Integer.valueOf(String.valueOf(obj));
	count--;
	if (count <= 0) {
	    mActiveCount.remove(name);
	    mActive.remove(name);
	} else {
	    mActiveCount.put(name, count);
	    mActive.get(name);
	}
	return;
    }

    private void clearActive() {
	mActive.clear();
	mActiveCount.clear();
    }

    private void moveToState(BackStack backStack, int newState) {

	if (backStack.mState < newState) {
	    switch (backStack.mState) {
	    case BackStack.INITIALIZING:
		// 将新的页面添加入堆栈前，检查上一个页面是否需要从堆栈里移除.
		BackStack bs = getLastBackStack();
		if (bs != null && !bs.mIsAdded) {
		    int last = mBackStackList.size() - 1;
		    mBackStackList.remove(last);
		    removeActive(bs.mName);
		}
		// 在堆栈里添加新的页面.
		BackStackState backStackState = new BackStackState(backStack);
		mBackStackList.add(backStackState);
		addActive(backStackState.mName, backStack);

		// 创建页面对应的UI
		ViewGroup container = null;
		if (backStack.mContainerId != 0) {
		    container = (ViewGroup) mActivity
			    .findViewById(backStack.mContainerId);
		    if (container == null) {
			throw new IllegalArgumentException(
				"No view found for id 0x"
					+ Integer
						.toHexString(backStack.mContainerId)
					+ " for backstack " + this);
		    }
		}
		backStack.mContainer = container;

		backStack.mView = backStack.onCreateView(
			backStack.getLayoutInflater(), container);

		backStack.mState = BackStack.CREATED;
		if (backStack.mState < newState) {
		    moveToState(backStack, newState);
		}
		break;
	    case BackStack.CREATED:
		// 重刷数据.
		backStack.onViewCreated(backStack.mArgs);

		if (backStack.mContainer != null && backStack.mView != null) {
		    backStack.mContainer.addView(backStack.mView);
		}

		backStack.performStart();

		backStack.mState = BackStack.STARTED;
		if (backStack.mState < newState) {
		    moveToState(backStack, newState);
		}
		break;
	    case BackStack.STARTED:
		// animation
		if (backStack.mAnimationViews != null) {
		    for (View v : backStack.mAnimationViews) {
			v.getAnimation().start();
		    }
		}
		backStack.onResume();

		backStack.mState = BackStack.RESUMED;
		break;
	    }
	} else if (backStack.mState > newState) {
	    switch (backStack.mState) {
	    case BackStack.RESUMED:
		// animation
		if (backStack.mAnimationViews != null) {
		    for (View v : backStack.mAnimationViews) {
			if (v.getAnimation() != null
				&& !v.getAnimation().hasEnded()) {
			    v.clearAnimation();
			}
		    }
		}
		mActivity.mHandler.removeCallbacksAndMessages(null);

		backStack.onDismissed();

		backStack.mState = BackStack.STARTED;

		if (!backStack.mIsAdded && newState != BackStack.INITIALIZING) {
		    BackStack bs = getLastBackStack();
		    if (bs != null && backStack.equals(bs)) {
			mBackStackList.remove(mBackStackList.size() - 1);
			removeActive(backStack.mName);
			moveToState(backStack, BackStack.INITIALIZING);
		    }
		} else if (backStack.mState > newState) {
		    moveToState(backStack, newState);
		}
		break;
	    case BackStack.STARTED:
		if (backStack.mContainer != null && backStack.mView != null) {
		    backStack.mContainer.removeView(backStack.mView);
		}

		backStack.performStop();

		backStack.mState = BackStack.CREATED;
		if (backStack.mState > newState) {
		    moveToState(backStack, newState);
		}
		break;
	    case BackStack.CREATED:
		mBackStackList.remove(mBackStackList.size() - 1);
		removeActive(backStack.mName);

		backStack.mContainer = null;
		backStack.mView = null;

		backStack.onDestroyed();

		backStack.mState = BackStack.INITIALIZING;
		break;
	    }
	}
    }

    private BackStackState getLastBackStackState() {
	int last = mBackStackList.size() - 1;
	BackStackState bss = null;
	if (last > -1) {
	    bss = mBackStackList.get(last);
	}
	return bss;
    }

    @Override
    public BackStack getLastBackStack() {
	BackStackState bss = getLastBackStackState();
	BackStack bs = null;
	if (bss != null) {
	    bs = mActive.get(bss.mName);
	}
	return bs;
    }

    @Override
    public int getBackStackSize() {
	return mBackStackList.size();

    }

}
