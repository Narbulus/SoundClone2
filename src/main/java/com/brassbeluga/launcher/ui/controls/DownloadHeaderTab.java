package com.brassbeluga.launcher.ui.controls;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.RenderingHints;

import net.technicpack.ui.lang.ResourceLoader;

import java.awt.Graphics2D;

import com.brassbeluga.launcher.ui.LauncherFrame;

@SuppressWarnings("serial")
public class DownloadHeaderTab extends HeaderTab {
	public static final int D_COUNTER_SIZE = 20;
	public static final int D_COUNTER_MARGIN = 3;
	public static final int D_COUNTER_EXP = 8;
	
	private int downloadsQueued;

	public DownloadHeaderTab(String text, ResourceLoader resources) {
		super(text, resources);
		downloadsQueued = 0;
	}
	
	public void setDownloads(int n) {
		downloadsQueued = n;
		setIsActive(true);
	}
	
	public void incDownloads() {
		downloadsQueued++;
		setIsActive(true);
	}
	
	public void decDownloads() {
		downloadsQueued--;
		setIsActive(true);
	}
	
	
	
	@Override
	protected void paintComponent(Graphics g) {
		char[] arr = Integer.toString(downloadsQueued).toCharArray();
		super.paintComponent(g);
		if (downloadsQueued > 0) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color old = g2d.getColor();
			g2d.setColor(LauncherFrame.COLOR_RED.darker());
			g2d.fillOval(D_COUNTER_MARGIN, this.getSize().height - D_COUNTER_MARGIN - D_COUNTER_SIZE, 
					D_COUNTER_SIZE, D_COUNTER_SIZE);
	
			int i = 0;
			for (int downTemp = downloadsQueued / 10; downTemp != 0; downTemp /= 10) {
				g2d.fillRect(D_COUNTER_MARGIN + D_COUNTER_SIZE / 2 + i * D_COUNTER_EXP, 
						this.getSize().height - D_COUNTER_MARGIN - D_COUNTER_SIZE,
						D_COUNTER_EXP, D_COUNTER_SIZE);
				i++;
			}
			
			if (i != 0) { 
				g2d.fillOval(D_COUNTER_MARGIN + D_COUNTER_EXP * i, this.getSize().height - D_COUNTER_MARGIN - D_COUNTER_SIZE, 
						D_COUNTER_SIZE, D_COUNTER_SIZE);
			}
			
			g2d.setColor(old);
			
			Font f = g2d.getFont();
			g2d.setFont(new Font(f.getName(), Font.PLAIN, (int)(f.getSize() * .5)));
			g2d.drawChars(arr, 0, arr.length, 10, this.getSize().height - 8);
		}
	}

}
