package com.brassbeluga.sound.main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.components.download.DownloadPanel;
import com.brassbeluga.launcher.ui.components.songs.SongsInfoPanel;
import com.brassbeluga.launcher.ui.components.songs.TracksListPanel;
import com.brassbeluga.managers.DownloadManager;
import com.brassbeluga.sound.gson.Configuration;
import com.brassbeluga.sound.gson.RedirectResponse;
import com.brassbeluga.sound.gson.TrackInfo;
import com.brassbeluga.sound.gson.TrackStreams;
import com.brassbeluga.sound.gson.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mpatric.mp3agic.ID3Wrapper;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class DownloadLikes {

	private ID3v2 template;
	private SoundLoader load;
	private Boolean threadRunning;

	private static final int TRACK_INFO_REQUEST_SIZE = 50;
	protected static final long CHUNK_SIZE = 1000;
	
	private DownloadManager dm;

	public DownloadLikes(DownloadManager dm) throws Exception {	
		this.dm = dm;
		threadRunning = false;

		// Load the template id3 tag from resource file
		InputStream is = ResourceManager.getResourceAsStream("tagdata");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		template = new ID3v22Tag(buffer.toByteArray());
	}
	
	public void updateUserLikes(final Configuration currentConfig, final String clientID) throws JsonSyntaxException  {
		try {
			load = new SoundLoader(currentConfig, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Dispatch worker to download songs in background and update status
		SwingWorker<List<TrackInfo>, List<TrackInfo>> worker = new SwingWorker<List<TrackInfo>, List<TrackInfo>>() {

			@Override
			protected List<TrackInfo> doInBackground()
					throws JsonSyntaxException, Exception {
				String redirect = "";
				try {
					redirect = load
							.getResponse("http://api.soundcloud.com/resolve.json?url=http://soundcloud.com/"
									+ currentConfig.getUsername() + "&client_id=" + clientID);
				} catch (Exception e) {
					threadRunning = false;
					e.printStackTrace();
				}

				RedirectResponse response = new Gson().fromJson(redirect,
						RedirectResponse.class);
				UserInfo info = new Gson().fromJson(
						load.getResponse(response.getLocation()),
						UserInfo.class);

				Image image = null;
				try {
					URL url = new URL(info.getAvatarURL());
					image = ImageIO.read(url);
				} catch (IOException e) {
					e.printStackTrace();
				}

				//panel.changeIcon(image);

				Type listType = new TypeToken<ArrayList<TrackInfo>>() {
				}.getType();

				List<TrackInfo> likes = new ArrayList<TrackInfo>();

				for (int i = 0; i < info.getFavoritesCount(); i += TRACK_INFO_REQUEST_SIZE) {
					String partLikes = load
							.getResponse("http://api.soundcloud.com/users/"
									+ info.getId()
									+ "/favorites.json?client_id=" + clientID
									+ "&offset=" + i);
					List<TrackInfo> newLikes = new Gson().fromJson(partLikes,
							listType);
					for (TrackInfo t : newLikes) {
						if (dm.isTrackDownloaded(t.getId()))
							t.setDownload(true);
					}
					dm.addNewLikes(newLikes);
					likes.addAll(newLikes);
					publish(newLikes);
				}
				
				return likes;

			}

			@Override
			protected void done() {
				threadRunning = false;
			}

			@Override
			protected void process(List<List<TrackInfo>> tracks) {
				//trackPanel.addNewTracks(tracks.get(tracks.size() - 1));
			}

		};

		threadRunning = true;
		worker.execute();
	}
	
	/**
	 * Called when the user presses start
	 * 
	 * @param user
	 * @param downloadPath
	 * @throws Exception
	 * @throws JsonSyntaxException
	 */
	public void downloadTracks(final String downloadPath, final String appDataPath, 
			final String clientID, final List<TrackInfo> tracks)
			throws JsonSyntaxException, Exception {
		// gui.updateStatus("Initializing downloads",
		// SoundCloneGUI.StatusType.PROCESS);
		// Dispatch worker to download songs in background and update status
		SwingWorker<String, TrackInfo> worker = new SwingWorker<String, TrackInfo>() {

			@Override
			protected String doInBackground() throws Exception {
				Gson gson = new Gson();
				TrackStreams tStream;
				int downloads = 0;
				for (TrackInfo t : tracks) {
					if (threadRunning) {
						publish(t);
						tStream = gson.fromJson(
										load.getResponse("https://api.soundcloud.com/i1/tracks/"
												+ t.getId()
												+ "/streams?client_id="
												+ clientID), TrackStreams.class);
						String mediaPath = tStream.getHttp_mp3_128_url();
						if (mediaPath != null) {
							if (t.getArtworkURL() == null) {
								// Load uploader profile picture url for track
								// image
								UserInfo uploader = gson
										.fromJson(
												load.getResponse("https://api.soundcloud.com/users/"
														+ t.getUserId()
														+ ".json?client_id="
														+ clientID),
												UserInfo.class);
								t.setArtworkURL(uploader.getAvatarURL());
							}
							

							if (mediaPath != null) {
								
								// Open the url connection
								URL website = new URL(mediaPath);
								HttpURLConnection connect = (HttpURLConnection) website
										.openConnection();
								connect.setRequestMethod("HEAD");
								
								// Get the total length of the file
								long total = connect.getContentLengthLong();
								//downloadPanel.downloadSize += connect.getContentLengthLong();
								ReadableByteChannel rbc = null;
								rbc = Channels.newChannel(website.openStream());
								
								// Format track name to a valid file path name
								String title = t.getTitle();
								String finalPath = fuzzTrackTitle(title, downloadPath);
								String tempPath = fuzzTrackTitle(title, appDataPath);
								
								// Create all the necessary directories for file download
								File finalDir = new File(finalPath);
								finalDir.getParentFile().mkdirs();
								
								// Download the file itself to the temporary directory
								FileOutputStream fos = null;
								try {
									fos = new FileOutputStream(tempPath);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
								
								// Read the file from url, CHUNK_SIZE bytes at a time
								long pos = 0;
								long read;
								do {
									read = fos.getChannel().transferFrom(rbc,
											pos, CHUNK_SIZE);
									pos += read;
									int prog = (int)(((pos * 1.0) / (total * 1.0)) * 100.0);
									
									// Update the loading bar progress
									setProgress(prog);

								} while (read > 0  && threadRunning);
								
								// Clean up after ourselves
								fos.close();
								
								File f = new File(tempPath);
								
								if (threadRunning) {
									// Open the downloaded file as an Mp3File for tag editing
									Mp3File mp3file = new Mp3File(tempPath);
									mp3file.setId3v2Tag(template);
									ID3v2 id3v2Tag = mp3file.getId3v2Tag();
	
									// If the file name has a parseable title and artist,
									// update tag with new info
									if (title.contains(" - ")) {
										String[] halves = title.split(" - ");
										if (halves.length == 2) {
											id3v2Tag.setArtist(halves[0]);
											id3v2Tag.setTitle(halves[1]);
										}
									}
	
									// Download artwork from URL if available
									if (t.getArtworkURL() != null) {
										URL artworkURL = new URL(t
												.getArtworkURL().replace("large",
														"t500x500"));
	
										BufferedImage image = ImageIO
												.read(artworkURL.openStream());
										
										// Write the image bytes to the mp3 tag
										ByteArrayOutputStream out = new ByteArrayOutputStream();
										ImageIO.write(image, "jpg", out);
										out.flush();
										byte[] bytes = out.toByteArray();
										out.close();
	
										ID3Wrapper newId3Wrapper = new ID3Wrapper(
												new ID3v1Tag(), new ID3v23Tag());
										newId3Wrapper.setAlbumImage(bytes,
												"image/jpeg");
										id3v2Tag.setAlbumImage(bytes, 2,
												"image/jpeg");
									}
									
									// Embed track id for directory scanning
									id3v2Tag.setPaymentUrl("" + t.getId());
	
									// Save the final file in the download directory
									mp3file.save(finalPath);
									
									downloads++;
								}
								
								// Delete the temporary file
								f.delete();
							}
						}
					}
				}

				// update the current configuration's history
				load.closeHistory();

				//updateConfigFile();

				return downloads + " songs downloaded successfully!";
			}

			@Override
			protected void done() {
				threadRunning = false;
			}

			@Override
			protected void process(List<TrackInfo> chunks) {
				//downloadPanel.setCurrentTrack(chunks.get(chunks.size() - 1));
			}
		};

		threadRunning = true;
		worker.execute();
	}
	
	private String fuzzTrackTitle(String track, String path) {
		track = track.replaceAll(
				"[<>?*:|/\\\\]", " ");
		track = track.replaceAll("\"", "'");
		String finalPath = path + "/"
				+ track + ".mp3";
		return finalPath;
	}

	public Boolean isThreadRunning() {
		return threadRunning;
	}

	public void stopThread() {
		threadRunning = false;
	}

	
}
