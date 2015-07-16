package com.brassbeluga.managers;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.TrackInfo;
import com.brassbeluga.sound.main.DownloadLikes;

/**
 * Manages all the work from retrieving tracks from soundcloud to
 * storing them on the user's computer.
 * 
 * @author Cameron, Spencer
 */
public class DownloadManager {
	// List of tracks to be downloaded.
	private List<TrackInfo> tracks;
	
	// Currently selected track in the likes panel
	private TrackInfo selectedTrack;
	
	// Currently flagged track in the likes panel
	private TrackInfo flaggedTrack;
	
	// Previously selected track in likes panel (For shift-clicking a range of tracks)
	private TrackInfo lastFlaggedTrack;
	
	// Text for the warnings bar on the bottom of the screen
	private String warningMessage;
	
	// List of current user's likes.
	private List<TrackInfo> likes;
	
	// List of already downloaded tracks in current download session.
	private List<TrackInfo> downloadedTracks;
	
	// Observers of download information to be notified on modification.
	private List<DownloadsObserver> observers;
	
	// Interface with soundcloud and retrieve tracks.
	private DownloadLikes downloader;
	
	// Tie image retrieval jobs to their appropriate labels.
	private HashMap<JLabel, SwingWorker<Void,Void>> imageLoads;
	
	// Configuration info.
	private ConfigurationManager cm;
	
	// 0-100 value representing download progress on current song.
	private Integer songProgress;
	
	// Handles to track likes and track downloads threads.
	SwingWorker<Void, TrackInfo> downloadsWorker;
	SwingWorker<List<TrackInfo>, List<TrackInfo>> likesWorker;
	
