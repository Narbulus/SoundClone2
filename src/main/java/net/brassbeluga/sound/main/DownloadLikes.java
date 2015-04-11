package net.brassbeluga.sound.main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import net.brassbeluga.sound.gson.Configuration;
import net.brassbeluga.sound.gson.RedirectResponse;
import net.brassbeluga.sound.gson.TrackInfo;
import net.brassbeluga.sound.gson.TrackStreams;
import net.brassbeluga.sound.gson.UserInfo;
import net.technicpack.launcher.ui.components.songs.DownloadPanel;
import net.technicpack.launcher.ui.components.songs.SongsInfoPanel;
import net.technicpack.launcher.ui.components.songs.TracksListPanel;
import net.technicpack.ui.lang.ResourceLoader;

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

	private String clientID;
	private int maxDuration;
	private ArrayList<Configuration> configs;
	private ArrayList<TrackInfo> likes;
	private Configuration currentConfig;
	private ID3v2 template;
	private SoundLoader load;
	private Boolean threadRunning;
	private String tempDir;
	private String defaultDownload;

	private static final int TRACK_INFO_REQUEST_SIZE = 50;
	protected static final long CHUNK_SIZE = 100;

	public DownloadLikes(ResourceLoader resources) throws Exception {
		// Create download locations if nonexistant
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();
		if (OS.contains("WIN")) {
			workingDirectory = System.getenv("AppData");
		} else {
			workingDirectory = System.getProperty("user.home");
			workingDirectory += "/Library/Application Support";
		}

		tempDir = workingDirectory + "/SoundClone";

		File tempFile = new File(tempDir);
		tempFile.mkdirs();

		defaultDownload = System.getProperty("user.home");

		threadRunning = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				resources.getResourceAsStream("/config")));
		File oldConfig = new File(tempDir + "/config");
		Scanner config;
		if (oldConfig.exists())
			config = new Scanner(oldConfig);
		else
			config = new Scanner(reader);
		clientID = config.nextLine();
		maxDuration = Integer.parseInt(config.nextLine());

		configs = new ArrayList<Configuration>();
		currentConfig = null;

		// Nothing

		// Load past configurations from config file's json
		while (config.hasNext()) {
			String nextConfig = config.nextLine();
			Configuration newConfig = new Gson().fromJson(nextConfig,
					Configuration.class);
			configs.add(newConfig);
		}

		config.close();

		// Load the template id3 tag from resource file
		InputStream is = resources.getResourceAsStream("/tagdata");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		template = new ID3v22Tag(buffer.toByteArray());

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
	public void updateUser(final String user, final SongsInfoPanel panel,
			final TracksListPanel trackPanel) throws JsonSyntaxException,
			Exception {
		for (Configuration c : configs) {
			if (c.getUsername().equals(user))
				currentConfig = c;
		}

		if (currentConfig == null || user != currentConfig.getUsername()) {
			currentConfig = new Configuration(user, defaultDownload, null);
			configs.add(currentConfig);
		}

		try {
			load = new SoundLoader(currentConfig, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// gui.updateStatus("Loading user's likes");

		// Dispatch worker to download songs in background and update status
		SwingWorker<List<TrackInfo>, List<TrackInfo>> worker = new SwingWorker<List<TrackInfo>, List<TrackInfo>>() {

			@SuppressWarnings("unchecked")
			@Override
			protected List<TrackInfo> doInBackground()
					throws JsonSyntaxException, Exception {
				String redirect = "";
				try {
					redirect = load
							.getResponse("http://api.soundcloud.com/resolve.json?url=http://soundcloud.com/"
									+ user + "&client_id=" + clientID);
				} catch (Exception e) {
					// gui.updateStatus("Username '" + user + "' not found",
					// SoundCloneGUI.StatusType.WARNING);
					// gui.unlockControls();
					threadRunning = false;
					e.printStackTrace();
				}

				RedirectResponse response = new Gson().fromJson(redirect,
						RedirectResponse.class);
				UserInfo info = new Gson().fromJson(
						load.getResponse(response.getLocation()),
						UserInfo.class);

				System.out.println(info.getAvatarURL());
				Image image = null;
				try {
					URL url = new URL(info.getAvatarURL());
					image = ImageIO.read(url);
				} catch (IOException e) {
					e.printStackTrace();
				}

				panel.changeIcon(image);

				Type listType = new TypeToken<ArrayList<TrackInfo>>() {
				}.getType();

				likes = new ArrayList<TrackInfo>();

				for (int i = 0; i < info.getFavoritesCount(); i += TRACK_INFO_REQUEST_SIZE) {
					String partLikes = load
							.getResponse("http://api.soundcloud.com/users/"
									+ info.getId()
									+ "/favorites.json?client_id=" + clientID
									+ "&offset=" + i);
					List<TrackInfo> newLikes = new Gson().fromJson(partLikes,
							listType);
					likes.addAll(newLikes);
					System.out.println("Likes size " + likes.size());
					publish(newLikes);
				}

				return likes;
			}

			@Override
			protected void done() {
				trackPanel.onFinishedLoading();
				threadRunning = false;
			}

			@Override
			protected void process(List<List<TrackInfo>> tracks) {
				trackPanel.addNewTracks(tracks.get(tracks.size() - 1));
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
	public void downloadTracks(String user, final String downloadPath,
			final List<TrackInfo> tracks, final DownloadPanel downloadPanel)
			throws JsonSyntaxException, Exception {
		// If a new path is specified, clear history on config so new files are
		// downloaded
		if (user.equals(currentConfig.getUsername())
				&& !downloadPath.equals(currentConfig.getDownloadPath())) {
			currentConfig.setDownloadPath(downloadPath);
			load.clearHistory();
		}
		// gui.updateStatus("Intializing downloads",
		// SoundCloneGUI.StatusType.PROCESS);
		// Dispatch worker to download songs in background and update status
		SwingWorker<String, TrackInfo> worker = new SwingWorker<String, TrackInfo>() {

			@Override
			protected String doInBackground() throws Exception {
				Gson gson = new Gson();
				TrackStreams tStream;
				Mp3Downloader download = new Mp3Downloader(template,
						currentConfig, tempDir);
				int i = 1;
				int downloads = 0;
				System.out.println("Size : " + tracks.size());
				for (TrackInfo t : tracks) {
					if (threadRunning) {
						publish(t);
						tStream = gson
								.fromJson(
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
								System.out.println("Generate mp3");
								
								// Download the mp3 file
								URL website = new URL(mediaPath);
								HttpURLConnection connect = (HttpURLConnection) website
										.openConnection();
								connect.setRequestMethod("HEAD");
								InputStream urlIn = connect.getInputStream();
								long total = connect.getContentLengthLong();
								System.out.println(total);
								ReadableByteChannel rbc = null;
								rbc = Channels.newChannel(website.openStream());
								String title = t.getTitle();
								String fuzzTitle = t.getTitle();
								fuzzTitle = fuzzTitle.replaceAll(
										"[<>?*:|/\\\\]", " ");
								fuzzTitle = fuzzTitle.replaceAll("\"", "'");
								String tempPath = tempDir + "/" + fuzzTitle
										+ ".mp3";
								String finalPath = downloadPath + "/"
										+ fuzzTitle + ".mp3";
								File finalDir = new File(finalPath);
								finalDir.getParentFile().mkdirs();
								FileOutputStream fos = null;
								try {
									fos = new FileOutputStream(tempPath);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}

								long pos = 0;
								long read;
								do {
									read = fos.getChannel().transferFrom(rbc,
											pos, CHUNK_SIZE);
									pos += read;
									int prog = (int)(((pos * 1.0) / (total * 1.0)) * 100.0);
									setProgress(prog);

								} while (read > 0);

								fos.close();

								File f = new File(tempPath);

								Mp3File mp3file = new Mp3File(tempPath);
								mp3file.setId3v2Tag(template);
								ID3v2 id3v2Tag = mp3file.getId3v2Tag();

								// If the file name has a parseable title and
								// artist,
								// update tag with new info
								if (title.contains(" - ")) {
									String[] halves = title.split(" - ");
									if (halves.length == 2) {
										id3v2Tag.setArtist(halves[0]);
										id3v2Tag.setTitle(halves[1]);
									}
								}

								if (t.getArtworkURL() != null) {
									URL artworkURL = new URL(t
											.getArtworkURL().replace("large",
													"t500x500"));

									BufferedImage image = ImageIO
											.read(artworkURL.openStream());
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

								mp3file.save(finalPath);
								f.delete();
								load.writeToHistory(t.getId());
								// downloadPanel.removeTrack(t);
								downloads++;
							}
						}
						i++;
					}
				}

				// update the current configuration's history
				load.closeHistory();

				// write the configurations to file
				PrintStream output = new PrintStream(tempDir + "/config");

				output.println(clientID);
				output.println(maxDuration);

				for (Configuration c : configs) {
					output.println(gson.toJson(c));
				}

				output.flush();
				output.close();

				return downloads + " songs downloaded successfully!";
			}

			@Override
			protected void done() {
				downloadPanel.onDownloadFinished();
				threadRunning = false;
			}

			@Override
			protected void process(List<TrackInfo> chunks) {
				downloadPanel.setCurrentTrack(chunks.get(chunks.size() - 1));
			}
		};

		threadRunning = true;
		worker.addPropertyChangeListener(downloadPanel);
		worker.execute();
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
		return !downloadPath.equals(currentConfig.getDownloadPath())
				&& load.getHistoryLength() > 0;
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

	public String getDownloadPath() {
		if (currentConfig != null)
			return currentConfig.getDownloadPath();
		return defaultDownload;
	}

	public Boolean isThreadRunning() {
		return threadRunning;
	}

	public void stopThread() {
		threadRunning = false;
	}

	public void toggleDownload(int index) {
		likes.get(index).setDownload(!likes.get(index).getDownload());
	}


}
