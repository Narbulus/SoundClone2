package com.brassbeluga.database;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class SoundCloneDB {
	private DB db;
	private String ip;
	private String mac;
	private String hostName;

	/**
	 * Default no-arg constructor.
	 */ 
	public SoundCloneDB() {
		try {
			MongoCredential credential = MongoCredential
					.createMongoCRCredential("client_user", "soundclone",
							"password".toCharArray());

			MongoClient mongoClient = new MongoClient(new ServerAddress(
					InetAddress.getByName("ds031872.mongolab.com"), 31872),
					Arrays.asList(credential));
			
			db = mongoClient.getDB("soundclone");
			setUserInfo();
		} catch (UnknownHostException e) {
			System.err.println("Error contacting host: " + e.getMessage());
			e.printStackTrace();
			db = null;
		}
	}
	
	/**
	 * Sets the MAC and IP addresses as well as host name for db submittal at a later time.
	 */
	private void setUserInfo() {
		// Default values
		mac = "N/A";
		ip = "N/A";
		hostName = "N/A";
		
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(
			                whatismyip.openStream()));

			ip = in.readLine(); //you get the external IP as a String
			
			// Get the MAC address now.
			InetAddress ip = InetAddress.getLocalHost();
			hostName = ip.getHostName();
	        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	        byte[] macBytes = network.getHardwareAddress();

	
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < macBytes.length; i++) {
	            sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? "-" : ""));        
	        }
	        
	        mac = sb.toString();
        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Submits download statistics.
	 */
	public void submitDownload(final String user, final long downloadSize, final int tracksDownloaded) {
		if (db != null) { 
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					BasicDBObject document = new BasicDBObject();
					document.put("user", user);
					document.put("hostname", hostName);
					document.put("MAC", mac);
					document.put("IP", ip);
					document.put("size", Math.round(downloadSize * 10 / (1024.0 * 1024.0)) / 10.0);
					document.put("total_tracks", tracksDownloaded);
			
					DBCollection collection = db.getCollection("downloads");
					collection.insert(document);
				}
			});
		}
	}

}
