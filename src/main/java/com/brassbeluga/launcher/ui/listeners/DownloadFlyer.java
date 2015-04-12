package com.brassbeluga.launcher.ui.listeners;

import java.awt.Point;

import com.brassbeluga.launcher.ui.FlyerGlassPane;

public class DownloadFlyer {
	public static final int FLIGHT_DURATION = 200;
	public static final int FLIGHT_INTERVAL = 5;
	
	private Point flyerLocation;
	private Point startPoint;
	private Point endPoint;
	private int callCount = 0;
	private boolean expired = false;
	
	public DownloadFlyer(Point flyerLocation) {
		this.flyerLocation = flyerLocation;
		this.startPoint = (Point) flyerLocation.clone();
		this.endPoint = new Point(FlyerGlassPane.D_COUNTER_FLY_TO_X,
				FlyerGlassPane.D_COUNTER_FLY_TO_Y);
	}
	
	public double getX() {
		return flyerLocation.getX();
	}
	
	public double getY() {
		return flyerLocation.getY();
	}
	
	public void updateFlyer() {
		callCount++;
		double x = flyerLocation.getX();
		double y = flyerLocation.getY();
		
		x = tween(callCount * FLIGHT_INTERVAL, startPoint.getX(), 
				endPoint.getX() - startPoint.getX(), FLIGHT_DURATION);
		y = tween(callCount * FLIGHT_INTERVAL, startPoint.getY(), 
				endPoint.getY() - startPoint.getY(), FLIGHT_DURATION);
		
		flyerLocation.setLocation(x, y);
		
		if (callCount * FLIGHT_INTERVAL == FLIGHT_DURATION) {
			expired = true;
		}
	}
	
	public boolean isExpired() {
		return expired;
	}

	public static double tween(double t, double b, double c, double d) {
		return c*t/d + b;
	}
}