	public DownloadManager() {
		tracks = new ArrayList<TrackInfo>();
		likes = new ArrayList<TrackInfo>();
		downloadedTracks = new ArrayList<TrackInfo>();
		observers = new ArrayList<DownloadsObserver>();
		imageLoads = new HashMap<JLabel, SwingWorker<Void,Void>>();
		cm = new ConfigurationManager();
		
		downloadsWorker = null;
		likesWorker = null;
		try {
			downloader = new DownloadLikes(this);
		} catch (Exception e) {
			downloader = null;
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a track to the list of tracks to be downloaded. If
	 * the track is already added then nothing happens.
	 * 
	 * @param trackInfo Track to be downloaded
	 */
	public void addTrack(TrackInfo trackInfo) {
		boolean added = false;
		synchronized(tracks) {
			if (!tracks.contains(trackInfo)) {
				flaggedTrack = trackInfo;
				tracks.add(trackInfo);
				downloadedTracks.clear();
				added = true;
			}
		}
		
		// If a modification was made, let observers know.
		if (added) {
			notifyObservers(DownloadAction.TRACKS_CHANGED);
		}
	}
	
	/**
	 * Adds all tracks. WILL ADD DUPLICATES!
	 * 
	 * @param trackInfos Tracks to be added to download queue
	 */
	public void addAllTracks(List<TrackInfo> trackInfos) {
		synchronized(tracks) {
			tracks.addAll(trackInfos);
		}
		synchronized (downloadedTracks) {
			downloadedTracks.clear();
		}
		notifyObservers(DownloadAction.TRACKS_CHANGED);
	}
	
	/**
	 * Removes a track from the download queue.
	 * 
	 * @param trackInfo Track to be removed
	 * @return true if track was removed and false otherwise.
	 */
	public boolean removeTrack(TrackInfo trackInfo) {
		boolean removed;
		synchronized(tracks) {
			removed = tracks.remove(trackInfo);
		}
		notifyObservers(DownloadAction.TRACKS_CHANGED);
		return removed;
	}
	
	/**
	 * Removes all tracks from the download queue.
	 */
	public void removeAllTracks() {
		synchronized(tracks) {
			tracks.clear();
		}
		notifyObservers(DownloadAction.TRACKS_CHANGED);
	}
	
	/**
	 * Gets the number of tracks queued for download
	 */
	public int getDownloadsSize() {
		int trackSize;
		synchronized(tracks) {
			trackSize = tracks.size();
		}
		return trackSize;
	}
	
	/**
	 * Gets the number of tracks downloaded this session
	 */
	public int getDownloadedSize() {
		int downloadedSize;
		synchronized(downloadedTracks) {
			downloadedSize = downloadedTracks.size();
		}
		return downloadedSize;
	}
	
	/**
	 * Gets a read-only copy of the tracks list.
	 */
	public synchronized List<TrackInfo> getTracks() {
		return Collections.unmodifiableList(tracks);
	}
	
	/**
	 * Gets a read-only copy of the likes list.
	 */
	public synchronized List<TrackInfo> getLikes() {
		return Collections.unmodifiableList(likes);
	}
	
	/**
	 * Gets a read-only copy of the already downloaded tracks list.
	 */
	public List<TrackInfo> getDownloadedTracks() {
		return Collections.unmodifiableList(downloadedTracks);
	}
	
	/**
	 * Adds a new like track.
	 * 
	 * @param newTracks Track to be added to likes
	 */
	public void addNewLikes(List<TrackInfo> newTracks) {
		synchronized(likes) {
			likes.addAll(newTracks);
		}
		notifyObservers(DownloadAction.LIKES_CHANGED);
	}
	
	/**
	 * Adds a range of tracks, from the last flagged track to the
	 * newly selected one. For shift-clicking selection.
	 * @param The track that was just clicked
	 */
	public void addTrackRange(TrackInfo info) {
		if (this.flaggedTrack != null) {
			// If there exists a previously flagged track, selected range
			this.lastFlaggedTrack = this.flaggedTrack;
			this.flaggedTrack = info;
			notifyObservers(DownloadAction.ADD_TRACK_RANGE);
		} else {
			// Otherwise just select the new track
			addTrack(info);
		}
	}
	
	/**
	 * Removes all tracks from the likes.
	 */
	public void removeAllLikes() {
		synchronized(likes) {
			likes.clear();
		}
		notifyObservers(DownloadAction.LIKES_CLEARED);
	}
	
	/**
	 * Checks if the downloader is currently downloading.
	 */
	public boolean downloadInProgress() {
		return downloadsWorker != null;
	}
	
	/**
	 * Checks if the likes are currently updating.
	 */
	public boolean likesUpdateInProgress() {
		return likesWorker != null;
	}
	
	/**
	 * Updates the progress for the current song in download.
	 */
	public void updateSongDownloadProgress(Integer progress) {
		songProgress = progress;
		notifyObservers(DownloadAction.SONG_PROGRESS);
	}
	
	/**
	 * Gets the progress for the current song in download.
	 */
	public int getSongProgress() {
		return songProgress;
	}

	/**
	 * Begin the download.
	 */
	public void startDownload() {
		try {
			downloadsWorker = downloader.downloadTracks(cm.getAppDataDir(), cm.getClientID(), tracks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyObservers(DownloadAction.DOWNLOADS_START);
	}
	
	public void selectTrack(TrackInfo track) {
		this.selectedTrack = track;
		notifyObservers(DownloadAction.SELECT_TRACK);
	}

	/**
	 * Stop the tracks being downloaded.
	 */
	public void stopDownload() {
		downloadsWorker.cancel(true);
		
		updateTrackDownloadedFlags();
		notifyObservers(DownloadAction.DOWNLOADS_FINISHED);
	}

	/**
	 * Updates the configs download path.
	 * 
	 * @param downloadPath Path to be updated to
	 */
	public void updateDownloadPath(String downloadPath) {
		cm.updateDownloadPath(downloadPath);
		notifyObservers(DownloadAction.DOWNLOAD_PATH_CHANGED);
	}


	/**
	 * Called when the user either enters or selects a new user to be loaded.
	 * Loads the tracklist for the user
	 * 
	 * @param user the new user's username
	 */
	public void updateUser(String user) {
		// If we are currently downloading likes then stop.
		if (likesUpdateInProgress()) {
			likesWorker.cancel(true);
			removeAllLikes();
		}
		
		cm.updateUser(user);
		
		// Download the users likes
		flaggedTrack = null;
		lastFlaggedTrack = null;
		selectedTrack = null;
		removeAllLikes();
		likesWorker = downloader.updateUserLikes(cm.getCurrentConfig(), cm.getClientID());
		
		notifyObservers(DownloadAction.USERNAME_CHANGED);
	}
	
	public void onUserArtworkLoaded() {
		notifyObservers(DownloadAction.USER_ARTWORK_LOADED);
	}
	
	public void setWarningMessage(String message) {
		warningMessage = message;
		notifyObservers(DownloadAction.WARNING_MESSAGE_CHANGED);
	}
	
	/**
	 * Gets the configuration manager for this download session.
	 * DO NOT make state changes directly to configuration manager,
	 * leave that to the DownloadManager.
	 * Read-only.
	 */
	public ConfigurationManager getConfig() {
		return cm;
	}
	
	/**
	 * Toggles the download flag on a track at index.
	 */
	public void toggleDownload(int index) {
		tracks.get(index).setDownload(!tracks.get(index).getDownload());
	}

	/**
	 * To be called when the track likes worker has completed.
	 */
	public void onLikesFinished() {
		likesWorker = null;
		notifyObservers(DownloadAction.LIKES_FINISHED);
		cm.updateConfigFile();
	}
	
	public void updateTrackDownloadedFlags() {
		cm.updateDownloadDirectory();
		for ( TrackInfo t : likes ) {
			if (cm.isTrackDownloaded(t.getId())) {
				t.setDownload(true);
			}
		}
	}

	/**
	 * To be called when the track downloads worker has completed.
	 */
	public void onDownloadsFinished() {
		downloadsWorker = null;
		
		// Some new tracks will be need to be flagged as having already
		// been downloaded.
		updateTrackDownloadedFlags();
		
		notifyObservers(DownloadAction.DOWNLOADS_FINISHED);
	}

	/**
	 * Called once a given track has been downloaded.
	 */
	public void trackDownloaded(TrackInfo trackInfo) {
		downloadedTracks.add(trackInfo);
		removeTrack(trackInfo);
	}
	
	/**
	 * Downloads an icon for a given label.
	 * 
	 * @param url URL to retrieve icon from
	 * @param label label to apply icon to
	 */
	public void downloadLabelIcon(final String url, final JLabel label) {
		downloadLabelIcon(url, label, null);
	}
	
	/**
	 * Downloads an icon for a given label.
	 * 
	 * @param url URL to retrieve icon from
	 * @param replaceSize new size
	 * @param label label to apply icon to
	 */
	public void downloadLabelIcon(TrackInfo t, String replaceSize, final JLabel label) {
		if (t.getArtworkURL() != null)
			downloadLabelIcon(t.getArtworkURL().replace("-large", replaceSize), label, null);
	}
	
	/**
	 * Downloads an icon for a given label.
	 * 
	 * @param url URL to retrieve icon from
	 * @param replaceSize new size
	 * @param label label to apply icon to
	 * @param alt Image to be used if artwork cannot be retrieved.
	 */
	public void downloadLabelIcon(TrackInfo t, String replaceSize, final JLabel label, final ImageIcon alt) {
		if (t.getArtworkURL() != null)
			downloadLabelIcon(t.getArtworkURL().replace("-large", replaceSize), label, alt);
		else
			label.setIcon(alt);
	}
	
	/**
	 * Downloads an icon for a given label.
	 * 
	 * @param url URL to retrieve icon from
	 * @param label label to apply icon to
	 * @param alt Image to be used if artwork cannot be retrieved.
	 */
	public void downloadLabelIcon(final String url, final JLabel label, final ImageIcon alt) {
		if (imageLoads.containsKey(label)) {
			imageLoads.get(label).cancel(true);
			imageLoads.remove(label);
		}
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			
			@Override
			protected Void doInBackground() {
				BufferedImage image = downloader.downloadArtwork(url);
				if (image != null) {
					label.setIcon(new ImageIcon(image));
					label.repaint();
				}else if (alt != null) {
					label.setIcon(alt);
					label.repaint();
				}
				return null;
			}
			
			@Override
			protected void done() {
				imageLoads.remove(label);
			}

		};
		imageLoads.put(label, worker);
		worker.execute();
	}

	/**
	 * Add an observer to be notified when changes are made.
	 * 
	 * @param observer Observer to be notified
	 */
	public void addObserver(DownloadsObserver observer) {
		observers.add(observer);
	}


	/**
	 * Notify all observers that something has changed.
	 */
	private void notifyObservers(DownloadAction action) {
		for (DownloadsObserver observer : observers) {
			observer.update(this, action);
		}
	}

	/**
	 * Returns the next track queued for download
	 */
	public TrackInfo getNextTrack() {
		if (tracks.size() > 0)
			return tracks.get(0);
		return null;
	}

	public String getWarningMessage() {
		return warningMessage;
	}

	public TrackInfo getSelectedTrack() {
		return selectedTrack;
	}
	
	public TrackInfo getFlaggedTrack() {
		return flaggedTrack;
	}
	
	public TrackInfo getLastFlaggedTrack() {
		return lastFlaggedTrack;
	}
	
}
