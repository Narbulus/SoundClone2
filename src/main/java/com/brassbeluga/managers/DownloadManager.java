package com.brassbeluga.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.brassbeluga.launcher.ui.components.download.DownloadPanel;
import com.brassbeluga.launcher.ui.components.songs.SongsInfoPanel;
import com.brassbeluga.launcher.ui.components.songs.TracksListPanel;
import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.TrackInfo;
import com.brassbeluga.sound.main.DownloadLikes;

public class DownloadManager {
	private List<TrackInfo> tracks;
	private List<DownloadsObserver> observers;
	private String downloadPath;
	private DownloadLikes downloader;
	
	public DownloadManager() {
		tracks = new ArrayList<TrackInfo>();
		observers = new ArrayList<DownloadsObserver>();
		downloadPath = null;
		try {
			downloader = new DownloadLikes(this);
		} catch (Exception e) {
			downloader = null;
			e.printStackTrace();
		}
	}
	
	public void addTrack(TrackInfo trackInfo) {
		synchronized(tracks) {
			if (!tracks.contains(trackInfo)) {
				tracks.add(trackInfo);
				notifyObservers(DownloadAction.TRACKS_CHANGED);
			}
		}
	}
	
	public void addAllTracks(List<TrackInfo> trackInfos) {
		synchronized(tracks) {
			tracks.addAll(trackInfos);
		}
		notifyObservers(DownloadAction.TRACKS_CHANGED);
	}
	
	public boolean removeTrack(TrackInfo trackInfo) {
		boolean removed;
		synchronized(tracks) {
			removed = tracks.remove(trackInfo);
		}
		notifyObservers(DownloadAction.TRACKS_CHANGED);
		return removed;
	}
	
	public void removeAllTracks() {
		synchronized(tracks) {
			tracks.clear();
		}
		notifyObservers(DownloadAction.TRACKS_CHANGED);
	}
	
	public int getDownloadsSize() {
		synchronized(tracks) {
			return tracks.size();
		}
	}
	
	public String getLastUser() {
		return downloader.getLastUser();
	}
	
	public List<String> getPreviousUsers() {
		return downloader.getPreviousUsers();
	}
	
	public String getCurrentUser() {
		return downloader.getCurrentUser();
	}
	
	public void updateUser(String user) {
		try {
			downloader.updateUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateUserLikes(String user, SongsInfoPanel siPanel, TracksListPanel tlPanel) {
		downloader.updateUserLikes(user, siPanel, tlPanel);
	}
	
	/**
	 * Checks if the downloader is currently downloading.
	 */
	public boolean downloadInProgress() {
		return downloader.isThreadRunning();
	}
	
	/**
	 * Begin the download.
	 */
	public void startDownload(DownloadPanel downloadPanel) {
		try {
			downloader.downloadTracks("narbulus",downloadPath, downloadPanel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stops the current download in progress.
	 */
	public void stopDownload() {
		downloader.stopThread();
	}
	
	/**
	 * Gets a read-only copy of the tracks list.
	 */
	public synchronized List<TrackInfo> getTracks() {
		return Collections.unmodifiableList(tracks);
	}
	
	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
		try {
			downloader.updateDownloadDirectory(downloadPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyObservers(DownloadAction.DOWNLOAD_PATH_CHANGED);
	}

	/**
	 * Add an observer to be notified when the underlying list of
	 * tracks to be downloaded is changed.
	 * 
	 * @param observer Observer to be notified
	 */
	public void addObserver(DownloadsObserver observer) {
		observers.add(observer);
	}
	
	/**
	 * Notify all observers that the tracks to be downloaded has changed
	 */
	private void notifyObservers(DownloadAction action) {
		for (DownloadsObserver observer : observers) {
			observer.update(this, action);
		}
	}
	
}
