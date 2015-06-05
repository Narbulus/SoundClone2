package com.brassbeluga.launcher.ui.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.launcher.ui.listeners.TabFlashListener;
import com.brassbeluga.managers.DownloadAction;
import com.brassbeluga.managers.DownloadManager;
import com.brassbeluga.observer.DownloadsObserver;

@SuppressWarnings("serial")
public class DownloadHeaderTab extends HeaderTab implements DownloadsObserver {
	private static final int D_COUNTER_SIZE = 20;
	private static final int D_COUNTER_MARGIN = 3;
	private static final int D_COUNTER_EXP = 8;
	
	private boolean clearFontLag = true;
	
	private int downloadsQueued;
	private TabFlashListener tabFlashListener;
	private Timer tabFlashTimer;

	public DownloadHeaderTab(String text) {
		super(text);
		downloadsQueued = 0;
		
		// Set up tab flashing
		tabFlashListener = new TabFlashListener(LauncherFrame.COLOR_BLUE, this);
		this.addActionListener(tabFlashListener);
		tabFlashTimer = new Timer(LauncherFrame.TAB_FLAST_INTERVAL, tabFlashListener);
		tabFlashTimer.setActionCommand(LauncherFrame.DOWNLOAD_TRACK_COMMAND);
	}
	
	/**
	 * Begins the flashing animation on the download tab.
	 */
	public void beginDownloadTabFlash() {
		this.setOpaque(true);
		
		tabFlashListener.reset();
		tabFlashTimer.start();
	}

	/**
	 * ends the flashing animation on the download tab (also stops the flash timer.)
	 */
	public void endDownloadTabFlash() {
		this.setOpaque(false);
		this.setBackground(LauncherFrame.COLOR_BLUE_DARKER);
		tabFlashTimer.stop();
	}
	
	@Override
	public void update(DownloadManager dm, DownloadAction action) {
		// Only flash when the downloads are added not removed.
		if (downloadsQueued < dm.getTracks().size()) {
			beginDownloadTabFlash();
		}
		downloadsQueued = dm.getTracks().size();
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		char[] arr = Integer.toString(downloadsQueued).toCharArray();
		// Draw one frame of some text to initialize the font renderer
		// AND KILL THE FLASH LAG
		if (clearFontLag) {
			clearFontLag = false;
			Graphics2D g2d = (Graphics2D) g;
			Font f = g2d.getFont();
			g2d.setFont(new Font(f.getName(), Font.PLAIN, (int)(f.getSize() * .5)));
		}
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
