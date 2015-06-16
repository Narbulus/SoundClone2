/*
 * This file is part of The Technic Launcher Version 3.
 * Copyright ©2015 Syndicate, LLC
 *
 * The Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Technic Launcher  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.brassbeluga.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.managers.ConfigurationManager;

public class LauncherMain {
	private static final String BASE_URL = "http://www.brassbeluga.com/soundclone/";

	public static void main(String[] args) throws Exception {
		attemptUpdate();
		new LauncherFrame();
	}

	/*
	 * Attemps an update. If update is needed this method will not return.
	 */
	private static void attemptUpdate() {

		try {	
			InputStream in = ResourceManager.getResourceAsStream("version.txt");
			String version = new BufferedReader(new InputStreamReader(in)).readLine().trim();
			
			String url = BASE_URL + "update.php?version=" + version;
				
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			
			if (con.getHeaderField("Update-Needed").equals("1")) {
				String updaterPath = ConfigurationManager.getTempDirectory() + "/updater.jar";
				File updater = new File(updaterPath);
				updater.delete();
				FileOutputStream fos = new FileOutputStream(updater);
		 
				InputStream is = con.getInputStream();
				int bytesRead = -1;
				byte[] buffer = new byte[16384];
				while ((bytesRead = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
				fos.close();
				is.close();
				
				// Launch the updater and then hasta la vista.
				Runtime.getRuntime().exec("java -jar " + updaterPath + " " + System.getProperty("user.dir") + "/");
				System.exit(0);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
