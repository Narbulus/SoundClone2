package com.brassbeluga.sound.gson;

public class Configuration {
	
	private String username;
	private String downloadPath;
	private String userIcon;
	private int[] history;
	
	public Configuration (String username, String downloadPath, int[] history) {
		this.username = username;
		this.downloadPath = downloadPath;
		this.history = history;
		this.userIcon = "";
	}
	
	public void setUserIcon(String icon) {
		this.userIcon = icon;
	}
	
	public String getUserIcon() {
		return userIcon;
	}

	public int[] getHistory() {
		return history;
	}

	public void setHistory(int[] history) {
		this.history = history;
	}

	public String getUsername() {
		return username;
	}

	public String getDownloadPath() {
		return downloadPath;
	}
	
	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

}
