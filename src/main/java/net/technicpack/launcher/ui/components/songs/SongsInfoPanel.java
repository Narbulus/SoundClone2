package net.technicpack.launcher.ui.components.songs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.google.gson.JsonSyntaxException;

import net.brassbeluga.sound.main.DownloadLikes;
import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.WatermarkTextField;
import net.technicpack.ui.controls.borders.RoundBorder;
import net.technicpack.ui.lang.ResourceLoader;

public class SongsInfoPanel extends TintablePanel {
	private ResourceLoader resources;
	private JPanel songsInfoContainer;
	private JPanel userInfo;
	private JPanel trackInfo;
	private JTextField usernameField;
	private JButton userIcon;

	private DownloadLikes downloader;

	public static final int SONGS_INFO_WIDTH = 400;
	public static final int SONGS_INFO_HEIGHT = 180;
	private static final int MAX_SEARCH_STRING = 90;

	public SongsInfoPanel(ResourceLoader resources, DownloadLikes downloader) {

		this.resources = resources;
		this.downloader = downloader;

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
				LauncherFrame.COLOR_BLUE_DARKER);
		usernameField.setBorder(null);
		usernameField.setPreferredSize(new Dimension(150, 30));
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
		usernameContainer.setBorder(BorderFactory.createEmptyBorder(60, 0, 50,
				0));
		usernameContainer.add(usernameField);
		usernameContainer.setOpaque(false);
		userInfo.add(userIcon);
		userInfo.add(usernameContainer);

		trackInfo.setLayout(new BoxLayout(trackInfo, BoxLayout.Y_AXIS));
		trackInfo.add(Box.createVerticalStrut(SONGS_INFO_HEIGHT));
	}

	public void changeIcon(final Image icon) {
				ImageIcon imgIcon = new ImageIcon(icon);
				userIcon.setIcon(imgIcon);
				revalidate();
				repaint();
	}

	protected void detectNameChanges() {
		if (downloader != null && !downloader.isThreadRunning()) {
			String select = usernameField.getText();
			String curUser = downloader.getCurrentUser();
			if (curUser == null
					|| (curUser != null && !downloader.getCurrentUser().equals(
							select))) {
				try {
					downloader.updateUser(select.replace(".", "-"), this);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
