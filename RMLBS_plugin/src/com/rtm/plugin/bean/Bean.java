package com.rtm.plugin.bean;

import com.rtm.common.model.POI;

public class Bean extends POI{
    private boolean isEaten;

    public Bean(int id, String buildId, String floorId, float x, float y, String name) {
        super();
        setPoiNo(id);
        setBuildId(buildId);
        setFloor(floorId);
        this.x = x;
        this.y = Math.abs(y);
        this.name = name;
        this.isEaten = false;
    }

    public Bean(int id, String buildId, String floorId, float x, float y,String name, boolean isEaten) {
        super();
        setPoiNo(id);
        setBuildId(buildId);
        setFloor(floorId);
        this.x = x;
        this.y = Math.abs(y);
        this.name = name;
        this.isEaten = isEaten;
    }

    public boolean isEaten() {
        return isEaten;
    }

    public void setEaten(boolean isEaten) {
        this.isEaten = isEaten;
    }
}
