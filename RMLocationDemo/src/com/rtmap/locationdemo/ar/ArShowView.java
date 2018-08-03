package com.rtmap.locationdemo.ar;

import android.view.View;
import android.view.animation.Animation;

public abstract class ArShowView {

    private View layoutView;
    // poi点在屏幕上的位置坐标
    private float screenX = 0;
    private float screenY = 0;

    private String targetName;
    /**
     * 这个poi在地图上的x,y坐标
     * */
    private float poiTargetX;
    private float poiTargetY;

    /***
     * poi所在楼层
     */
    private String floor;

    /***
     * view动画
     */
    private Animation animation;

    /***
     * poi两点距离
     */
    private float targerDistance;


    public void setLayoutView(View view) {
        layoutView = view;
    }

    public View getLayoutView() {
        return layoutView;
    }

    /***
     * poi在屏幕上的坐标x
     * @return
     */
    public float getScreenX() {
        return screenX;
    }

    /**
     * view.setX 只有在API LEVEL 11及之后才添加，所有使用之前的低版本可能会有问题
     * @param screenX
     */
    public void setScreenX(float screenX) {
        this.screenX = screenX;
        try{
            layoutView.setX(screenX);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * poi在屏幕上的坐标y
     * @return
     */
    public float getScreenY() {
        return screenY;
    }

    /**
     * view.setY 只有在API LEVEL 11及之后才添加，所有使用之前的低版本可能会有问题
     * @param screenY
     */
    public void setScreenY(float screenY) {
        this.screenY = screenY;
        try{
            layoutView.setY(screenY);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /***
     * poi在地图上的坐标x
     * @return
     */
    public float getPoiTargetX() {
        return poiTargetX;
    }

    public void setPoiTargetX(float poiTargetX) {
        this.poiTargetX = poiTargetX;
    }

    /***
     * poi在地图上的坐标y
     * @return
     */
    public float getPoiTargetY() {
        return poiTargetY;
    }

    public void setPoiTargetY(float poiTargetY) {
        this.poiTargetY = poiTargetY;
    }

    public float getTargerDistance() {
        return targerDistance;
    }

    public void setTargerDistance(float targerDistance) {
        this.targerDistance = targerDistance;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }


    /***
     * 获取view动画
     * @return
     */
    public Animation getAnimation() {
        return animation;
    }

    /***
     * 设置view指定动画
     * @param animation
     */
    public void setAnimation(Animation animation) {
        this.animation = animation;
        layoutView.setAnimation(animation);
    }

    /***
     * 启动动画
     */
    private void startAnimation() {
        if (animation != null && !animation.hasStarted()) {
            animation.start();
        }
    }

    /***
     * 停止动画
     */
    private void stopAnimation() {
        if (animation != null && animation.hasStarted()) {
            animation.cancel();
        }
    }

    /***
     * 显示view，如果设置动画，则启动动画
     */
    public void show() {
        layoutView.setVisibility(View.VISIBLE);
        startAnimation();
    }

    /***
     * 隐藏view
     */
    public void hide() {
        layoutView.setVisibility(View.GONE);
        stopAnimation();
    }

    /***
     * 设置点击监听事件
     * @param listener
     */
    public void setOnClickListener(View.OnClickListener listener) {
        layoutView.setOnClickListener(listener);
    }

    public abstract void setDistance(float distance);
}
