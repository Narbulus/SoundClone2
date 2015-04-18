package com.brassbeluga.launcher.ui.components.download;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.sound.gson.TrackInfo;

public class DownloadPanel extends JPanel implements PropertyChangeListener {


	public static final int DOWNLOAD_HEIGHT = 200;

	private LauncherFrame parent;

	private JPanel infoPanel;
	private JLabel trackIcon;
	private JProgressBar progress;
	private JButton button;
	private JButton browseButton;
	private Border trackBorder;
	private JLabel progressInfo;
	private JLabel overallInfo;
	
	private LabelProgressBar trackProgress;
	
	private int downloadIndex;

	private ArrayList<TrackInfo> tracks;

	private JPanel trackList;
	private JFileChooser browse;

	private JScrollPane scrollPane;

	public DownloadPanel(LauncherFrame parent) {
		this.parent = parent;
		downloadIndex = 0;

		initComponents();
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(LauncherFrame.COLOR_CENTRAL_BACK_OPAQUE);
		trackBorder = BorderFactory.createEmptyBorder(4, 8, 4, 8);

		tracks = new ArrayList<TrackInfo>();
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
					String path = browse.getCurrentDirectory().getAbsolutePath();
					if (browse.getSelectedFile() != null)
						path = browse.getSelectedFile().getAbsolutePath();
					button.setText(parent.downloadButtonPressed(path));
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
				browse.showOpenDialog(browseButton);
				setBrowseInfo();
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
		
		JPanel loadInfo = new JPanel();
		loadInfo.setOpaque(false);
		loadInfo.setLayout(new BoxLayout(loadInfo, BoxLayout.Y_AXIS));
		loadInfo.add(progressInfo);
		loadInfo.add(Box.createRigidArea(new Dimension(0, 16)));
		loadInfo.add(progress);
		loadInfo.add(Box.createRigidArea(new Dimension(0, 16)));
		loadInfo.add(overallInfo);

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
	
	public void setProgressInfo(String info) {
		progressInfo.setText(info);
	}
	
	public void setOverallInfo(String info) {
		overallInfo.setText(info);
	}
	
	public List<TrackInfo> getTracks() {
		return this.tracks;
	}

	public void addTrack(TrackInfo track) {
		if (!tracks.contains(track)) {
			tracks.add(track);
			rebuildUI();
		}
		progressInfo.setText(tracks.size() + " tracks ready to download");
	}
	
	public void addAllTracks(List<TrackInfo> newTracks) {
		tracks.clear();
		tracks.addAll(newTracks);
		rebuildUI();
		progressInfo.setText(tracks.size() + " tracks ready to download");
	}
	
	public void removeAllTracks() {
		tracks.clear();
		rebuildUI();
		progressInfo.setText("No tracks selected for download");
	}

	public void removeTrack(TrackInfo track) {
		if (tracks.remove(track))
			rebuildUI();
		progressInfo.setText(tracks.size() + " tracks ready to download");
	}
	
	public void setCurrentTrack(TrackInfo track) {
		if (tracks.size() > 0) {
			if (track.getId() != tracks.get(downloadIndex).getId()) {
				downloadIndex++;
				trackList.getComponent(downloadIndex).setBackground(LauncherFrame.COLOR_GREY_TEXT);
				updateInfo();
				parent.onTrackDownloaded();
				rebuildUI();
			}
		}
	}
	
	private void setBrowseInfo() {
		String downloadPath = browse.getCurrentDirectory().getAbsolutePath();
		if (browse.getSelectedFile() != null)
			downloadPath = browse.getSelectedFile().getAbsolutePath();
		overallInfo.setText(downloadPath);
	}
	
	public void updateInfo() {
		overallInfo.setText(tracks.get(downloadIndex).getTitle());
		progressInfo.setText("Downloading track " + (downloadIndex + 1) + " of " + tracks.size());
	}

	private void rebuildUI() {
		SwingWorker worker = new SwingWorker<String, String>() {
			
			@Override 
			public String doInBackground() {
				trackList.removeAll();
				
				if (tracks.size() > 0) {
					TrackInfo t = tracks.get(downloadIndex);
					if (t.getArtworkURL() != null) {
						Image image = null;
				        try {
				            URL url = new URL(t.getArtworkURL());
				            image = ImageIO.read(url);
				        } catch (IOException e) {
				        	e.printStackTrace();
				        }
				        trackIcon.setIcon(new ImageIcon(image));
					}
				}
		
				int i = 0;
				for (TrackInfo t : tracks) {
					JLabel label;
					if (i == downloadIndex) {
						System.out.println("Load bar on " + i + " & " + t.getTitle());
						label = trackProgress;
						trackProgress.setProgress(0);
						trackProgress.setText(t.getTitle());
					}else{
						label = new JLabel(t.getTitle());
						label.setBorder(trackBorder);
						label.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 16));
						if (i > downloadIndex)
							label.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
						else
							label.setForeground(LauncherFrame.COLOR_GREY_TEXT);
					}
					trackList.add(label);
					i++;
				}
				
				//trackProgress.scrollRectToVisible(trackProgress.getBounds());
		
				trackList.add(Box.createGlue());
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
		parent.onTrackDownloaded();
		downloadIndex = 0;
		if (downloadIndex > 0)
			progressInfo.setText((downloadIndex + 1) + " tracks successfully downloaded!");
		else
			progressInfo.setText("No tracks selected for download");
		setBrowseInfo();
			
		button.setText("START");
		tracks.clear();
		trackList.removeAll();
		progress.setValue(0);
		
		revalidate();
		repaint();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
            int progressAmt = (Integer) evt.getNewValue();
            trackProgress.setProgress(progressAmt);
            progress.setValue((int) (downloadIndex / (tracks.size() * 1.0) * 100 + (progressAmt / (tracks.size() * 1.0))));
        } 
	}

}
