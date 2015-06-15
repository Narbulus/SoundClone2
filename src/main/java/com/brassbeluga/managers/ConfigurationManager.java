package com.brassbeluga.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.sound.gson.Configuration;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class ConfigurationManager {
	// Config-universal clientID and max track length
	private String clientID;
	private String maxLength;
	
	// Configuration information
	private List<Configuration> configs;
	private String appDataDir;
	private String defaultDownload;
	private Configuration currentConfig;
	
	// Track id's of already downloaded tracks
	private HashSet<Integer> oldDownloads;
	
	public ConfigurationManager() {
		loadConfigurationFiles();
		oldDownloads = new HashSet<Integer>();
	}
	
	/**
	 * Load up configuration data from local file.
	 */
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
		Scanner configScanner = null;
		
		if (oldConfig.exists()) {
			try {
				configScanner = new Scanner(oldConfig);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			configScanner = new Scanner(reader);
		}
		
		clientID = configScanner.nextLine();
		maxLength = configScanner.nextLine();

		configs = new ArrayList<Configuration>();
		currentConfig = null;

		// Load past configurations from config file's json
		while (configScanner.hasNext()) {
			String nextConfig = configScanner.nextLine();
			System.out.println("loaded config: " + nextConfig);
			Configuration newConfig = new Gson().fromJson(nextConfig,
					Configuration.class);
			configs.add(newConfig);
		}

		configScanner.close();
	}
	
	/**
	 * Updates the download path.
	 * 
	 * @param downloadPath The download path to update to
	 */
	public void updateDownloadPath(String downloadPath) {
		currentConfig.setDownloadPath(downloadPath);
		try {
			updateDownloadDirectory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateConfigFile();
	}
	
	/**
	 * Updates the current configuration to the new download directory
	 * and scans the directory for existing mp3's. If they were downloaded
	 * by SoundClone, the SoundCloud track id is parsed from the mp3 tag
	 * 
	 * @param downloadPath The new download path
	 * @throws UnsupportedTagException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	public void updateDownloadDirectory() {
		File folder = new File(currentConfig.getDownloadPath());
		File[] files = folder.listFiles();
		for (int i=0; i < files.length; i++) {
			if (files[i].isFile()) {
				File f = files[i];
				if (f.getName().contains(".mp3")) {
					Mp3File mp3file;
					try {
						mp3file = new Mp3File(f.getAbsolutePath());
					} catch (Exception e) {
						mp3file = null;
						e.printStackTrace();
					}
					ID3v2 tag = mp3file.getId3v2Tag();
					if (tag != null && tag.getPaymentUrl() != null)
						oldDownloads.add(Integer.parseInt(tag.getPaymentUrl()));
				}
			}
		}
	}
	
	/**
	 * Updates the config file with the current Configuration information.
	 */
	public void updateConfigFile() {
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
	
	public boolean isTrackDownloaded(int id) {
		return oldDownloads.contains(id);
	}
	
	/**
	 * Called when the user either enters or selects a new user to be loaded.
	 * 
	 * @param user the new user's username
	 * @param gui
	 * @throws Exception
	 * @throws JsonSyntaxException
	 * @return Returns a new status for the program
	 */
	public void updateUser(String user) {
		
		// Sanitize username input
		String select = user.trim().replace(".", "-");
		if (getCurrentUser() == null || !getCurrentUser().equals(select)) {
			try {
				currentConfig = null;
				
				// Check if a config for this username exists
				for (Configuration c : configs) {
					if (c.getUsername().equals(select)) {
						currentConfig = c;
					}
				}
				
				// Create a new config if this is a new username
				if (currentConfig == null) {
					currentConfig = new Configuration(select, defaultDownload, null);
					configs.add(currentConfig);
				}
				
				// Scan the new download directory for tracks
				if (currentConfig.getDownloadPath() != null)
					updateDownloadDirectory();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* Getters */
	
	/**
	 * @return all previous users
	 */
	public List<String> getPreviousUsers() {
		List<String> users = new ArrayList<String>();
		for (Configuration c : configs) {
			users.add(c.getUsername());
		}
		return users;
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
	 * @return the current configs download path.
	 */
	public String getDownloadPath() {
		return currentConfig.getDownloadPath();
	}

	/**
	 * @return the username of the active selected configuration
	 */
	public String getCurrentUser() {
		if (currentConfig != null)
			return currentConfig.getUsername();
		return null;
	}

	/**
	 * @return the last user
	 */
	public String getLastUser() {
		if (configs.size() > 0){
			return configs.get(0).getUsername();
		}
		return null;
	}

	/**
	 * @return the avatar URL for the current configuration
	 */
	public String getAvatarURL() {
		if (currentConfig != null)
			return currentConfig.getUserIcon();
		return null;
	}

	/**
	 * @return the clientID
	 */
	public String getClientID() {
		return clientID;
	}

	/**
	 * @return the maxLength
	 */
	public String getMaxLength() {
		return maxLength;
	}

	/**
	 * @return the configs
	 */
	public List<Configuration> getConfigs() {
		return configs;
	}

	/**
	 * @return the appDataDir
	 */
	public String getAppDataDir() {
		return appDataDir;
	}

	/**
	 * @return the currentConfig
	 */
	public Configuration getCurrentConfig() {
		return currentConfig;
	}
}