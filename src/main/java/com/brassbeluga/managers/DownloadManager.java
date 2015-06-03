package com.brassbeluga.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingWorker;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.components.download.DownloadPanel;
import com.brassbeluga.launcher.ui.components.songs.SongsInfoPanel;
import com.brassbeluga.launcher.ui.components.songs.TracksListPanel;
import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.Configuration;
import com.brassbeluga.sound.gson.TrackInfo;
import com.brassbeluga.sound.main.DownloadLikes;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class DownloadManager {
	// List of tracks to be downloaded
	private List<TrackInfo> tracks;
	// List of current user's likes
	private List<TrackInfo> likes;
	private List<DownloadsObserver> observers;
	private DownloadLikes downloader;
	
	// Config-universal clientID and max track length
	private String clientID;
	private String maxLength;
	
	// Configuration information
	private List<Configuration> configs;
	private String appDataDir;
	private String defaultDownload;
	private Configuration currentConfig;
	// Track id's of already downloaded tracks
	private HashSet<Integer> downloaded;
	
	private Integer songProgress;
	
	SwingWorker<String, TrackInfo> downloadsWorker;
	SwingWorker<List<TrackInfo>, List<TrackInfo>> likesWorker;
	
	public DownloadManager() {
		tracks = new ArrayList<TrackInfo>();
		likes = new ArrayList<TrackInfo>();
		observers = new ArrayList<DownloadsObserver>();
		downloadsWorker = null;
		likesWorker = null;
		loadConfigurationFiles();
		try {
			downloader = new DownloadLikes(this);
		} catch (Exception e) {
			downloader = null;
			e.printStackTrace();
		}
	}
	
	private void loadConfigurationFiles() {
		// Create download locations if nonexistent
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();
		if (OS.contains("WIN")) {
			workingDirectory = System.getenv("AppData");
		} else if (OS.contains("MAC")) {
			workingDirectory = System.getProperty("user.home");
			workingDirectory += "/Library/Application Support";
		} else if (OS.contains("NIX") || OS.contains("NUX") || OS.contains("AIX")) {
			workingDirectory = System.getProperty("user.home");
			workingDirectory += "/.config/";
		} else {
			System.out.println("Warning: OS not recognized!");
			workingDirectory = "";
		}

		appDataDir = workingDirectory + "/SoundClone";

		File tempFile = new File(appDataDir);
		tempFile.mkdirs();

		defaultDownload = System.getProperty("user.home");

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				ResourceManager.getResourceAsStream("config")));
		File oldConfig = new File(appDataDir + "/config");
		Scanner config = null;
		
		if (oldConfig.exists()) {
			try {
				config = new Scanner(oldConfig);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			config = new Scanner(reader);
		}
		
		clientID = config.nextLine();
		maxLength = config.nextLine();

		configs = new ArrayList<Configuration>();
		currentConfig = null;
		downloaded = new HashSet<Integer>();

		// Load past configurations from config file's json
		while (config.hasNext()) {
			String nextConfig = config.nextLine();
			System.out.println("loaded config: " + nextConfig);
			Configuration newConfig = new Gson().fromJson(nextConfig,
					Configuration.class);
			configs.add(newConfig);
		}

		config.close();
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
	
	public synchronized void removeAllTracks() {
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
	
	public synchronized List<TrackInfo> getLikes() {
		return Collections.unmodifiableList(likes);
	}
	
	public void onLikesFinished() {
		likesWorker = null;
		notifyObservers(DownloadAction.LIKES_FINISHED);
		updateConfigFile();
	}
	
	public void addNewLikes(List<TrackInfo> newTracks) {
		synchronized(likes) {
			likes.addAll(newTracks);
		}
		notifyObservers(DownloadAction.LIKES_CHANGED);
	}
	
	public synchronized void removeAllLikes() {
		likes.clear();
		notifyObservers(DownloadAction.LIKES_CLEARED);
	}
	
	public boolean isTrackDownloaded(int id) {
		return downloaded.contains(id);
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
	 * Begin the download.
	 */
	public void startDownload() {
		try {
			downloadsWorker = downloader.downloadTracks(appDataDir, clientID, tracks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateSongDownloadProgress(Integer progress) {
		songProgress = progress;
		notifyObservers(DownloadAction.SONG_PROGRESS);
	}
	
	public int getSongProgress() {
		return songProgress;
	}

	public void stopDownload() {
		downloadsWorker.cancel(true);
	}
	
	/**
	 * Gets a read-only copy of the tracks list.
	 */
	public synchronized List<TrackInfo> getTracks() {
		return Collections.unmodifiableList(tracks);
	}

	public void setDownloadPath(String downloadPath) {
		currentConfig.setDownloadPath(downloadPath);
		try {
			updateDownloadDirectory();
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
	
	/**
	 * Called when the user either enters or selects a new user to be loaded.
	 * Loads the tracklist for the user
	 * 
	 * @param user
	 *            The new user's username
	 * @param gui
	 * @throws Exception
	 * @throws JsonSyntaxException
	 * @return Returns a new status for the program
	 */
	public void updateUser(String user) {
		
		// If we are currently downloading likes then stop.
		if (likesUpdateInProgress()) {
			likesWorker.cancel(true);
			removeAllLikes();
		}
		
		// Sanitize username input
		String select = user.trim().replace(".", "-");
		if (getCurrentUser() == null || (getCurrentUser() != null 
				&& !getCurrentUser().equals(select))) {
			try {
				// Check if a config for this username exists
				for (Configuration c : configs) {
					if (c.getUsername().equals(user))
						currentConfig = c;
				}
				
				// Create a new config if this is a new username
				if (currentConfig == null || user != currentConfig.getUsername()) {
					currentConfig = new Configuration(user, defaultDownload, null);
					configs.add(currentConfig);
				}
				
				// Scan the new download directory for tracks
				if (currentConfig.getDownloadPath() != null)
					updateDownloadDirectory();
				
				// Download the users likes
				removeAllLikes();
				likesWorker = downloader.updateUserLikes(currentConfig, clientID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Updates the current configuration to the new download directory
	 * and scans the directory for existing mp3's. If they were downloaded
	 * by SoundClone, the SoundCloud track id is parsed from the mp3 tag
	 * @param downloadPath The new download path
	 * @throws UnsupportedTagException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	public void updateDownloadDirectory() throws UnsupportedTagException, InvalidDataException, IOException {
		File folder = new File(getDownloadPath());
		File[] files = folder.listFiles();
		for (int i=0; i < files.length; i++) {
			if (files[i].isFile()) {
				File f = files[i];
				if (f.getName().contains(".mp3")) {
					Mp3File mp3file = new Mp3File(f.getAbsolutePath());
					ID3v2 tag = mp3file.getId3v2Tag();
					if (tag != null && tag.getPaymentUrl() != null)
						downloaded.add(Integer.parseInt(tag.getPaymentUrl()));
				}
			}
		}
	}
	
	/**
	 * Checks if the given path conflicts with the download path of the current
	 * configuration
	 * 
	 * @param downloadPath
	 *            The new download path
	 * @return true if the downloadPath differs from the configuration's path
	 */
	public boolean isNewPath(String downloadPath) {
		// If a new path is specified, clear history on config so new files are
		// downloaded
		return !downloadPath.equals(currentConfig.getDownloadPath());
	}

	/**
	 * Returns the names of the users for which a configuration exists from
	 * prior runs
	 * 
	 * @return
	 */
	public String[] getConfigNames() {
		String[] names = new String[configs.size()];
		int i = 0;
		for (Configuration c : configs) {
			names[i] = c.getUsername();
			i++;
		}
		return names;
	}

	/**
	 * Returns the username of the active selected configuration
	 * 
	 * @return
	 */
	public String getCurrentUser() {
		if (currentConfig != null)
			return currentConfig.getUsername();
		return null;
	}
	
	public String getLastUser() {
		if (configs.size() > 0){
			return configs.get(0).getUsername();
		}
		return null;
	}

	public String getDownloadPath() {
		if (currentConfig != null)
			return currentConfig.getDownloadPath();
		return defaultDownload;
	}
	
	public void toggleDownload(int index) {
		tracks.get(index).setDownload(!tracks.get(index).getDownload());
	}
	
	public List<String> getPreviousUsers() {
		List<String> users = new ArrayList<String>();
		for (Configuration c : configs) {
			users.add(c.getUsername());
		}
		return users;
	}
	
	private void updateConfigFile() {
		// write the configurations to file
		PrintStream output = null;
		try {
			output = new PrintStream(appDataDir + "/config");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		output.println(clientID);
		output.println(maxLength);

		Gson gson = new Gson();
		
		for (Configuration c : configs) {
			output.println(gson.toJson(c));
		}

		output.flush();
		output.close();
	}

	public void onDownloadsFinished() {
		downloadsWorker = null;
		notifyObservers(DownloadAction.DOWNLOADS_FINISHED);
	}
}
