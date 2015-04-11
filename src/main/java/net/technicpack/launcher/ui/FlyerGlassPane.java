package net.technicpack.launcher.ui;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import net.technicpack.launcher.ui.components.songs.TrackEntry;
import net.technicpack.ui.lang.ResourceLoader;

public class FlyerGlassPane extends JPanel {
	public static final int INITIAL_X_OFFSET = 44;
	
	private ResourceLoader resources;
	private Point flyerLocation;
	
	public FlyerGlassPane(ResourceLoader resources) {
		this.resources = resources;
	}
	
	public void setFlyerLocation(Point location) {
		flyerLocation = location;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (flyerLocation != null)
			g.drawImage(resources.getImage("track_download.png"), 
					flyerLocation.x + INITIAL_X_OFFSET, flyerLocation.y, null);
	}
}
