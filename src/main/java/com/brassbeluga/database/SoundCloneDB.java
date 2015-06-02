package com.brassbeluga.database;

import java.net.InetAddress;
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

	/**
	 * Default no-arg constructor.
	 */ 
	public SoundCloneDB() {
		MongoClient mongoClient = null;

		try {
			MongoCredential credential = MongoCredential
					.createMongoCRCredential("client_user", "soundclone",
							"password".toCharArray());

			mongoClient = new MongoClient(new ServerAddress(
					InetAddress.getByName("ds031872.mongolab.com"), 31872),
					Arrays.asList(credential));
		} catch (UnknownHostException e) {
			System.err.println("Error contacting host: " + e.getMessage());
			e.printStackTrace();
		}

		db = mongoClient.getDB("soundclone");
	}

	/**
	 * Submits download statistics.
	 */
	public void submitDownload(String user, String mac, String ip, long downloadSize, int tracksDownloaded) {
		BasicDBObject document = new BasicDBObject();
		document.put("user", user);
		document.put("MAC", mac);
		document.put("IP", ip);
		document.put("size", Math.round(downloadSize / (1024.0 * 1024.0)));
		document.put("total_tracks", tracksDownloaded);

		DBCollection collection = db.getCollection("downloads");
		collection.insert(document);
	}

}
