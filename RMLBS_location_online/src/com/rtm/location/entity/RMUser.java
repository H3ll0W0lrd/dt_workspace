package com.rtm.location.entity;

import java.io.Serializable;

public class RMUser implements Serializable {
	private int id=1;
	private String lbsid;
	private int isbadlog_return;
	private int isphone_whitelist;
	private String expiration_time;
	private int log_record_time;
	private String bei_md5;
	private String bei_url;
	
	public String getBei_url() {
		return bei_url;
	}
	public void setBei_url(String bei_url) {
		this.bei_url = bei_url;
	}
	public int getLog_record_time() {
		return log_record_time;
	}
	public void setLog_record_time(int log_record_time) {
		this.log_record_time = log_record_time;
	}
	public String getBei_md5() {
		return bei_md5;
	}
	public void setBei_md5(String bei_md5) {
		this.bei_md5 = bei_md5;
	}
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
