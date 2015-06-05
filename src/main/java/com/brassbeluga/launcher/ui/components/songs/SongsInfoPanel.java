package com.brassbeluga.launcher.ui.components.songs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.WatermarkTextField;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.Autocomplete;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.managers.DownloadAction;
import com.brassbeluga.managers.DownloadManager;
import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.TrackInfo;

@SuppressWarnings("serial")
public class SongsInfoPanel extends TintablePanel implements DownloadsObserver {
	
	private JPanel userInfo;
	private JPanel trackInfo;
	private JTextField usernameField;
	private JLabel userIcon;
	private JLabel trackName;
	private JLabel trackNameOverflow;
	private JLabel trackArtist;
	private JLabel trackArt;
	
	private TrackInfo selected;

	private LauncherFrame parent;
	private DownloadManager dm;

	public static final int SONGS_INFO_WIDTH = 400;
	public static final int SONGS_INFO_HEIGHT = 140;
	private static final int MAX_TITLE_LENGTH = 42;
	
	private static final String COMMIT_ACTION = "commit";

	public SongsInfoPanel(LauncherFrame parent, DownloadManager dm) {
		this.parent = parent;
		this.dm = dm;

		initComponents();
	}

	private void initComponents() {
		setBackground(LauncherFrame.COLOR_SONGS_INFO);
		setLayout(new BorderLayout());
		setMaximumSize(new Dimension(SONGS_INFO_WIDTH, SONGS_INFO_HEIGHT));

		trackInfo = new JPanel();
		userInfo = new JPanel();

		add(userInfo, BorderLayout.PAGE_START);
		add(trackInfo, BorderLayout.CENTER);

		userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.X_AXIS));
		userInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		userInfo.setPreferredSize(new Dimension(SONGS_INFO_WIDTH,
				SONGS_INFO_HEIGHT));
		userInfo.setBackground(LauncherFrame.COLOR_BLUE_DARKER);

		usernameField = new WatermarkTextField("username",
				LauncherFrame.COLOR_WHITE_TEXT);
		usernameField.setFocusTraversalKeysEnabled(false);
		usernameField.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		usernameField.setFont(ResourceManager
				.getFont(ResourceManager.FONT_RALEWAY, 16));
		usernameField.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		usernameField.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
		usernameField.setCaretColor(LauncherFrame.COLOR_WHITE_TEXT);
		usernameField.setPreferredSize(new Dimension(200, 40));
		// Initialize the new autocomplete document listener
		System.out.println(dm.getConfig().getPreviousUsers().toString());
		Autocomplete autocomplete = new Autocomplete(usernameField, dm.getConfig().getPreviousUsers());
		// Assign it to the username field's document
		usernameField.getDocument().addDocumentListener(autocomplete);
		// Assign the tab keystroke to the complete action
		usernameField.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
		usernameField.getActionMap().put(COMMIT_ACTION, autocomplete.new CommitAction());
		
		usernameField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					dm.updateUser(usernameField.getText());
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

		});

		ImageIcon userDefaultImg = ResourceManager.getIcon("default_user.png");
		userIcon = new JLabel(userDefaultImg);
		userIcon.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

		JPanel usernameContainer = new JPanel();
		usernameContainer.setBorder(BorderFactory.createEmptyBorder(40, 0, 50,
				0));
		usernameContainer.add(usernameField);
		usernameContainer.setOpaque(false);
		userInfo.add(userIcon);
		userInfo.add(usernameContainer);

		trackInfo.setLayout(new BoxLayout(trackInfo, BoxLayout.Y_AXIS));
		// trackInfo.add(Box.createVerticalStrut(SONGS_INFO_HEIGHT));
		trackInfo.setBackground(LauncherFrame.COLOR_BLUE);
		trackInfo.setBorder(BorderFactory.createEmptyBorder(25, 15, 30, 15));
		trackName = new JLabel();
		trackName.setText("Track Name");
		trackName.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		trackName.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 18));
		trackName.setAlignmentX(CENTER_ALIGNMENT);
		Dimension d = new Dimension(SONGS_INFO_WIDTH, trackName.getPreferredSize().height);
		trackName.setPreferredSize(d);
		trackNameOverflow = new JLabel();
		trackNameOverflow.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		trackNameOverflow.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 18));
		trackNameOverflow.setAlignmentX(CENTER_ALIGNMENT);
		trackNameOverflow.setPreferredSize(d);
		
		trackInfo.add(trackName);
		trackInfo.add(trackNameOverflow);
		trackArtist = new JLabel("Artist");
		trackArtist.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		trackArtist.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 16));
		trackArtist.setAlignmentX(CENTER_ALIGNMENT);
		trackInfo.add(Box.createVerticalGlue());
		trackInfo.add(trackArtist);
		trackInfo.add(Box.createVerticalGlue());
		trackArt = new JLabel(ResourceManager.getIcon("default_track.png"));
		trackArt.setBorder(BorderFactory.createEmptyBorder());
		trackArt.setAlignmentX(CENTER_ALIGNMENT);
		trackInfo.add(trackArt);

	}

	public void updateTrack(final TrackInfo track) {
		if (selected != track) {
			selected = track;
			String title = track.getTitle();
			String[] split = title.split("-|~");
			if (split.length > 1) {
				trackArtist.setText(split[0]);
				title = split[1];
			} else {
				trackArtist.setText("");
				title = split[0];
			}
			if (title.length() > MAX_TITLE_LENGTH) {
				int lastSpace = title.lastIndexOf(' ', MAX_TITLE_LENGTH);
				String top = title.substring(0, lastSpace);
				String bottom = title.substring(lastSpace);
				trackName.setText(top);
				trackNameOverflow.setText(bottom);
			}else{
				trackName.setText(title);
				trackNameOverflow.setText("");
			}
			dm.downloadLabelIcon(track, "-t300x300", trackArt, 
					ResourceManager.getIcon("default_track.png"));
		}
	}

	public void setUsername(String username) {
		usernameField.setText(username);
		dm.updateUser(username);
	}

	@Override
	public void update(DownloadManager dm, DownloadAction action) {
		if (action == DownloadAction.USER_ARTWORK_LOADED) {
			dm.downloadLabelIcon(dm.getConfig().getAvatarURL(), userIcon, ResourceManager.getIcon("default_user.png"));
		}
	}
	
}
