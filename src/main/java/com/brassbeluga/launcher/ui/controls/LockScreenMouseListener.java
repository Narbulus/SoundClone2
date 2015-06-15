package com.brassbeluga.launcher.ui.controls;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

public class LockScreenMouseListener extends MouseInputAdapter {
	Toolkit toolkit;
	Component redirect;

	public LockScreenMouseListener(Component redirect) {
		toolkit = Toolkit.getDefaultToolkit();
		this.redirect = redirect;
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
		System.out.println(e.getPoint().toString());
		if (redirect.getBounds().contains(e.getPoint())) {
			redirect.dispatchEvent(e);
		}
	}

}
