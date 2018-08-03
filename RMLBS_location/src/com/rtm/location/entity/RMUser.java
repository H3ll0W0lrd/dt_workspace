package com.rtm.location.entity;

import java.io.Serializable;

public class RMUser implements Serializable {
	private int id=1;
	private String lbsid;
	private int delaylocate_time;
	private int isbadlog_return;
	private int isphone_whitelist;
	private String expiration_time;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLbsid() {
		return lbsid;
	}
	public void setLbsid(String lbsid) {
		this.lbsid = lbsid;
	}
	public int getDelaylocate_time() {
		return delaylocate_time;
	}
	public void setDelaylocate_time(int delaylocate_time) {
		this.delaylocate_time = delaylocate_time;
	}
	public int getIsbadlog_return() {
		return isbadlog_return;
	}
	public void setIsbadlog_return(int isbadlog_return) {
		this.isbadlog_return = isbadlog_return;
	}
	public int getIsphone_whitelist() {
		return isphone_whitelist;
	}
	public void setIsphone_whitelist(int isphone_whitelist) {
		this.isphone_whitelist = isphone_whitelist;
	}
	public String getExpiration_time() {
		return expiration_time;
	}
	public void setExpiration_time(String expiration_time) {
		this.expiration_time = expiration_time;
	}
}
