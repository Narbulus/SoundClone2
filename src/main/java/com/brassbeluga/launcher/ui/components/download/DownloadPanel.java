package com.brassbeluga.launcher.ui.components.download;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.launcher.ui.components.songs.TrackEntry;
import com.brassbeluga.launcher.ui.controls.SimpleScrollbarUI;
import com.brassbeluga.managers.ConfigurationManager;
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
	
	private LabelProgressBar labelProgress;

	private JPanel trackList;
	private JFileChooser browse;

	private SimpleScrollbarUI scroll;
	private JScrollPane scrollPane;

	private JButton openButton;
	
	private DownloadPanel dp;
	private String macAddr;
	private String ipAddr;
	private String currentUser;
	public long downloadSize;
	
	private Map<TrackInfo, LabelProgressBar> infoEntry;

	private DownloadManager dm;
	private ConfigurationManager config;

	private JLabel pathLabel;

	public DownloadPanel(DownloadManager dm) {
		this.dm = dm;
		this.dp = this;
		this.config = dm.getConfig();
		
		infoEntry = new HashMap<TrackInfo, LabelProgressBar>();

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
		scroll = new SimpleScrollbarUI(LauncherFrame.COLOR_SCROLL_TRACK,
				LauncherFrame.COLOR_WHITE_TEXT);
		scrollPane.getVerticalScrollBar().setUI(scroll);
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
		overallInfo.setText(" ");
		
		openButton = new JButton("OPEN");
		openButton.setContentAreaFilled(false);
		openButton.setFocusPainted(false);
		openButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		// openButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		openButton.setOpaque(true);
		openButton.setBackground(LauncherFrame.COLOR_BLUE);
		openButton.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 34));
		openButton.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		openButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							Desktop.getDesktop().open(new File(dm.getConfig().getDownloadPath()));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
				
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
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setOpaque(false);
		buttonPanel.add(button);
		buttonPanel.add(Box.createRigidArea(new Dimension(16, 0)));
		buttonPanel.add(browseButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(16, 0)));
		buttonPanel.add(openButton);
		
		JPanel loadInfo = new JPanel();
		loadInfo.setOpaque(false);
		loadInfo.setLayout(new BoxLayout(loadInfo, BoxLayout.Y_AXIS));
		loadInfo.add(progressInfo);
		loadInfo.add(Box.createRigidArea(new Dimension(0, 16)));
		loadInfo.add(progress);
		loadInfo.add(Box.createRigidArea(new Dimension(0, 16)));
		loadInfo.add(browseInfo);
		
		JPanel infoButtons = new JPanel();
		infoButtons.add(Box.createRigidArea(new Dimension(0, 42)));
		infoButtons.setLayout(new BoxLayout(infoButtons, BoxLayout.Y_AXIS));
		infoButtons.setOpaque(false);
		infoButtons.add(buttonPanel);
		infoButtons.add(Box.createRigidArea(new Dimension(0, 16)));
		
		pathLabel = new JLabel();
		pathLabel.setAlignmentX(CENTER_ALIGNMENT);
		pathLabel.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 22));
		pathLabel.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		Dimension d2 = overallInfo.getPreferredSize();
		pathLabel.setPreferredSize(new Dimension(220, d2.height));
		
		infoButtons.add(pathLabel);
		pathLabel.setText("Download path here");
			

		infoPanel.add(trackIcon);
		infoPanel.add(Box.createHorizontalGlue());
		infoPanel.add(loadInfo);
		infoPanel.add(Box.createHorizontalGlue());
		infoPanel.add(infoButtons);

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
	}
	
	private void rebuildUI() {
		SwingWorker<String, String> worker = new SwingWorker<String, String>() {
			
			@Override 
			public String doInBackground() {
				
				synchronized(trackList) {
					try {
						
						List<TrackInfo> removeThese = new ArrayList<TrackInfo>();
						// Remove tracks that were removed from the queue
						for (TrackInfo t : infoEntry.keySet()) {
							if (!dm.getTracks().contains(t) && !dm.getDownloadedTracks().contains(t)) {
								removeThese.add(t);
							}
						}
						
						for (TrackInfo t : removeThese) {
							trackList.remove(infoEntry.get(t));
							infoEntry.remove(t);
						}
						
						// If there's a track in the download queue that isn't on our list, make a new row for it
						for (TrackInfo t : dm.getTracks()) {
							if (!infoEntry.containsKey(t)) {
								LabelProgressBar newRow = new LabelProgressBar(0, 100, 400, false);
								newRow.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 16));
								newRow.setLoadColor(LauncherFrame.COLOR_GREEN);
								newRow.setBackground(LauncherFrame.COLOR_GREY_TEXT);
								newRow.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
								newRow.setBorder(trackBorder);
								newRow.setOpaque(false);
								newRow.setText(t.getTitle());
								infoEntry.put(t, newRow);
								trackList.add(newRow, trackList.getComponentCount() - 1);
							} else {
								LabelProgressBar bar = infoEntry.get(t);
								if (bar != null && bar.getForeground() != LauncherFrame.COLOR_WHITE_TEXT) {
									trackList.remove(bar);
									infoEntry.get(t).setForeground(LauncherFrame.COLOR_WHITE_TEXT);
									trackList.add(bar, trackList.getComponentCount() - 1);
								}
							}
						}
						
						// Make all the downloaded tracks grey
						for (TrackInfo t : dm.getDownloadedTracks()) {
							LabelProgressBar bar = infoEntry.get(t);
							if (bar != null) {
								bar.setForeground(LauncherFrame.COLOR_GREY_TEXT);
								bar.setBarVisible(false);
							}
						}	
						
						if (dm.getDownloadsSize() > 0) {
							
							// Make the progress bar visible on the current row
							labelProgress = infoEntry.get(dm.getNextTrack());
							labelProgress.setBarVisible(true);
							
							scroll.updateCurrentPosition(((dm.getDownloadedSize() * 1.0) + 1) / trackList.getComponentCount(), 
									labelProgress.getHeight() / (trackList.getHeight() * 1.0));
							
							overallInfo.setText(dm.getNextTrack().getTitle());
							progressInfo.setText("Downloading track " + (dm.getDownloadedSize() + 1) + " of " + 
									(dm.getDownloadsSize() + dm.getDownloadedSize()));
							
							//LabelProgress.scrollRectToVisible(LabelProgress.getBounds());
						
							updateInfo();
						}
						
						//LabelProgress.scrollRectToVisible(LabelProgress.getBounds());
					} catch (Exception e) {
						e.printStackTrace();
					}
		
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
		
		if (dm.getDownloadsSize() > 0) {
			rebuildUI();
		}else{
			progressInfo.setText(dm.getDownloadedSize() + " tracks successfully downloaded!");
		}
		
		overallInfo.setText(" ");
		progress.setValue(0);
		labelProgress.setProgress(0);
	}
	
	private void updateInfo() {
		overallInfo.setText(dm.getNextTrack().getTitle());
		if (dm.downloadInProgress()) {
			progressInfo.setText("Downloading track " + (dm.getDownloadedSize() + 1) + " of " + 
					(dm.getDownloadsSize() + dm.getDownloadedSize()));
		}else{
			if (dm.getDownloadedSize() <= 0)
				progressInfo.setText("Ready to download " + dm.getDownloadsSize() + " tracks");
			else
				progressInfo.setText(dm.getDownloadedSize() + " tracks downloaded!");
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
				dm.downloadLabelIcon(dm.getNextTrack(), "-large", trackIcon, 
						ResourceManager.getIcon("default_track_small.png"));
			}else{
				trackIcon.setIcon(ResourceManager.getIcon("default_track_small.png"));
			}
			rebuildUI();
		}else if (action == DownloadAction.SONG_PROGRESS) {
			labelProgress.setProgress(dm.getSongProgress());
			//overallInfo.setText(dm.getNextTrack().getTitle() + " " + dm.getSongProgress() + "%");
			int totalSize = dm.getDownloadsSize() + dm.getDownloadedSize();
            progress.setValue((int)(((dm.getDownloadedSize() * 1.0 + (dm.getSongProgress() / 100.0)) / (totalSize * 1.0)) * 100));;
		}else if (action == DownloadAction.DOWNLOADS_FINISHED) {
			onDownloadFinished();
		}else if (action == DownloadAction.USERNAME_CHANGED) {
			if (config.getDownloadPath() != null) {
				pathLabel.setText(config.getDownloadPath());
			}
			browse.setCurrentDirectory(new File(dm.getConfig().getDownloadPath()));
		}else if (action == DownloadAction.DOWNLOAD_PATH_CHANGED) {
			if (config.getDownloadPath() != null) {
				pathLabel.setText(config.getDownloadPath());
			}
		}
	}

}
