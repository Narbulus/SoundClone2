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
	private String downloadPath;
	private DownloadLikes downloader;
	
	// Config-universal clientID and max track length
	private String clientID;
	private int maxLength;
	
	// Configuration information
	private List<Configuration> configs;
	private String appDataDir;
	private String defaultDownload;
	private Configuration currentConfig;
	// Track id's of already downloaded tracks
	private HashSet<Integer> downloaded;
	
	public DownloadManager() {
		tracks = new ArrayList<TrackInfo>();
		observers = new ArrayList<DownloadsObserver>();
		loadConfigurationFiles();
		downloadPath = null;
		try {
			downloader = new DownloadLikes();
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
		maxLength = config.nextInt();

		configs = new ArrayList<Configuration>();
		currentConfig = null;
		downloaded = new HashSet<Integer>();

		// Load past configurations from config file's json
		while (config.hasNext()) {
			String nextConfig = config.nextLine();
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
				notifyObservers();
			}
		}
	}
	
	public void addAllTracks(List<TrackInfo> trackInfos) {
		synchronized(tracks) {
			tracks.addAll(trackInfos);
		}
		notifyObservers();
	}
	
	public boolean removeTrack(TrackInfo trackInfo) {
		boolean removed;
		synchronized(tracks) {
			removed = tracks.remove(trackInfo);
		}
		notifyObservers();
		return removed;
	}
	
	public void removeAllTracks() {
		synchronized(tracks) {
			tracks.clear();
		}
		notifyObservers();
	}
	
	public int getDownloadsSize() {
		synchronized(tracks) {
			return tracks.size();
		}
	}
	
	public void updateUserLikes(String user, SongsInfoPanel siPanel, TracksListPanel tlPanel) {
		downloader.updateUserLikes(currentConfig, clientID);
	}
	
	public void addNewTrackInfo(List<TrackInfo> newTracks) {
		
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
	public void startDownload() {
		try {
			downloader.downloadTracks("narbulus", downloadPath,  tracks);
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

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
		try {
			updateDownloadDirectory(downloadPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyObservers();
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
	public void updateUser(final String user) throws JsonSyntaxException,
			Exception {
		for (Configuration c : configs) {
			if (c.getUsername().equals(user))
				currentConfig = c;
		}

		if (currentConfig == null || user != currentConfig.getUsername()) {
			currentConfig = new Configuration(user, defaultDownload, null);
			configs.add(currentConfig);
		}
		
		if (currentConfig.getDownloadPath() != null)
			updateDownloadDirectory(currentConfig.getDownloadPath());

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
	public void updateDownloadDirectory(String downloadPath) throws UnsupportedTagException, InvalidDataException, IOException {
		currentConfig.setDownloadPath(downloadPath);
		
		File folder = new File(downloadPath);
		File[] files = folder.listFiles();
		for (int i=0; i < files.length; i++) {
			if (files[i].isFile()) {
				File f = files[i];
				if (f.getName().contains(".mp3")) {
					Mp3File mp3file = new Mp3File(f.getAbsolutePath());
					ID3v2 tag = mp3file.getId3v2Tag();
					if (tag.getPaymentUrl() != null)
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
	
	private void updateConfigFile() throws FileNotFoundException {
		// write the configurations to file
		PrintStream output = new PrintStream(appDataDir + "/config");

		output.println(clientID);
		output.println(maxLength);

		Gson gson = new Gson();
		
		for (Configuration c : configs) {
			output.println(gson.toJson(c));
		}

		output.flush();
		output.close();
	}
}
