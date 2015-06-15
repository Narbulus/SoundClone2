package com.brassbeluga.launcher.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

import com.brassbeluga.launcher.ui.controls.LockScreenMouseListener;

@SuppressWarnings("serial")
public class GlassLockPane extends JPanel {
	
	private Rectangle bounds;
	private Color color;
	
	public GlassLockPane(Rectangle bounds, Component redirect, Color color) {
		this.bounds = bounds;
		this.color = color;
		
		this.addMouseListener(new LockScreenMouseListener(redirect));
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(color);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
}
