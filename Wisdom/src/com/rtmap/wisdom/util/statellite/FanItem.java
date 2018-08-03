package com.rtmap.wisdom.util.statellite;

/**
 * Created by rtmap on 2016/12/9.
 */

public class FanItem {
    private int normalImgRes;
    private int foucusImgRes;
    private String name;
    private int type;

    public FanItem(int normalImgRes, int foucusImgRes, String name, int type) {
        this.normalImgRes = normalImgRes;
        this.foucusImgRes = foucusImgRes;
        this.name = name;
        this.type = type;
    }

    public int getNormalImgRes() {
        return normalImgRes;
    }

    public void setNormalImgRes(int normalImgRes) {
        this.normalImgRes = normalImgRes;
    }

    public int getFoucusImgRes() {
        return foucusImgRes;
    }

    public void setFoucusImgRes(int foucusImgRes) {
        this.foucusImgRes = foucusImgRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
