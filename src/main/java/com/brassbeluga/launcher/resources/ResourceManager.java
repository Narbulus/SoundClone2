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

public class ResourceManager {
	public static final String FONT_RALEWAY = "fonts/Raleway-Light.ttf";
	public static final String FONT_OPENSANS = "fonts/OpenSans-Regular.ttf";
	
	private static final String RESOURCE_BASE_URL = "src/main/resources/";
	
	/**
	 * Get an image icon from a specified resource. This is typically used when
	 * you have small images that you want to add to buttons or to use as window icons.
	 */
	public static ImageIcon getIcon(String iconName) {
		return new ImageIcon(RESOURCE_BASE_URL + iconName);
	}
	
	/**
	 * Load a buffered image from a local resource. Typically used when you need to
	 * manipulate individual pixels within an image, or when you want to double-buffer
	 * a custom paint(Graphics g) method. The image resides in RAM, so it can take
	 * up a lot of space.
	 */
	public static BufferedImage getImage(String imageName) {
		try {
			return ImageIO.read(new File(RESOURCE_BASE_URL + imageName));
		} catch (IOException e) {
			// log
			e.printStackTrace();
			return null;
		}
	}
	
	public static Font getFont(String fontName, int fontSize) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, new File(RESOURCE_BASE_URL + fontName))
					.deriveFont(Font.PLAIN, fontSize);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static InputStream getResourceAsStream(String resourceName) {
		try {
			return new FileInputStream(RESOURCE_BASE_URL + resourceName);
		} catch (FileNotFoundException e) {
			// log
			e.printStackTrace();
			return null;
		}
	}
}
