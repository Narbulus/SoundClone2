package com.brassbeluga.launcher.ui.components.songs;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;

import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.list.SimpleScrollbarUI;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.managers.DownloadAction;
import com.brassbeluga.managers.DownloadManager;
import com.brassbeluga.observer.DownloadsObserver;
import com.brassbeluga.sound.gson.TrackInfo;

@SuppressWarnings("serial")
public class TracksListPanel extends TintablePanel implements DownloadsObserver{

	private JPanel trackList;
	private JLabel loading;
	private JPanel trackControls;
	private JScrollPane scrollPane;
	private List<TrackInfo> tracks;
	private List<TrackEntry> entries;

	private LauncherFrame parent;
	private DownloadManager dm;

	public TracksListPanel(LauncherFrame parent, DownloadManager dm) {
		this.parent = parent;
		this.dm = dm;

		initComponents();
	}

	private void initComponents() {
		tracks = new ArrayList<TrackInfo>();
		entries = new ArrayList<TrackEntry>();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// setBorder(new MatteBorder(5, 5, 5, 5,
		// LauncherFrame.COLOR_BLUE_DARKER));;

		trackList = new JPanel();
		trackList.setLayout(new BoxLayout(trackList, BoxLayout.Y_AXIS));
		trackList.setBackground(LauncherFrame.COLOR_BLUE_DARKER);

		scrollPane = new JScrollPane(trackList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createMatteBorder(0, 8, 0, 0,
				LauncherFrame.COLOR_BLUE_DARKER));
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUI(
				new SimpleScrollbarUI(LauncherFrame.COLOR_SCROLL_TRACK,
						LauncherFrame.COLOR_WHITE_TEXT));
		scrollPane.getVerticalScrollBar().setPreferredSize(
				new Dimension(10, 10));
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);

		loading = new JLabel("Loading user's likes...");
		loading.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY,
				20));
		loading.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		loading.setPreferredSize(new Dimension(
				loading.getPreferredSize().width, 40));
		loading.setAlignmentX(CENTER_ALIGNMENT);

		trackControls = new JPanel();
		trackControls.setLayout(new BoxLayout(trackControls, BoxLayout.X_AXIS));
		trackControls.setBorder(new MatteBorder(8, 0, 0, 8,
				LauncherFrame.COLOR_BLUE_DARKER));
		trackControls.setBackground(LauncherFrame.COLOR_BLUE);
		final JButton selectAll = new JButton("Select All");
		selectAll.setContentAreaFilled(false);
		selectAll.setFocusPainted(false);
		selectAll.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		// browseButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		selectAll.setOpaque(true);
		selectAll.setBackground(LauncherFrame.COLOR_BLUE);
		selectAll.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY,
				26));
		selectAll.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		selectAll.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			} 

			@Override
			public void mousePressed(MouseEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						List<TrackInfo> selectTracks = new ArrayList<TrackInfo>();
						for (TrackEntry t : entries) {
							if (!t.getInfo().getDownload()) {
								selectTracks.add(t.getInfo());
								t.setDownloadFlag(true);
							}
						}
						
						dm.removeAllTracks();
						dm.addAllTracks(selectTracks);
					}
				});

			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				selectAll.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				selectAll.setBackground(LauncherFrame.COLOR_BLUE);
			}
		});
		final JButton deselectAll = new JButton("Deselect All");
		deselectAll.setContentAreaFilled(false);
		deselectAll.setFocusPainted(false);
		deselectAll.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		// browseButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		deselectAll.setOpaque(true);
		deselectAll.setBackground(LauncherFrame.COLOR_BLUE);
		deselectAll.setFont(ResourceManager.getFont(
				ResourceManager.FONT_RALEWAY, 26));
		deselectAll.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		deselectAll.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (TrackEntry t : entries) {
							t.setDownloadFlag(false);
						}
						dm.removeAllTracks();
					}
					
				});	
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				deselectAll.setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				deselectAll.setBackground(LauncherFrame.COLOR_BLUE);
			}
		});
		trackControls.add(Box.createHorizontalGlue());
		trackControls.add(selectAll);
		trackControls.add(Box.createRigidArea(new Dimension(16, 0)));
		trackControls.add(deselectAll);

		add(scrollPane);
		add(trackControls);

	}

	// Called before tracks are loaded
	public void startUpdateTracks() {
		trackList.removeAll();
		tracks.clear();
		entries.clear();

		trackList.add(loading);
		trackList.add(Box.createGlue());

		repaint();
	}

	public void addNewTracks(List<TrackInfo> newTracks) {
		if (trackList.getComponentCount() > 0)
			// Remove glue
			trackList.remove(trackList.getComponentCount() - 1);

		tracks.addAll(newTracks);

		int i = tracks.size();
		for (TrackInfo t : newTracks) {
			if (i == newTracks.size())
				parent.selectTrack(t);
			TrackEntry track = new TrackEntry(t, i, parent, dm);
			trackList.add(track);
			entries.add(track);
			i++;
		}

		trackList.add(Box.createGlue());

		repaint();
		revalidate();
	}

	public void onFinishedLoading() {
		trackList.remove(0);

		revalidate();
		repaint();
	}

	@Override
	public void update(DownloadManager dm, DownloadAction action) {
		switch (action) {
			case TRACKS_CHANGED:
				List<TrackInfo> infos = dm.getTracks();
				for (TrackEntry entry : entries) {
					if (infos.contains(entry.getInfo())) {
						entry.setDownloadFlag(true);
					} else {
						entry.setDownloadFlag(false);
					}
				}
				break;
			case LIKES_CHANGED:
				List<TrackInfo> newLikes = new ArrayList<TrackInfo>();
				for (TrackInfo t : dm.getLikes()) {
					if (!tracks.contains(t))
						newLikes.add(t);
				}
				addNewTracks(newLikes);
				break;
			case LIKES_CLEARED:
				startUpdateTracks();
				break;
			case LIKES_FINISHED:
				trackList.remove(0);
				repaint();
				revalidate();
				break;
			default:
				break;
		}
	}

}
