package com.brassbeluga.launcher.ui.controls;

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.JMenuBar;
import javax.swing.event.MouseInputAdapter;

import com.brassbeluga.launcher.ui.GlassLockPane;

public class LockScreenMouseListener extends MouseInputAdapter {
	Toolkit toolkit;

	public LockScreenMouseListener() {
		toolkit = Toolkit.getDefaultToolkit();
	}

	public void mouseMoved(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseDragged(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseClicked(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseEntered(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseExited(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mousePressed(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseReleased(MouseEvent e) {
		redispatchMouseEvent(e, true);
	}

	// A basic implementation of redispatching events.
	private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
		
	}

}
