package net.technicpack.launcher.ui.components.songs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.brassbeluga.sound.gson.TrackInfo;
import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.controls.TintablePanel;
import net.technicpack.ui.controls.list.SimpleScrollbarUI;
import net.technicpack.ui.lang.ResourceLoader;

@SuppressWarnings("serial")
public class TracksListPanel extends TintablePanel {

	private ResourceLoader resources;
	private JPanel trackList;
	private JLabel loading;
	private JScrollPane scrollPane;
	private List<TrackInfo> tracks;

	private LauncherFrame parent;
	
	public TracksListPanel(ResourceLoader resources, LauncherFrame parent) {

		this.resources = resources;
		this.parent = parent;

		initComponents();
	}

	private void initComponents() {
		tracks = new ArrayList<TrackInfo>();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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

		add(scrollPane);

	}
	
	// Called before tracks are loaded
	public void startUpdateTracks() {
		trackList.removeAll();		
		
		trackList.add(loading);
        trackList.add(Box.createGlue());
		
		revalidate();
		repaint();
	}

	public void addNewTracks(List<TrackInfo> newTracks) {
		if (trackList.getComponentCount() > 0)
			// Remove glue
			trackList.remove(trackList.getComponentCount() - 1);

		tracks.addAll(newTracks);
		
		int i = tracks.size();
		for (TrackInfo t : newTracks) {
			if (i == 0)
				parent.selectTrack(t);
			TrackEntry track = new TrackEntry(resources, t, i, parent);
			trackList.add(track);
			i++;
		}

        trackList.add(Box.createGlue());
		
		revalidate();
		repaint();
	}
	
	public void onFinishedLoading() {
		trackList.remove(0);
		
		revalidate();
		repaint();
	}
	
}
