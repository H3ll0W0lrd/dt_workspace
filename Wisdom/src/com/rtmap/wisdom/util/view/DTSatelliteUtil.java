package com.rtmap.wisdom.util.view;

import java.util.ArrayList;

import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import com.rtm.frm.model.PointInfo;

public class DTSatelliteUtil {

	private int raidus = 150;// 半径
	private float angle = 0;// 初始弧度

	private ArrayList<ImageView> mImageList;

	public DTSatelliteUtil() {
		mImageList = new ArrayList<ImageView>();
	}

	/**
	 * 设置弧度
	 * @param angle
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	public ArrayList<ImageView> getImageList() {
		return mImageList;
	}

	public void refreshView(PointInfo point) {
		for (int i = 0; i < mImageList.size(); i++) {
			LayoutParams params = (LayoutParams) mImageList.get(i)
					.getLayoutParams();
			params.leftMargin = (int) (point.getX() - mImageList.get(i).getWidth()/2);
			params.topMargin = (int) (point.getY() - mImageList.get(i).getHeight()/2);
			mImageList.get(i).setLayoutParams(params);
		}
	}

	public void closeAnim(AnimatorListener listener) {
		// 一共是7个子菜单，7个子菜单就是把90度分成6分，然后以这个为基准计算菜单的弧度。PI/2/6
		float myroate = (float) (Math.PI/3*2 / mImageList.size());
		for (int i = 0; i < mImageList.size(); i++) {
			// 500是半径，通过三角函数计算坐标
			float angle = myroate * i + this.angle;
			float x = (float) (raidus * Math.cos(angle));
			float y = (float) (raidus * Math.sin(angle));

			// 使用属性动画的平移动画，将坐标从现在的位置移回到原点
			ObjectAnimator animator = ObjectAnimator.ofFloat(mImageList.get(i),
					"translationY", y, 0);
			ObjectAnimator animator2 = ObjectAnimator.ofFloat(
					mImageList.get(i), "translationX", x, 0);
			// 使用AnimatorSet可以同时播放多个属性动画
			AnimatorSet set = new AnimatorSet();
			set.addListener(listener);
			// 使用自由落体的差值器
			animator.setInterpolator(new AccelerateInterpolator());
			animator2.setInterpolator(new AccelerateInterpolator());
			// 设定同时播放
			set.playTogether(animator, animator2);
			// 设定播放时间
			set.setDuration(800);
			// 开始播放动画（千万不要忘记这一行）
			set.start();
		}
	}
	
	public void startAnim() {
		startAnim(null);
	}

	public void startAnim(AnimatorListener listener) {
		float myroate = (float) (Math.PI/3*2 / mImageList.size());
		for (int i = 0; i < mImageList.size(); i++) {
			float angle = myroate * i + this.angle;
			float x = (float) (raidus * Math.cos(angle));
			float y = (float) (raidus * Math.sin(angle));

			// 使用属性动画的平移动画，将坐标从原点移动到每个子菜单对应的位置
			ObjectAnimator animator = ObjectAnimator.ofFloat(mImageList.get(i),
					"translationY", 0, y);
			ObjectAnimator animator2 = ObjectAnimator.ofFloat(
					mImageList.get(i), "translationX", 0, x);
			AnimatorSet set = new AnimatorSet();
			if(listener!=null){
				set.addListener(listener);
			}
			animator.setInterpolator(new AccelerateInterpolator());
			animator2.setInterpolator(new AccelerateInterpolator());
			set.playTogether(animator, animator2);
			set.setDuration(800);
			set.start();
		}
	}
	
}
