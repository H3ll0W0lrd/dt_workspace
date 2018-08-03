package com.rtmap.wisdom.model;

import java.io.Serializable;

import com.rtm.common.model.BuildInfo;

public class UIBuildInfo implements Serializable {
	private BuildInfo build;
	private int bgIndex = -1;

	public BuildInfo getBuild() {
		return build;
	}

	public void setBuild(BuildInfo build) {
		this.build = build;
	}

	public int getBgIndex() {
		return bgIndex;
	}

	public void setBgIndex(int bgIndex) {
		this.bgIndex = bgIndex;
	}
}
