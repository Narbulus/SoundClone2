package net.technicpack.launcher.ui.components.songs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import net.brassbeluga.sound.gson.TrackInfo;
import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.ui.lang.ResourceLoader;

public class TrackEntry extends JPanel {
	
	private TrackInfo info;
	private boolean downloadFlag;
	
	private ResourceLoader resources;
	private LauncherFrame parent;
	private Color backColor;
	private Color altColor;
	
	private JLabel title;
	private JButton flag;
	private int index;
	
	public TrackEntry(ResourceLoader resources, TrackInfo info, int index, LauncherFrame parent) {
		this.info = info;
		this.downloadFlag = false;
		this.resources = resources;
		this.index = index;
		this.parent = parent;
		
		if (index % 2 == 0) {
			backColor = LauncherFrame.COLOR_BLUE_ALT;
			altColor = LauncherFrame.COLOR_BLUE;
		}else{
			backColor = LauncherFrame.COLOR_BLUE;
			altColor = LauncherFrame.COLOR_BLUE_ALT;
		}
		
		initComponents();
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
				parent.selectTrack(info);
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
		title.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 16));
		title.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
		title.setPreferredSize(new Dimension(
				title.getPreferredSize().width, 40));
		
		flag = new JButton(resources.getIcon("track_idle.png"));
		flag.setContentAreaFilled(false);
		flag.setFocusPainted(false);
		flag.setOpaque(false);
		flag.setPreferredSize(new Dimension(120, flag.getPreferredSize().height));
		flag.setBorder(new MatteBorder(0, 4, 0, 0, altColor));
		flag.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (downloadFlag) {
					downloadFlag = false;
					flag.setIcon(resources.getIcon("track_idle.png"));
					parent.unFlagTrackForDownload(info);
				}else{
					downloadFlag = true;
					flag.setIcon(resources.getIcon("track_download.png"));
					parent.flagTrackForDownload(info);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!downloadFlag)
					flag.setIcon(resources.getIcon("track_hover.png"));
				setBackground(LauncherFrame.COLOR_BUTTON_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!downloadFlag)
					flag.setIcon(resources.getIcon("track_idle.png"));
				setBackground(backColor);
			}
		});
		
		add(title);
		add(Box.createHorizontalGlue());
		add(flag);
	}
	
}
