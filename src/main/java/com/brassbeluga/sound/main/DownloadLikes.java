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
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import com.brassbeluga.launcher.resources.ResourceManager;
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
import com.mpatric.mp3agic.Mp3File;

public class DownloadLikes {

	private ID3v2 template;

	private DownloadManager dm;

	private static final int TRACK_INFO_REQUEST_SIZE = 50;
	protected static final long CHUNK_SIZE = 1000;
	
	public DownloadLikes(DownloadManager dm) throws Exception {
		this.dm = dm;

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
	
	public String getResponse(String urlPath) throws Exception {
		URL url = new URL(urlPath);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setDoOutput(true);
		connect.setDoInput(true);
		
		connect.setRequestMethod("GET");
		int responseCode = connect.getResponseCode();
		
		String line;
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
	
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		
		return buffer.toString();
				
	}
	
	public SwingWorker<List<TrackInfo>, List<TrackInfo>> updateUserLikes(final Configuration currentConfig, final String clientID) throws JsonSyntaxException  {
		// Dispatch worker to download songs in background and update status
		SwingWorker<List<TrackInfo>, List<TrackInfo>> worker = new SwingWorker<List<TrackInfo>, List<TrackInfo>>() {

			@Override
			protected List<TrackInfo> doInBackground()
					throws JsonSyntaxException, Exception {
				String redirect = "";
				try {
					redirect = getResponse("http://api.soundcloud.com/resolve.json?url=http://soundcloud.com/"
									+ currentConfig.getUsername() + "&client_id=" + clientID);
				} catch (Exception e) {
					e.printStackTrace();
				}

				RedirectResponse response = new Gson().fromJson(redirect,
						RedirectResponse.class);
				UserInfo info = new Gson().fromJson(getResponse(response.getLocation()),
						UserInfo.class);
				
				currentConfig.setUserIcon(info.getAvatarURL());

				Type listType = new TypeToken<ArrayList<TrackInfo>>() {
				}.getType();

				List<TrackInfo> likes = new ArrayList<TrackInfo>();
				int i = 0;
				while (i < info.getFavoritesCount() && !isCancelled()) {
					String partLikes = getResponse("http://api.soundcloud.com/users/"
									+ info.getId()
									+ "/favorites.json?client_id=" + clientID
									+ "&offset=" + i);
					List<TrackInfo> newLikes = new Gson().fromJson(partLikes,
							listType);
					for (TrackInfo t : newLikes) {
						if (dm.getConfig().isTrackDownloaded(t.getId()))
							t.setDownload(true);
					}
					likes.addAll(newLikes);
					
					// Make sure thread wasn't cancelled since we last checked.s
					if (!isCancelled()) {
						dm.addNewLikes(likes);
					}
					i += TRACK_INFO_REQUEST_SIZE;
				}
				
				return likes;

			}

			@Override
			protected void done() {
				dm.onLikesFinished();
			}

		};

		worker.execute();
		return worker;
	}
	
	/**
	 * Called when the user presses start
	 * 
	 * @param user
	 * @param downloadPath
	 * @throws Exception
	 * @throws JsonSyntaxException
	 */
	public SwingWorker<Void, TrackInfo> downloadTracks(final String appDataPath, 
			final String clientID, final List<TrackInfo> tracks)
			throws JsonSyntaxException, Exception {
		// gui.updateStatus("Initializing downloads",
		// SoundCloneGUI.StatusType.PROCESS);
		// Dispatch worker to download songs in background and update status
		SwingWorker<Void, TrackInfo> worker = new SwingWorker<Void, TrackInfo>() {

			@Override
			protected Void doInBackground() throws Exception {
				Gson gson = new Gson();
				TrackStreams tStream;
				int downloads = 0;
				while (dm.getDownloadsSize() > 0 && !isCancelled()) {
					TrackInfo t = dm.getTracks().get(0);
					tStream = gson.fromJson(
									getResponse("https://api.soundcloud.com/i1/tracks/"
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
											getResponse("https://api.soundcloud.com/users/"
													+ t.getUserId()
													+ ".json?client_id="
													+ clientID),
											UserInfo.class);
							t.setArtworkURL(uploader.getAvatarURL());
						}
						

						if (mediaPath != null) {
							
							// Open the url connection
							URL website = new URL(mediaPath);
							HttpURLConnection connect = (HttpURLConnection) website.openConnection();
							connect.setRequestMethod("HEAD");
							
							// Get the total length of the file
							long total = connect.getContentLengthLong();
							//downloadPanel.downloadSize += connect.getContentLengthLong();
							ReadableByteChannel rbc = null;
							rbc = Channels.newChannel(website.openStream());
							
							// Format track name to a valid file path name
							String title = t.getTitle();
							String finalPath = fuzzTrackTitle(title, dm.getConfig().getDownloadPath());
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
								dm.updateSongDownloadProgress(prog);
								
								if (isCancelled()) {
									fos.close();
									File f = new File(tempPath);
									f.delete();
									return null;
								}
									

							} while (read > 0);
							
							// Clean up after ourselves
							fos.close();
							
							File f = new File(tempPath);
							
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

							BufferedImage image = downloadArtwork(t.getArtworkURL().replace("-large", "-t500x500"));
							// Write the image bytes to the mp3 tag
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							ImageIO.write(image, "jpg", out);
							out.flush();
							byte[] bytes = out.toByteArray();
							out.close();

							ID3Wrapper newId3Wrapper = new ID3Wrapper(new ID3v1Tag(), new ID3v23Tag());
							newId3Wrapper.setAlbumImage(bytes, "image/jpeg");
							id3v2Tag.setAlbumImage(bytes, 2, "image/jpeg");
							
							// Embed track id for directory scanning
							id3v2Tag.setPaymentUrl("" + t.getId());

							// Save the final file in the download directory
							mp3file.save(finalPath);
							
							// Delete the temporary file
							f.delete();
						}
					dm.trackDownloaded(t);
					}
				}

				return null;
			}

			@Override
			protected void done() {
				dm.onDownloadsFinished();
			}

			@Override
			protected void process(List<TrackInfo> chunks) {
			}
		};

		worker.execute();
		return worker;
	}
	
	private String fuzzTrackTitle(String track, String path) {
		track = track.replaceAll(
				"[<>?*:|/\\\\]", " ");
		track = track.replaceAll("\"", "'");
		String finalPath = path + "/"
				+ track + ".mp3";
		return finalPath;
	}	

	public BufferedImage downloadArtwork(String url) {
		// Download artwork from URL if available
		if (url != null) {
			URL artworkURL;
			try {
				artworkURL = new URL(url);
				BufferedImage image = ImageIO.read(artworkURL.openStream());
				return image;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
