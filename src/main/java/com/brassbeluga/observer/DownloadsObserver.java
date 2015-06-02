package com.brassbeluga.observer;

import com.brassbeluga.managers.DownloadManager;

public interface DownloadsObserver {
	public void update(DownloadManager dm);
}
