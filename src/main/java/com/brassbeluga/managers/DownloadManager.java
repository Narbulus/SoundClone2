package com.brassbeluga.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.TrackInfo;

public class DownloadManager {
	private List<TrackInfo> tracks;
	private List<DownloadsObserver> observers;
	
	public DownloadManager() {
		tracks = new ArrayList<TrackInfo>();
		observers = new ArrayList<DownloadsObserver>();
	}
	
	public synchronized void addTrack(TrackInfo trackInfo) {
		tracks.add(trackInfo);
		notifyObservers();
	}
	
	public synchronized void addAllTracks(List<TrackInfo> trackInfos) {
		tracks.addAll(trackInfos);
		notifyObservers();
	}
	
	public synchronized boolean removeTrack(TrackInfo trackInfo) {
		boolean removed = tracks.remove(trackInfo);
		notifyObservers();
		return removed;
	}
	
	public synchronized void removeAllTracks() {
		tracks.clear();
		notifyObservers();
	}
	
	public synchronized int getDownloadsSize() {
		return tracks.size();
	}
	
	/**
	 * Gets a read-only copy of the tracks list.
	 */
	public synchronized List<TrackInfo> getTracks() {
		return Collections.unmodifiableList(tracks);
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
	private void notifyObservers() {
		for (DownloadsObserver observer : observers) {
			observer.update(this);
		}
	}
	
}
