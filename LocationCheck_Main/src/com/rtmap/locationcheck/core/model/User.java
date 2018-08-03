package com.rtmap.locationcheck.core.model;

public class User {
	private static User instance;
	
	private String mUserName;
	private String mPassword;
	private String mToken;
	
	public static User getInstance() {
		if(instance == null) {
			instance = new User();
		}
		
		return instance;
	}
	
	public String getUserName() {
		return mUserName;
	}
	
	public String getPassword() {
		return mPassword;
	}
	
	public String getToken() {
		return mToken;
	}
	
	public void setUserName(String name) {
		mUserName = name;
	}
	
	public void setPassword(String password) {
		mPassword = password;
	}
	
	public void setToken(String token) {
		mToken = token;
	}
}