package com.rtm.frm.dianxin.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by ly on 15-8-17.
 */
public class PrivateBuild {


    @JSONField(name = "buildName")
    private String buildName;
    @JSONField(name = "floor")
    private JSONArray floor;
    @JSONField(name = "buildId")
    private String buildId;

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public JSONArray getFloor() {
        return floor;
    }

    public void setFloor(JSONArray floor) {
        this.floor = floor;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }
}
