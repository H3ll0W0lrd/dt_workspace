package com.rtmap.indoor_switch.manager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import java.util.Stack;

/**
 * Created by fushenghua on 15/3/17.
 */
public class FraManager {
    private static Stack<Fragment> fragmentStack;
    private static FraManager instance;
    private static final Object obj = new Object();

    private FraManager() {
    }

    /**
     * 单一实例
     */
    public static FraManager getFraManager() {
        if (instance == null) {
            synchronized (obj) {
                if (instance == null) {
                    instance = new FraManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加Fragment到堆栈
     */
    public static void pushFragment(FragmentActivity fact, Fragment fragment, int layoutID, String... title) {
        if (fragmentStack == null) {
            fragmentStack = new Stack<Fragment>();
        }
        fragmentStack.push(fragment);
        FragmentTransaction ft = fact.getSupportFragmentManager().beginTransaction();

        ft.replace(layoutID, fragment);
        if (fragmentStack.size() > 1) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    /**
     * 弹出Fragment
     */
    public static Fragment popFragment(FragmentActivity fact) {
        Fragment fragment = fragmentStack.pop();
        FragmentTransaction ft = fact.getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
        return fragment;
    }

    /**
     * 弹出指定Activity中所有的fragment
     */
    public static void finishFragment(FragmentActivity fact) {
        for (int i = 0, size = fragmentStack.size(); i < size; i++) {
            Fragment stackFragment = fragmentStack.get(i);
            if (null != stackFragment) {
                Fragment fragment = stackFragment;
                if (fact == fragment.getActivity()) {
                    FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
                    ft.remove(fragment);
                    fragmentStack.remove(i);
                    ft.commit();
                }
            }
        }
    }

    /**
     * 清除栈中所有的项
     */
    public static void clearStack() {
        if (fragmentStack != null && fragmentStack.isEmpty()) {
            fragmentStack.clear();
        }
    }
}
