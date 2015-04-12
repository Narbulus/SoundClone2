package com.brassbeluga.launcher.ui.components.songs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

public class LabelProgressBar extends JLabel {

	private int min;
	private int max;
	private int progress;
	private int loadWidth;
	private Color loadColor;
	
	public LabelProgressBar(int min, int max, int loadWidth) {
		super();
		this.min = min;
		this.max = max;
		progress = 0;
		this.loadWidth = loadWidth;
		setOpaque(false);
	}
	
	public void setProgress(int progress) {
		this.progress = progress;
		this.repaint();
	}
	
	public void setLoadColor(Color c) {
		this.loadColor = c;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(loadColor);
		g2d.fillRect(0, 0, (int) (((1.0 * progress) / max ) * getWidth()), getHeight());
		super.paintComponent(g);
	}
	
}
