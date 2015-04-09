package net.technicpack.launcher.ui.components.songs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.brassbeluga.sound.gson.TrackInfo;
import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.controls.list.SimpleScrollbarUI;
import net.technicpack.ui.lang.ResourceLoader;

import com.google.gson.JsonSyntaxException;

public class DownloadPanel extends JPanel {

	public static final int DOWNLOAD_HEIGHT = 200;

	private ResourceLoader resources;
	private LauncherFrame parent;

	private JPanel infoPanel;
	private JLabel trackIcon;
	private JLabel progress;
	private JButton button;

	private ArrayList<TrackInfo> tracks;

	private JPanel trackList;

	private JScrollPane scrollPane;

	public DownloadPanel(ResourceLoader resources, LauncherFrame parent) {

		this.resources = resources;
		this.parent = parent;

		initComponents();
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(LauncherFrame.COLOR_CENTRAL_BACK_OPAQUE);

		tracks = new ArrayList<TrackInfo>();
		trackList = new JPanel();
		trackList.setLayout(new GridBagLayout());
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

		trackList.add(Box.createGlue(), new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
		infoPanel.setPreferredSize(new Dimension(getPreferredSize().width,
				DOWNLOAD_HEIGHT));
		infoPanel.setBackground(LauncherFrame.COLOR_BLUE_DARKER);

		trackIcon = new JLabel(resources.getIcon("default_track_small.png"));

		progress = new JLabel("No songs queued for download");
		progress.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 24));
		progress.setForeground(LauncherFrame.COLOR_WHITE_TEXT);

		button = new JButton("START");
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		// button.setBorder(new LineBorder(new Color(0, 0, 0, 50)));
		button.setOpaque(true);
		button.setBackground(LauncherFrame.COLOR_BLUE);
		button.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 34));
		button.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		button.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				try {
					button.setText(parent.downloadButtonPressed());
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

		infoPanel.add(trackIcon);
		infoPanel.add(Box.createHorizontalGlue());
		infoPanel.add(progress);
		infoPanel.add(Box.createHorizontalGlue());
		infoPanel.add(button);

		add(infoPanel);
	}
	
	public List<TrackInfo> getTracks() {
		return this.tracks;
	}

	public void addTrack(TrackInfo track) {
		if (!tracks.contains(track)) {
			tracks.add(track);
			rebuildUI();
		}
	}

	public void removeTrack(TrackInfo track) {
		if (tracks.remove(track))
			rebuildUI();
	}

	private void rebuildUI() {
		trackList.removeAll();
		
		if (tracks.size() > 0) {
			TrackInfo t = tracks.get(0);
			if (t.getArtworkURL() != null) {
				Image image = null;
		        try {
		            URL url = new URL(tracks.get(0).getArtworkURL());
		            image = ImageIO.read(url);
		        } catch (IOException e) {
		        	e.printStackTrace();
		        }
		        trackIcon.setIcon(new ImageIcon(image));
			}
		}
		
		if (tracks.size() > 1) {
			progress.setText(tracks.size() + " tracks teady to download");
		} else if (tracks.size() == 1) {
			progress.setText("One track ready to download");
		}else{
			progress.setText("No tracks ready to download");
		}

		GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		int i = 0;
		for (TrackInfo t : tracks) {
			JLabel label = new JLabel(t.getTitle());
			if (i == 0)
				label.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 24));
			else
				label.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 16));
			label.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
			trackList.add(label, constraints);
			i++;
			constraints.gridy++;
		}

		constraints.weighty = 1.0;
		trackList.add(Box.createGlue(), constraints);

		revalidate();
		repaint();

	}
	
	public void setStatus(String status) {
		progress.setText(status);
	}

	public void onDownloadFinished() {
		button.setText("START");
	}

}
