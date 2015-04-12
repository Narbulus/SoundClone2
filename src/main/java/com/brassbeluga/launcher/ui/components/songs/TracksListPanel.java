package com.brassbeluga.launcher.ui.components.songs;

import java.awt.Dimension;
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
import javax.swing.border.MatteBorder;

import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.list.SimpleScrollbarUI;
import net.technicpack.ui.lang.ResourceLoader;

import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.sound.gson.TrackInfo;

@SuppressWarnings("serial")
public class TracksListPanel extends TintablePanel {

	private ResourceLoader resources;
	private JPanel trackList;
	private JLabel loading;
	private JPanel trackControls;
	private JScrollPane scrollPane;
	private List<TrackInfo> tracks;
	private List<TrackEntry> entries;

	private LauncherFrame parent;
	
	public TracksListPanel(ResourceLoader resources, LauncherFrame parent) {

		this.resources = resources;
		this.parent = parent;

		initComponents();
	}

	private void initComponents() {
		tracks = new ArrayList<TrackInfo>();
		entries = new ArrayList<TrackEntry>();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		setBorder(new MatteBorder(5, 5, 5, 5, LauncherFrame.COLOR_BLUE_DARKER));;

		trackList = new JPanel();
		trackList.setLayout(new BoxLayout(trackList, BoxLayout.Y_AXIS));
		trackList.setBackground(LauncherFrame.COLOR_BLUE_DARKER);

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
		
		loading = new JLabel("Loading user's likes...");
		loading.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 20));
		loading.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		loading.setPreferredSize(new Dimension(
				loading.getPreferredSize().width, 40));
		loading.setAlignmentX(CENTER_ALIGNMENT);
		
		trackControls = new JPanel();
		trackControls.setLayout(new BoxLayout(trackControls, BoxLayout.X_AXIS));
		trackControls.setBorder(new MatteBorder(8, 0, 0, 0, LauncherFrame.COLOR_BLUE_DARKER));
		trackControls.setBackground(LauncherFrame.COLOR_BLUE);
		final JButton selectAll = new JButton("Select All");
		selectAll.setContentAreaFilled(false);
		selectAll.setFocusPainted(false);
		// browseButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		selectAll.setOpaque(true);
		selectAll.setBackground(LauncherFrame.COLOR_BLUE);
		selectAll.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 26));
		selectAll.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		selectAll.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				for (TrackEntry t : entries)
					t.setDownloadFlag(true);
				parent.flagAllForDownload(tracks);
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
		// browseButton.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		deselectAll.setOpaque(true);
		deselectAll.setBackground(LauncherFrame.COLOR_BLUE);
		deselectAll.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 26));
		deselectAll.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		deselectAll.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				for (TrackEntry t : entries)
					t.setDownloadFlag(false);
				parent.unflagAllForDownload();
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
		
		revalidate();
		repaint();
	}
	
	public void selectAll() {
		for (TrackEntry t : entries) {
			
		}
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
			TrackEntry track = new TrackEntry(resources, t, i, parent);
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
	
}
