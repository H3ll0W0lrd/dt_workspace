package com.rtmap.wisdom.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "build_table")
public class MyBuild implements Serializable {
	@DatabaseField(columnName = "build_id", id = true)
	private String buildId;
	@DatabaseField(columnName = "content")
	private String content;
	@DatabaseField(columnName = "time")
	private long time;

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
