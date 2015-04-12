package com.brassbeluga.launcher.ui.components.songs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.sound.gson.TrackInfo;

import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.WatermarkTextField;
import net.technicpack.ui.lang.ResourceLoader;

public class SongsInfoPanel extends TintablePanel {
	
	private ResourceLoader resources;
	private JPanel songsInfoContainer;
	private JPanel userInfo;
	private JPanel trackInfo;
	private JTextField usernameField;
	private JButton userIcon;
	private JLabel trackName;
	private JLabel trackNameOverflow;
	private JLabel trackArtist;
	private JButton trackArt;

	private LauncherFrame parent;

	public static final int SONGS_INFO_WIDTH = 400;
	public static final int SONGS_INFO_HEIGHT = 140;
	private static final int MAX_SEARCH_STRING = 90;
	private static final int MAX_TITLE_LENGTH = 42;

	public SongsInfoPanel(ResourceLoader resources, LauncherFrame parent) {

		this.resources = resources;
		this.parent = parent;

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
		usernameField.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		usernameField.setFont(resources
				.getFont(ResourceLoader.FONT_RALEWAY, 16));
		usernameField.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		usernameField.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
		usernameField.setCaretColor(LauncherFrame.COLOR_WHITE_TEXT);
		usernameField.setPreferredSize(new Dimension(200, 40));
		((AbstractDocument) usernameField.getDocument())
				.setDocumentFilter(new DocumentFilter() {
					@Override
					public void insertString(DocumentFilter.FilterBypass fb,
							int offset, String string, AttributeSet attr)
							throws BadLocationException {
						if (fb.getDocument().getLength() + string.length() <= MAX_SEARCH_STRING) {
							fb.insertString(offset, string, attr);
						}
					}

					@Override
					public void remove(DocumentFilter.FilterBypass fb,
							int offset, int length) throws BadLocationException {
						fb.remove(offset, length);
					}

					@Override
					public void replace(DocumentFilter.FilterBypass fb,
							int offset, int length, String text,
							AttributeSet attrs) throws BadLocationException {
						int finalTextLength = (fb.getDocument().getLength() - length)
								+ text.length();
						if (finalTextLength > MAX_SEARCH_STRING)
							text = text.substring(0, text.length()
									- (finalTextLength - MAX_SEARCH_STRING));
						fb.replace(offset, length, text, attrs);
					}
				});
		usernameField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					detectNameChanges();
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

		});

		ImageIcon userDefaultImg = resources.getIcon("default_user.png");
		userIcon = new JButton(userDefaultImg);
		userIcon.setContentAreaFilled(false);
		userIcon.setFocusPainted(false);

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
		trackName.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 18));
		trackName.setAlignmentX(CENTER_ALIGNMENT);
		Dimension d = new Dimension(SONGS_INFO_WIDTH, trackName.getPreferredSize().height);
		trackName.setPreferredSize(d);
		trackNameOverflow = new JLabel();
		trackNameOverflow.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		trackNameOverflow.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 18));
		trackNameOverflow.setAlignmentX(CENTER_ALIGNMENT);
		trackNameOverflow.setPreferredSize(d);
		
		trackInfo.add(trackName);
		trackInfo.add(trackNameOverflow);
		trackArtist = new JLabel("Artist");
		trackArtist.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		trackArtist.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 16));
		trackArtist.setAlignmentX(CENTER_ALIGNMENT);
		trackInfo.add(Box.createVerticalGlue());
		trackInfo.add(trackArtist);
		trackInfo.add(Box.createVerticalGlue());
		trackArt = new JButton(resources.getIcon("default_track.png"));
		trackArt.setAlignmentX(CENTER_ALIGNMENT);
		trackArt.setContentAreaFilled(false);
		trackArt.setFocusPainted(false);
		trackInfo.add(trackArt);

	}

	public void updateTrack(final TrackInfo track) {
		String title = track.getTitle();
		String[] split = title.split(" - ");
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
		SwingWorker<String, String> worker = new SwingWorker<String, String>() {

			@Override
			protected String doInBackground() {
				Image image = null;
				try {
					URL url = new URL(track.getArtworkURL().replace("-large",
							"-t300x300"));
					image = ImageIO.read(url);
				} catch (IOException e) {
					e.printStackTrace();
				}
				trackArt.setIcon(new ImageIcon(image));
				return "";
			}

			@Override
			protected void done() {
				revalidate();
				repaint();
			}
		};
		worker.execute();
	}

	public void changeIcon(final Image icon) {
		ImageIcon imgIcon = new ImageIcon(icon);
		userIcon.setIcon(imgIcon);
		revalidate();
		repaint();
	}

	protected void detectNameChanges() {
		parent.onUserChanged(usernameField.getText());
	}
}
