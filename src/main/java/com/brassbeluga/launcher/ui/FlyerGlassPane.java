package com.brassbeluga.launcher.ui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import com.brassbeluga.launcher.ui.listeners.DownloadFlyer;

import net.technicpack.ui.lang.ResourceLoader;

public class FlyerGlassPane extends JPanel implements ActionListener {
	public static final int INITIAL_X_OFFSET = 44;
	
	public static final int FLIGHT_DURATION = 200;
	public static final int FLIGHT_INTERVAL = 20;
	
	public static final int D_COUNTER_FLY_TO_X = 208;
	public static final int D_COUNTER_FLY_TO_Y = 43;
	
	private ResourceLoader resources;
	private LauncherFrame launcher;
	private List<DownloadFlyer> flyers;
	
	public FlyerGlassPane(ResourceLoader resources, LauncherFrame launcher) {
		this.resources = resources;
		this.launcher = launcher;
		this.flyers = new ArrayList<DownloadFlyer>();
	}
	
	public void addFlyer(DownloadFlyer flyer) {
		flyers.add(flyer);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (flyers.isEmpty()) {
			return;
		}
		
		Iterator<DownloadFlyer> itr = flyers.iterator();
		while (itr.hasNext()) {
			DownloadFlyer flyer = itr.next();
			if (flyer.isExpired()){
				itr.remove();
				launcher.onFlyerArrival();
			} else {
				flyer.updateFlyer();
			}
		}
		
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for (DownloadFlyer f : flyers) {
			g.drawImage(resources.getImage("track_download.png"), 
					(int)f.getX(), (int)f.getY(), null);
		}
	}
}
