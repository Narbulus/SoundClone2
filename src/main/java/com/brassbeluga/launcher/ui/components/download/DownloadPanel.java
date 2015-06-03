package com.brassbeluga.launcher.ui.components.download;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import net.technicpack.ui.controls.list.SimpleScrollbarUI;

import com.brassbeluga.database.SoundCloneDB;
import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.managers.DownloadAction;
import com.brassbeluga.managers.DownloadManager;
import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.TrackInfo;

@SuppressWarnings("serial")
public class DownloadPanel extends JPanel implements DownloadsObserver {

	public static final int DOWNLOAD_HEIGHT = 200;

	private JPanel infoPanel;
	private JLabel trackIcon;
	private JProgressBar progress;
	private JButton button;
	private JButton browseButton;
	private Border trackBorder;
	private JLabel progressInfo;
	private JLabel overallInfo;
	
	private LabelProgressBar trackProgress;

	private JPanel trackList;
	private JFileChooser browse;

	private JScrollPane scrollPane;

	private JButton openButton;
	
	private SoundCloneDB db;
	private DownloadPanel dp;
	private String macAddr;
	private String ipAddr;
	private String currentUser;
	public long downloadSize;

	private DownloadManager dm;

	public DownloadPanel(SoundCloneDB db, DownloadManager dm) {
		this.db = db;
		this.dm = dm;
		this.dp = this;
		
		try {
			
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(
			                whatismyip.openStream()));

			ipAddr = in.readLine(); //you get the external IP as a String
			
			// Get the MAC address now.
			InetAddress ip = InetAddress.getLocalHost();
	        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	        byte[] mac = network.getHardwareAddress();

	
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < mac.length; i++) {
	            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));        
	        }
	        
	        macAddr = sb.toString();
        
		} catch (Exception e) {
			macAddr = "N/A";
			ipAddr = "N/A";
		}

		initComponents();
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(LauncherFrame.COLOR_CENTRAL_BACK_OPAQUE);
		trackBorder = BorderFactory.createEmptyBorder(4, 8, 4, 8);

		trackList = new JPanel();
		trackList.setLayout(new BoxLayout(trackList, BoxLayout.Y_AXIS));
		trackList.setBackground(LauncherFrame.COLOR_CHARCOAL);

		scrollPane = new JScrollPane(trackList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUI(
				new SimpleScrollbarUI(LauncherFrame.COLOR_SCROLL_TRACK,
						LauncherFrame.COLOR_WHITE_TEXT));
		scrollPane.getVerticalScrollBar().setPreferredSize(
				new Dimension(10, 10));
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);
		add(scrollPane);

		trackList.add(Box.createGlue());

		infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
		infoPanel.setPreferredSize(new Dimension(getPreferredSize().width,
				DOWNLOAD_HEIGHT));
		infoPanel.setBackground(LauncherFrame.COLOR_BLUE_DARKER);

		trackIcon = new JLabel(ResourceManager.getIcon("default_track_small.png"));

		progress = new JProgressBar(0, 100);
		progress.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 34));
		progress.setStringPainted(true);
		progress.setForeground(LauncherFrame.COLOR_GREEN);
		progress.setBackground(Color.white);
		progress.setPreferredSize(new Dimension(400, 26));

		button = new JButton("CANCEL");
		button.setPreferredSize(new Dimension(200, button.getPreferredSize().height));
		button.setText("START");
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		// button.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		button.setOpaque(true);
		button.setBackground(LauncherFrame.COLOR_BLUE);
		button.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 34));
		button.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		button.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				try {
					String buttonText = "";
					if (!dm.downloadInProgress() && dm.getDownloadsSize() > 0) {
						dm.startDownload();
						updateInfo();
						button.setText("CANCEL");
					} else {
						if (dm.downloadInProgress())
							dm.stopDownload();
						button.setText("START");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(LauncherFrame.COLOR_BLUE);
			}
		});
		
		browse = new JFileChooser();
		browse.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		browseButton = new JButton("BROWSE");
		browseButton.setContentAreaFilled(false);
		browseButton.setFocusPainted(false);
		browseButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		// browseButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		browseButton.setOpaque(true);
		browseButton.setBackground(LauncherFrame.COLOR_BLUE);
		browseButton.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 34));
		browseButton.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		browseButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (!dm.downloadInProgress()) {
					browse.showOpenDialog(browseButton);
					setBrowseInfo();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				browseButton.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				browseButton.setBackground(LauncherFrame.COLOR_BLUE);
			}
		});
		
		progressInfo = new JLabel();
		progressInfo.setAlignmentX(CENTER_ALIGNMENT);
		progressInfo.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 22));
		progressInfo.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		
		overallInfo = new JLabel();
		overallInfo.setAlignmentX(CENTER_ALIGNMENT);
		overallInfo.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 22));
		overallInfo.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		Dimension d = overallInfo.getPreferredSize();
		overallInfo.setPreferredSize(new Dimension(220, d.height));
		
		openButton = new JButton("OPEN");
		openButton.setContentAreaFilled(false);
		openButton.setFocusPainted(false);
		openButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		// openButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		openButton.setOpaque(true);
		openButton.setBackground(LauncherFrame.COLOR_BLUE);
		openButton.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 22));
		openButton.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		openButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				try {
					Desktop.getDesktop().open(new File(browse.getCurrentDirectory().getAbsolutePath()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				openButton.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				openButton.setBackground(LauncherFrame.COLOR_BLUE);
			}
		});
		
		JPanel browseInfo = new JPanel();
		browseInfo.setLayout(new BoxLayout(browseInfo, BoxLayout.X_AXIS));
		browseInfo.setOpaque(false);
		browseInfo.add(overallInfo);
		browseInfo.add(Box.createRigidArea(new Dimension(16, 0)));
		browseInfo.add(openButton);
		
		JPanel loadInfo = new JPanel();
		loadInfo.setOpaque(false);
		loadInfo.setLayout(new BoxLayout(loadInfo, BoxLayout.Y_AXIS));
		loadInfo.add(progressInfo);
		loadInfo.add(Box.createRigidArea(new Dimension(0, 16)));
		loadInfo.add(progress);
		loadInfo.add(Box.createRigidArea(new Dimension(0, 16)));
		loadInfo.add(browseInfo);

		infoPanel.add(trackIcon);
		infoPanel.add(Box.createHorizontalGlue());
		infoPanel.add(loadInfo);
		infoPanel.add(Box.createHorizontalGlue());
		infoPanel.add(button);
		infoPanel.add(Box.createRigidArea(new Dimension(16, 0)));
		infoPanel.add(browseButton);
		
		trackProgress = new LabelProgressBar(0, 100, 400);
		trackProgress.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 26));
		trackProgress.setLoadColor(LauncherFrame.COLOR_GREEN);
		trackProgress.setBackground(LauncherFrame.COLOR_GREY_TEXT);
		trackProgress.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		trackProgress.setBorder(trackBorder);
		trackProgress.setOpaque(false);

		add(infoPanel);
		
		progressInfo.setText("No tracks selected for download");
		setBrowseInfo();
	}
	
	private void setBrowseInfo() {
		String downloadPath = browse.getCurrentDirectory().getAbsolutePath();
		if (browse.getSelectedFile() != null) {
			downloadPath = browse.getSelectedFile().getAbsolutePath();
			dm.updateDownloadPath(downloadPath);
		}
		overallInfo.setText(downloadPath);
	}
	
	private void rebuildUI() {
		SwingWorker<String, String> worker = new SwingWorker<String, String>() {
			
			@Override 
			public String doInBackground() {
				
				synchronized(trackList) {
					trackList.removeAll();
					
					for (TrackInfo t : dm.getDownloadedTracks()) {
						JLabel label = new JLabel(t.getTitle());
						label.setBorder(trackBorder);
						label.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 16));
						label.setForeground(LauncherFrame.COLOR_GREY_TEXT);
						trackList.add(label);
					}
					
					// Rebuild the remaining tracks queued for download
					if (dm.getDownloadsSize() > 0) {
						TrackInfo curTrack = dm.getTracks().get(0);
						trackProgress.setText(curTrack.getTitle());
						trackProgress.setProgress(0);
						trackList.add(trackProgress);
						for (int i = 1; i < dm.getTracks().size(); i++) {
							JLabel label = new JLabel(dm.getTracks().get(i).getTitle());
							label.setBorder(trackBorder);
							label.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 16));
							label.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
							trackList.add(label);
						}
					}
					
					
					if (dm.downloadInProgress()) {
						overallInfo.setText(dm.getTracks().get(0).getTitle());
					}else{
						overallInfo.setText(dm.getDownloadPath());
					}
					progressInfo.setText("Downloading track " + (dm.getDownloadedTracks().size() + 1) + " of " + 
							(dm.getDownloadsSize() + dm.getDownloadedTracks().size()));
					
					//trackProgress.scrollRectToVisible(trackProgress.getBounds());
			
					trackList.add(Box.createGlue());
				
				
				updateInfo();
				
				//trackProgress.scrollRectToVisible(trackProgress.getBounds());
		
				trackList.add(Box.createGlue());
				}

				return "Information";
			}
			
			@Override
			public void done() {
				revalidate();
				repaint();
			}
			
		};
		worker.execute();

	}

	public void onDownloadFinished() {

		// Notify remote db of successful download.
		// db.submitDownload(getCurrentUser(), macAddr, ipAddr, downloadSize, dm.getDownloadsSize());

		button.setText("START");
		setBrowseInfo();
		
		dm.removeAllTracks();
			
		progress.setValue(0);
		
		repaint();
	}
	
	private void updateInfo() {
		if (dm.downloadInProgress()) {
			overallInfo.setText(dm.getTracks().get(0).getTitle());
			progressInfo.setText("Downloading track " + (dm.getDownloadedTracks().size() + 1) + " of " + 
					(dm.getDownloadsSize() + dm.getDownloadedTracks().size()));
		}else{
			overallInfo.setText(dm.getDownloadPath());
			if (dm.getDownloadedTracks().size() <= 0)
				progressInfo.setText("Ready to download " + dm.getDownloadsSize() + " tracks");
			else
				progressInfo.setText(dm.getDownloadedTracks().size() + " tracks downloaded!");
		}
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String lastUser) {
		this.currentUser = lastUser;
	}

	@Override
	public void update(DownloadManager dm, DownloadAction action) {
		if (action == DownloadAction.TRACKS_CHANGED) {
			if (dm.getDownloadsSize() > 0) {
				dm.downloadLabelIcon(dm.getTracks().get(0), "-large", trackIcon, 
						ResourceManager.getIcon("default_track_small.png"));
			}
			rebuildUI();
		}else if (action == DownloadAction.SONG_PROGRESS) {
			trackProgress.setProgress(dm.getSongProgress());
			int totalSize = dm.getDownloadsSize() + dm.getDownloadedTracks().size();
            progress.setValue((int)(((dm.getDownloadedTracks().size() * 1.0 + (dm.getSongProgress() / 100.0)) / (totalSize * 1.0)) * 100));;
		}else if (action == DownloadAction.DOWNLOADS_FINISHED) {
			onDownloadFinished();
		}else if (action == DownloadAction.USERNAME_CHANGED) {
			if (dm.getDownloadPath() != null) {
				overallInfo.setText(dm.getDownloadPath());
			}
		}
	}

}
