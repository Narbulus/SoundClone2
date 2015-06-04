package com.brassbeluga.launcher.resources;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.technicpack.ui.lang.ResourceLoader;

public class ResourceManager {
	
	// True if launching soundcloud from the IDE, false if packaging into jar
	public static final boolean IS_DEVBUILD = false;
	public static final String FONT_RALEWAY = "fonts/Raleway-Light.ttf";
	public static final String FONT_OPENSANS = "fonts/OpenSans-Regular.ttf";
	
	private static final String RESOURCE_BASE_URL = "src/main/resources/";
	private static final String RESOURCE_JAR_URL = "resources/";
	
	/**
	 * Get an image icon from a specified resource. This is typically used when
	 * you have small images that you want to add to buttons or to use as window icons.
	 */
	public static ImageIcon getIcon(String iconName) {
		if (IS_DEVBUILD)
			return getDevIcon(iconName);
		else 
			return getJarIcon(iconName);
	}
	
	private static ImageIcon getDevIcon(String iconName) {
		return new ImageIcon(RESOURCE_BASE_URL + iconName);
	}
	
	private static ImageIcon getJarIcon(String iconName) {
		ClassLoader cl = ResourceManager.class.getClassLoader();
		return new ImageIcon(cl.getResource(RESOURCE_JAR_URL + iconName));
	}
	
	/**
	 * Load a buffered image from a local resource. Typically used when you need to
	 * manipulate individual pixels within an image, or when you want to double-buffer
	 * a custom paint(Graphics g) method. The image resides in RAM, so it can take
	 * up a lot of space.
	 */
	public static BufferedImage getImage(String imageName) {
		if (IS_DEVBUILD)
			return getDevImage(imageName);
		else
			return getJarImage(imageName);
	}
	
	private static BufferedImage getDevImage(String imageName) {
		try {
			return ImageIO.read(new File(RESOURCE_BASE_URL + imageName));
		} catch (IOException e) {
			// log
			e.printStackTrace();
			return null;
		}
	}
	
	private static BufferedImage getJarImage(String imageName) {
		ClassLoader cl = ResourceLoader.class.getClassLoader();
		try {
			InputStream stream = cl.getResourceAsStream(RESOURCE_JAR_URL + imageName);
			return ImageIO.read(stream);
		} catch (IOException e) {
			System.out.println("Hey");
			e.printStackTrace();
			return null;
		}
	}
	
	public static Font getFont(String fontName, int fontSize) {
		if (IS_DEVBUILD)
			return getDevFont(fontName, fontSize);
		else
			return getJarFont(fontName, fontSize);
	}
	
	private static Font getDevFont(String fontName, int fontSize) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, new File(RESOURCE_BASE_URL + fontName))
					.deriveFont(Font.PLAIN, fontSize);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Font getJarFont(String fontName, int fontSize) {
		ClassLoader cl = ResourceLoader.class.getClassLoader();
		try {
			return Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream(RESOURCE_JAR_URL + fontName))
				.deriveFont(Font.PLAIN, fontSize);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
			
	}
	
	public static InputStream getResourceAsStream(String resourceName) {
		if (IS_DEVBUILD)
			return getDevResourceAsStream(resourceName);
		else 
			return getJarResourceAsStream(resourceName);
	}
	
	private static InputStream getDevResourceAsStream(String resourceName) {
		try {
			return new FileInputStream(RESOURCE_BASE_URL + resourceName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static InputStream getJarResourceAsStream(String resourceName) {
		ClassLoader cl = ResourceLoader.class.getClassLoader();
		return cl.getResourceAsStream(RESOURCE_JAR_URL + resourceName);
	}
	
	
}
