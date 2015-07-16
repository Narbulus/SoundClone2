package com.brassbeluga.launcher.ui.components.songs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.managers.DownloadManager;
import com.brassbeluga.sound.gson.TrackInfo;

public class TrackEntry extends JPanel {
	public static final int DOWNLOAD_ICON_WIDTH = 120;
	private static final int TRACK_TITLE_WIDTH = 600;
	
	private TrackInfo info;
	private boolean downloadFlag;
	
	private TracksListPanel parent;
	private Color backColor;
	private Color altColor;
	
	private JLabel title;
	private JButton flag;
	private JLabel warning;
	private int index;
	private DownloadManager dm;
	
	public TrackEntry(TrackInfo info, int index, TracksListPanel parent, DownloadManager dm) {
		this.info = info;
		this.downloadFlag = false;
		this.index = index;
		this.parent = parent;
		this.dm = dm;
		
		if (index % 2 == 0) {
			backColor = LauncherFrame.COLOR_BLUE_ALT;
			altColor = LauncherFrame.COLOR_BLUE;
		}else{
			backColor = LauncherFrame.COLOR_BLUE;
			altColor = LauncherFrame.COLOR_BLUE_ALT;
		}
		
		initComponents();
	}
	
	public TrackInfo getInfo() {
		return this.info;
	}
	
	public void setDownloadFlag(boolean flagValue) {
		if (!flagValue) {
			flag.setIcon(ResourceManager.getIcon("track_idle.png"));
			if (info.getDownload())
				warning.setIcon(ResourceManager.getIcon("danger_icon.png"));
		}else{
			flag.setIcon(ResourceManager.getIcon("track_download.png"));
			if (info.getDownload())
				warning.setIcon(ResourceManager.getIcon("warning_icon.png"));
		}
		downloadFlag = flagValue;
	}
	
	public boolean isFlaggedForDownload() {
		return downloadFlag;
	}
	
	public void updateWarningStatus() {
		warning.setVisible(info.getDownload());
	}
	
	public void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		setOpaque(true);
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
					dm.selectTrack(info);
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(backColor);
			}
		});
		
		setBackground(backColor);
		
		
		
		title = new JLabel(info.getTitle());
		title.setFont(ResourceManager.getFont(ResourceManager.FONT_RALEWAY, 16));
		title.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		title.setPreferredSize(new Dimension(
				TRACK_TITLE_WIDTH, 40));
		
		flag = new JButton(ResourceManager.getIcon("track_idle.png"));
		flag.setContentAreaFilled(false);
		flag.setFocusPainted(false);
		flag.setOpaque(false);
		flag.setPreferredSize(new Dimension(DOWNLOAD_ICON_WIDTH, flag.getPreferredSize().height));
		flag.setBorder(new MatteBorder(0, 4, 0, 0, altColor));
		//flag.addActionListener(new DownloadFlyerListener(parent, resources));
		flag.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isShiftDown()) {
					dm.addTrackRange(getInfo());
				} else {
					((TrackEntry)flag.getParent()).setDownloadFlag(!downloadFlag);
					if (downloadFlag) {
						dm.addTrack(info);
					} else {
						dm.removeTrack(info);
					}
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!downloadFlag)
					flag.setIcon(ResourceManager.getIcon("track_hover.png"));
				setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!downloadFlag)
					flag.setIcon(ResourceManager.getIcon("track_idle.png"));
				setBackground(backColor);
			}
		});
		
		warning = new JLabel(ResourceManager.getIcon("danger_icon.png"));
		warning.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
		warning.setVisible(info.getDownload());
		warning.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				dm.setWarningMessage("This track has already been downloaded");;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				dm.setWarningMessage("");;
			}
		});

		add(title);
		add(Box.createHorizontalGlue());
		add(warning);
		add(flag);
	}
	
}
