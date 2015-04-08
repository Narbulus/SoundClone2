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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
		
		setLayout(new BorderLayout());

		trackList = new JPanel();
		trackList.setLayout(new GridBagLayout());
		trackList.setBackground(LauncherFrame.COLOR_BLUE_DARKER);

		scrollPane = new JScrollPane(trackList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUI(
				new SimpleScrollbarUI(LauncherFrame.COLOR_SCROLL_TRACK,
						LauncherFrame.COLOR_SCROLL_THUMB));
		scrollPane.getVerticalScrollBar().setPreferredSize(
				new Dimension(10, 10));
		scrollPane.getVerticalScrollBar().setUnitIncrement(12);

		add(scrollPane, BorderLayout.CENTER);
		trackList.add(Box.createHorizontalStrut(294), new GridBagConstraints(0,
				0, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		trackList.add(Box.createGlue(), new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

	}

	public void updateTracks(List<TrackInfo> newTracks) {
		tracks.clear();
		tracks.addAll(newTracks);
		trackList.removeAll();
		trackList.add(Box.createHorizontalStrut(294), new GridBagConstraints(0,
				0, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		int r = 12;
		int g = 94;
		int b = 143;
		
		GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0);
		
		for (TrackInfo t : tracks) {
			final JButton button = new JButton(t.getTitle());
			final TrackInfo info = t;
			button.setContentAreaFilled(false);
			button.setFocusPainted(false);
			button.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
			button.setOpaque(true);
			button.setBackground(LauncherFrame.COLOR_BLUE);
			button.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 16));
			button.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
			button.setPreferredSize(new Dimension(
					button.getPreferredSize().width, 40));
			button.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					parent.selectTrack(info);
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
			trackList.add(button, constraints);
			constraints.gridy++;
		}
		
		trackList.add(Box.createHorizontalStrut(294), constraints);
        constraints.gridy++;

        constraints.weighty = 1.0;
        trackList.add(Box.createGlue(), constraints);
		
		revalidate();
		repaint();

	}
}
