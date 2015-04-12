/*
 * This file is part of The Technic Launcher Version 3.
 * Copyright ©2015 Syndicate, LLC
 *
 * The Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Technic Launcher  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.brassbeluga.launcher.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import net.technicpack.autoupdate.IBuildNumber;
import net.technicpack.launchercore.auth.IAuthListener;
import net.technicpack.launchercore.auth.IUserType;
import net.technicpack.launchercore.auth.UserModel;
import net.technicpack.launchercore.image.ImageRepository;
import net.technicpack.launchercore.install.LauncherDirectories;
import net.technicpack.launchercore.launch.java.JavaVersionRepository;
import net.technicpack.launchercore.launch.java.source.FileJavaSource;
import net.technicpack.launchercore.modpacks.ModpackModel;
import net.technicpack.launchercore.modpacks.sources.IInstalledPackRepository;
import net.technicpack.minecraftcore.mojang.auth.MojangUser;
import net.technicpack.platform.IPlatformApi;
import net.technicpack.platform.io.AuthorshipInfo;
import net.technicpack.ui.controls.DraggableFrame;
import net.technicpack.ui.controls.RoundedButton;
import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.installation.ProgressBar;
import net.technicpack.ui.lang.IRelocalizableResource;
import net.technicpack.ui.lang.ResourceLoader;
import net.technicpack.utilslib.DesktopUtils;

import com.brassbeluga.launcher.launch.Installer;
import com.brassbeluga.launcher.settings.StartupParameters;
import com.brassbeluga.launcher.settings.TechnicSettings;
import com.brassbeluga.launcher.ui.components.songs.DownloadPanel;
import com.brassbeluga.launcher.ui.components.songs.SongsInfoPanel;
import com.brassbeluga.launcher.ui.components.songs.TracksListPanel;
import com.brassbeluga.launcher.ui.controls.DownloadHeaderTab;
import com.brassbeluga.launcher.ui.controls.HeaderTab;
import com.brassbeluga.launcher.ui.listeners.TabFlashListener;
import com.brassbeluga.sound.gson.TrackInfo;
import com.brassbeluga.sound.main.DownloadLikes;
import com.google.gson.JsonSyntaxException;

public class LauncherFrame extends DraggableFrame implements IRelocalizableResource{

	private static final int FRAME_WIDTH = 1194;
	private static final int FRAME_HEIGHT = 718;

	/* Different Colors used throughout the ui */
	public static final Color COLOR_RED = new Color(229, 0, 0);
	public static final Color COLOR_GREEN = new Color(90, 184, 96);
	public static final Color COLOR_BLUE = new Color(16, 108, 163);
	public static final Color COLOR_BLUE_ALT = new Color(15, 100, 150);
	public static final Color COLOR_BLUE_DARKER = new Color(12, 94, 145);
	public static final Color COLOR_WHITE_TEXT = new Color(208, 208, 208);
	public static final Color COLOR_CHARCOAL = new Color(31, 31, 31);
	public static final Color COLOR_SCROLL_TRACK = new Color(16, 108, 163);
	public static final Color COLOR_CENTRAL_BACK_OPAQUE = new Color(25, 30, 34);
	public static final Color COLOR_BUTTON_BLUE = new Color(43, 128, 195);
	public static final Color COLOR_GREY_TEXT = new Color(86, 98, 110);
	public static final Color COLOR_FOOTER = new Color(27, 32, 36);
	public static final Color COLOR_SONGS_INFO = new Color(170, 0, 0);
	public static final Color COLOR_TRACKS_LIST = new Color(0, 0, 102);
	
	/* Specifies how long the DOWNLOAD tab should take to flash  */
	public static final int TAB_FLASH_TIME = 200;
	public static final int TAB_FLAST_INTERVAL = 20;

	public static final String TAB_SONGS = "songs";
	public static final String TAB_DOWNLOAD = "download";
	public static final String DOWNLOAD_TRACK_COMMAND = "download_track";

	private ResourceLoader resources;
	private final UserModel<MojangUser> userModel;
	private final ImageRepository<IUserType> skinRepository;
	private final TechnicSettings settings;
	private final ImageRepository<ModpackModel> iconRepo;
	private final ImageRepository<ModpackModel> logoRepo;
	private final ImageRepository<ModpackModel> backgroundRepo;
	private final ImageRepository<AuthorshipInfo> avatarRepo;
	private final Installer installer;
	private final IPlatformApi platformApi;
	private final LauncherDirectories directories;
	private final IInstalledPackRepository packRepo;
	private final StartupParameters params;
	private final JavaVersionRepository javaVersions;
	private final FileJavaSource fileJavaSource;
	private final IBuildNumber buildNumber;

	private HeaderTab songsTab;
	private HeaderTab downloadTab;

	private CardLayout infoLayout;
	private JPanel infoSwap;

	private ProgressBar installProgress;
	private Component installProgressPlaceholder;
	private RoundedButton playButton;
	private TintablePanel centralPanel;
	private TintablePanel footer;

	private TracksListPanel tracksPanel;
	private SongsInfoPanel songsInfoPanel;
	private DownloadPanel downloadPanel;

	private DownloadLikes downloader;

	private String currentTabName;
	private Timer tabFlashTimer;
	private TabFlashListener tabFlashListener;

	public LauncherFrame(final ResourceLoader resources,
			final ImageRepository<IUserType> skinRepository,
			final UserModel userModel, final TechnicSettings settings,
			final ImageRepository<ModpackModel> iconRepo,
			final ImageRepository<ModpackModel> logoRepo,
			final ImageRepository<ModpackModel> backgroundRepo,
			final Installer installer,
			final ImageRepository<AuthorshipInfo> avatarRepo,
			final IPlatformApi platformApi,
			final LauncherDirectories directories,
			final IInstalledPackRepository packRepository,
			final StartupParameters params,
			final JavaVersionRepository javaVersions,
			final FileJavaSource fileJavaSource,
			final IBuildNumber buildNumber, final DownloadLikes downloader) {
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.userModel = userModel;
		this.skinRepository = skinRepository;
		this.settings = settings;
		this.iconRepo = iconRepo;
		this.logoRepo = logoRepo;
		this.backgroundRepo = backgroundRepo;
		this.installer = installer;
		this.avatarRepo = avatarRepo;
		this.platformApi = platformApi;
		this.directories = directories;
		this.packRepo = packRepository;
		this.params = params;
		this.fileJavaSource = fileJavaSource;
		this.javaVersions = javaVersions;
		this.buildNumber = buildNumber;

		this.downloader = downloader;

		// Handles rebuilding the frame, so use it to build the frame in the
		// first place
		relocalize(resources);

		selectTab(TAB_SONGS);

		// Show yee self
		this.setVisible(true);

		setLocationRelativeTo(null);
	}

	// ///////////////////////////////////////////////
	// Action responses
	// ///////////////////////////////////////////////

	public void selectTab(String tabName) {
		songsTab.setIsActive(false);
		downloadTab.setIsActive(false);

		if (tabName.equalsIgnoreCase(TAB_SONGS)) {
			songsTab.setIsActive(true);
		}else if (tabName.equalsIgnoreCase(TAB_DOWNLOAD)) {
			downloadTab.setIsActive(true);
		}

		infoLayout.show(infoSwap, tabName);

		currentTabName = tabName;
	}

	protected void closeWindow() {
		System.exit(0);
	}

	protected void minimizeWindow() {
		this.setState(Frame.ICONIFIED);
	}

	protected void logout() {
		if (installer.isCurrentlyRunning())
			return;

		userModel.setCurrentUser(null);
	}

	public void launchCompleted() {
		if (installer.isCurrentlyRunning()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					launchCompleted();
				}
			});
			return;
		}

		installProgress.setVisible(false);
		installProgressPlaceholder.setVisible(true);

		userModel.setCurrentUser(userModel.getCurrentUser());

		invalidate();
	}

	protected void openLauncherOptions() {
		centralPanel.setTintActive(true);
		footer.setTintActive(true);
		centralPanel.setTintActive(false);
		footer.setTintActive(false);
	}

	// ///////////////////////////////////////////////
	// End Action responses
	// ///////////////////////////////////////////////

	private void initComponents() {
		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		// ///////////////////////////////////////////////////////////
		// HEADER
		// ///////////////////////////////////////////////////////////
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
		header.setBackground(COLOR_BLUE);
		header.setForeground(COLOR_WHITE_TEXT);
		header.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		this.add(header, BorderLayout.PAGE_START);

		ImageIcon headerIcon = resources.getIcon("soundcloud_logo.png");
		JButton headerLabel = new JButton(headerIcon);
		headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 0));
		headerLabel.setContentAreaFilled(false);
		headerLabel.setFocusPainted(false);
		headerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		headerLabel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DesktopUtils.browseUrl("http://www.technicpack.net/");
			}
		});
		header.add(headerLabel);
		
		header.add(Box.createRigidArea(new Dimension(6, 0)));

		ActionListener tabListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectTab(e.getActionCommand());
			}
		};


		songsTab = new HeaderTab("SONGS", resources);
		songsTab.addActionListener(tabListener);
		songsTab.setActionCommand(TAB_SONGS);
		header.add(songsTab);
		
		downloadTab = new DownloadHeaderTab("DOWNLOAD", resources);
		downloadTab.addActionListener(tabListener);
		downloadTab.setActionCommand(TAB_DOWNLOAD);
		tabFlashListener = new TabFlashListener(COLOR_BLUE, downloadTab, this);
		downloadTab.addActionListener(tabFlashListener);
		tabFlashTimer = new Timer(TAB_FLAST_INTERVAL, tabFlashListener);
		tabFlashTimer.setActionCommand(DOWNLOAD_TRACK_COMMAND);
		header.add(downloadTab);

		/*
		JLabel flag = new JLabel(resources.getIcon("track_idle.png"));
		flag.setOpaque(true);
		flag.setVisible(true);
		flag.set
		
		flag.setBounds(50, 50,50, 50);*/
		
		/*
		FlyerGlassPane fg = new FlyerGlassPane(resources, this);
		setGlassPane(fg);
		
		fg.setVisible(true);
		fg.setOpaque(false);
		
		new Timer(DownloadFlyer.FLIGHT_INTERVAL, (ActionListener) fg).start();
		*/
		
		
		
		header.add(Box.createHorizontalGlue());

		JPanel rightHeaderPanel = new JPanel();
		rightHeaderPanel.setOpaque(false);
		rightHeaderPanel.setLayout(new BoxLayout(rightHeaderPanel,
				BoxLayout.PAGE_AXIS));
		rightHeaderPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		JPanel windowGadgetPanel = new JPanel();
		windowGadgetPanel.setOpaque(false);
		windowGadgetPanel.setLayout(new BoxLayout(windowGadgetPanel,
				BoxLayout.LINE_AXIS));
		windowGadgetPanel.setAlignmentX(RIGHT_ALIGNMENT);

		ImageIcon minimizeIcon = resources.getIcon("minimize.png");
		JButton minimizeButton = new JButton(minimizeIcon);
		minimizeButton.setBorder(BorderFactory.createEmptyBorder());
		minimizeButton.setContentAreaFilled(false);
		minimizeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		minimizeButton.setFocusable(false);
		minimizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				minimizeWindow();
			}
		});
		windowGadgetPanel.add(minimizeButton);

		ImageIcon closeIcon = resources.getIcon("close.png");
		JButton closeButton = new JButton(closeIcon);
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeWindow();
			}
		});
		closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		closeButton.setFocusable(false);
		windowGadgetPanel.add(closeButton);

		rightHeaderPanel.add(windowGadgetPanel);
		rightHeaderPanel.add(Box.createVerticalGlue());

		JButton launcherOptionsLabel = new JButton(
				resources.getString("launcher.title.options"));
		launcherOptionsLabel.setIcon(resources.getIcon("options_cog.png"));
		launcherOptionsLabel.setFont(resources.getFont(
				ResourceLoader.FONT_RALEWAY, 14));
		launcherOptionsLabel.setForeground(COLOR_WHITE_TEXT);
		launcherOptionsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		launcherOptionsLabel.setHorizontalTextPosition(SwingConstants.LEADING);
		launcherOptionsLabel.setAlignmentX(RIGHT_ALIGNMENT);
		launcherOptionsLabel.setCursor(Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));
		launcherOptionsLabel.setBorder(BorderFactory.createEmptyBorder());
		launcherOptionsLabel.setContentAreaFilled(false);
		launcherOptionsLabel.setFocusPainted(false);
		launcherOptionsLabel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openLauncherOptions();
			}
		});
		rightHeaderPanel.add(launcherOptionsLabel);

		header.add(rightHeaderPanel);

		// ///////////////////////////////////////////////////////////
		// CENTRAL AREA
		// ///////////////////////////////////////////////////////////
		centralPanel = new TintablePanel();
		centralPanel.setBackground(COLOR_CHARCOAL);
		centralPanel.setForeground(COLOR_WHITE_TEXT);
		this.add(centralPanel, BorderLayout.CENTER);
		centralPanel.setLayout(new BorderLayout());

		infoSwap = new JPanel();
		infoLayout = new CardLayout();
		infoSwap.setLayout(infoLayout);
		infoSwap.setOpaque(false);

		JPanel newsHost = new JPanel();
		infoSwap.add(newsHost, "news");
		JPanel modpackHost = new JPanel();
		infoSwap.add(modpackHost, "modpacks");
		centralPanel.add(infoSwap, BorderLayout.CENTER);

		JPanel songsHost = new JPanel();
		tracksPanel = new TracksListPanel(resources, this);
		songsInfoPanel = new SongsInfoPanel(resources, this);
		infoSwap.add(songsHost, TAB_SONGS);

		songsHost.setLayout(new BorderLayout());
		songsHost.add(tracksPanel, BorderLayout.CENTER);
		songsHost.add(songsInfoPanel, BorderLayout.WEST);
		
		JPanel downloadHost = new JPanel();
		downloadHost.setBackground(COLOR_CENTRAL_BACK_OPAQUE);
		downloadPanel = new DownloadPanel(resources, this);
		
		infoSwap.add(downloadPanel, TAB_DOWNLOAD);

		footer = new TintablePanel();
		footer.setBackground(COLOR_FOOTER);
		footer.setLayout(new BoxLayout(footer, BoxLayout.LINE_AXIS));
		footer.setForeground(COLOR_WHITE_TEXT);
		footer.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 12));

		installProgress = new ProgressBar();
		installProgress.setForeground(Color.white);
		installProgress.setBackground(LauncherFrame.COLOR_GREEN);
		installProgress
				.setBorder(BorderFactory.createEmptyBorder(5, 45, 4, 45));
		installProgress.setIcon(resources.getIcon("download_icon.png"));
		installProgress.setFont(resources.getFont(ResourceLoader.FONT_OPENSANS,
				12));
		installProgress.setVisible(false);
		footer.add(installProgress);

		installProgressPlaceholder = Box.createHorizontalGlue();
		footer.add(installProgressPlaceholder);

		String[] names = { "TuneZip", "Zipmeister", "Zip Zop", "SoundZip",
				"iZip", "ZipCloud", "ZipMan", "Zorp", "Zoop", "Zoodily",
				"Zippster", "ZippMe", "SipZip", "Music Downloader" };
		Random r = new Random();
		JLabel buildCtrl = new JLabel(names[r.nextInt(names.length)]);
		buildCtrl.setForeground(COLOR_WHITE_TEXT);
		buildCtrl.setFont(resources.getFont(ResourceLoader.FONT_OPENSANS, 20));
		buildCtrl.setHorizontalTextPosition(SwingConstants.RIGHT);
		buildCtrl.setHorizontalAlignment(SwingConstants.RIGHT);
		footer.add(buildCtrl);

		this.add(footer, BorderLayout.PAGE_END);

	}

	public void onUserChanged(String user) {
		if (downloader != null && !downloader.isThreadRunning()) {
			String select = user;
			String curUser = downloader.getCurrentUser();
			if (curUser == null
					|| (curUser != null && !downloader.getCurrentUser().equals(
							select))) {
				try {
					downloader.updateUser(select.replace(".", "-"),
							songsInfoPanel, tracksPanel);
					tracksPanel.startUpdateTracks();
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void selectTrack(TrackInfo track) {
		songsInfoPanel.updateTrack(track);
	}
	
	public void flagTrackForDownload(TrackInfo track) {
		downloadPanel.addTrack(track);
		
		((DownloadHeaderTab) downloadTab).incDownloads();
		beginDownloadTabFlash();
	}
	
	public void unFlagTrackForDownload(TrackInfo track) {
		downloadPanel.removeTrack(track);
		((DownloadHeaderTab) downloadTab).decDownloads();
		endDownloadTabFlash();
	}
	
	public String downloadButtonPressed(String path) throws JsonSyntaxException, Exception {
		if (!downloader.isThreadRunning()) {
			downloader.downloadTracks("narbulus", path, downloadPanel.getTracks(), downloadPanel);
			downloadPanel.updateInfo();
			return "CANCEL";
		}else{
			return "START";
		}
	}
	
	public void onDownloadFinished() {
		downloadPanel.onDownloadFinished();
	}
	
	public void onFlyerArrival() {
		((DownloadHeaderTab) downloadTab).incDownloads();
		beginDownloadTabFlash();	
	}
	
	public void beginDownloadTabFlash() {
		downloadTab.setOpaque(true);
		tabFlashListener.reset();
		tabFlashTimer.start();
	}
	
	public void endDownloadTabFlash() {
		downloadTab.setOpaque(false);
		downloadTab.setBackground(COLOR_BLUE_DARKER);
		tabFlashTimer.stop();
	}

	@Override
	public void relocalize(ResourceLoader loader) {
		this.resources = loader;
		this.resources.registerResource(this);

		setIconImage(this.resources.getImage("icon.png"));

		// Wipe controls
		this.getContentPane().removeAll();
		this.setLayout(null);

		// Clear references to existing controls

		initComponents();

		if (currentTabName != null)
			selectTab(currentTabName);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				invalidate();
				repaint();
			}
		});
	}
	
	public void onTrackDownloaded() {
		((DownloadHeaderTab)downloadTab).decDownloads();
	}

	public void setupPlayButtonText(ModpackModel modpack, MojangUser user) {
		playButton.setEnabled(true);
		playButton.setForeground(LauncherFrame.COLOR_BUTTON_BLUE);

		if (installer.isCurrentlyRunning()) {
			playButton.setText(resources.getString("launcher.pack.cancel"));
		} else if (modpack.getInstalledVersion() != null) {
			if (userModel.getCurrentUser() == null
					|| userModel.getCurrentUser().isOffline()) {
				playButton.setText(resources
						.getString("launcher.pack.launch.offline"));
			} else {
				playButton.setText(resources.getString("launcher.pack.launch"));
			}
			playButton.setIcon(new ImageIcon(resources.colorImage(
					resources.getImage("play_button.png"),
					LauncherFrame.COLOR_BUTTON_BLUE)));
			playButton.setHoverIcon(new ImageIcon(resources.colorImage(
					resources.getImage("play_button.png"),
					LauncherFrame.COLOR_BLUE)));
		} else {
			if (userModel.getCurrentUser() == null
					|| userModel.getCurrentUser().isOffline()) {
				playButton.setEnabled(false);
				playButton.setForeground(LauncherFrame.COLOR_GREY_TEXT);
				playButton.setText(resources
						.getString("launcher.pack.cannotinstall"));
			} else {
				playButton
						.setText(resources.getString("launcher.pack.install"));
			}
			playButton.setIcon(new ImageIcon(resources.colorImage(
					resources.getImage("download_button.png"),
					LauncherFrame.COLOR_BUTTON_BLUE)));
			playButton.setHoverIcon(new ImageIcon(resources.colorImage(
					resources.getImage("download_button.png"),
					LauncherFrame.COLOR_BLUE)));
		}
	}
	
	public Point getAbsolutePosition(Component component) {
		int x = 0;
		int y = 0;
		do { 
			x += component.getX();
			y += component.getY();
			component = component.getParent();
		}
		while (component != this);
		
		return new Point(x,y);
	}
}
