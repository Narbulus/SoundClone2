package com.brassbeluga.launcher.ui.listeners;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.launcher.ui.controls.DownloadHeaderTab;
import com.brassbeluga.launcher.ui.controls.HeaderTab;

public class TabFlashListener implements ActionListener {
	private Color startColor;
	private DownloadHeaderTab downloadTab;
	private boolean brighten;
	private int callCount;
	
	public TabFlashListener(Color startingColor, DownloadHeaderTab downloadTab) {
		this.startColor = startingColor;
		this.downloadTab = downloadTab;
		brighten = true;
		callCount = 0;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (!arg0.getActionCommand().equals(LauncherFrame.DOWNLOAD_TRACK_COMMAND)) {
			return;
		}
		callCount++;
		
		int r, g, b;
		if (brighten) {
			r = (int) tween(callCount * LauncherFrame.TAB_FLAST_INTERVAL, startColor.getRed(), 
					255 - startColor.getRed(), LauncherFrame.TAB_FLASH_TIME);
			g = (int) tween(callCount * LauncherFrame.TAB_FLAST_INTERVAL, startColor.getGreen(), 
					255 - startColor.getGreen(), LauncherFrame.TAB_FLASH_TIME);
			b = (int) tween(callCount * LauncherFrame.TAB_FLAST_INTERVAL, startColor.getBlue(), 
					255 - startColor.getBlue(), LauncherFrame.TAB_FLASH_TIME);
		} else {
			// darken
			r = (int) tween(callCount * LauncherFrame.TAB_FLAST_INTERVAL, 255, 
					startColor.getRed() - 255, LauncherFrame.TAB_FLASH_TIME);
			g = (int) tween(callCount * LauncherFrame.TAB_FLAST_INTERVAL, 255, 
					startColor.getGreen() - 255, LauncherFrame.TAB_FLASH_TIME);
			b = (int) tween(callCount * LauncherFrame.TAB_FLAST_INTERVAL, 255, 
					startColor.getBlue() - 255, LauncherFrame.TAB_FLASH_TIME);
		}
		
		downloadTab.setBackground(new Color(r,g,b));
		
		if (callCount * LauncherFrame.TAB_FLAST_INTERVAL == LauncherFrame.TAB_FLASH_TIME 
				&& !brighten) {
			// Done with the flash, so reset.
			downloadTab.endDownloadTabFlash();
			callCount = 0;
			brighten = true;
		} else if (callCount * LauncherFrame.TAB_FLAST_INTERVAL == LauncherFrame.TAB_FLASH_TIME) {
			// Tab is now white so begin to descend back to original color.
			brighten = false;
			callCount = 0;
		}
	}
	
	public void reset() {
		callCount = 0;
		brighten = true;
	}
	
	private static float tween(float t,float b, float c, float d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	}

}
