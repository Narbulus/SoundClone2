package com.brassbeluga.database;

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
	private String userIP;
	private String userMAC;

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
			setUserAddresses();
		} catch (UnknownHostException e) {
			System.err.println("Error contacting host: " + e.getMessage());
			e.printStackTrace();
			db = null;
		}
	}
	
	/**
	 * Sets the MAC and IP addresses for db submittal at a later time.
	 */
	private void setUserAddresses() {
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(
			                whatismyip.openStream()));

			userIP = in.readLine(); //you get the external IP as a String
			
			// Get the MAC address now.
			InetAddress ip = InetAddress.getLocalHost();
	        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	        byte[] mac = network.getHardwareAddress();

	
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < mac.length; i++) {
	            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));        
	        }
	        
	        userMAC = sb.toString();
        
		} catch (Exception e) {
			userMAC = "N/A";
			userIP = "N/A";
		}
	}

	/**
	 * Submits download statistics.
	 */
	public void submitDownload(String user, long downloadSize, int tracksDownloaded) {
		if (db != null) { 
			BasicDBObject document = new BasicDBObject();
			document.put("user", user);
			document.put("MAC", userMAC);
			document.put("IP", userIP);
			document.put("size", Math.round(downloadSize * 10 / (1024.0 * 1024.0)) / 10.0);
			document.put("total_tracks", tracksDownloaded);
	
			DBCollection collection = db.getCollection("downloads");
			collection.insert(document);
		}
	}

}
