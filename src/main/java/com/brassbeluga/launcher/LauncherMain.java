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

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.brassbeluga.launcher.ui.LauncherFrame;

public class LauncherMain {
	private static final String BASE_URL = "http://www.brassbeluga.com/soundclone/";

	public static void main(String[] args) throws Exception {
		if (checkUpdate()) {
			Runtime.getRuntime().exec("java -jar updater.jar");
			System.exit(0);
		}
		new LauncherFrame();
	}

	private static boolean checkUpdate() {
		boolean update = false;
		
		try {
			String version = new String(Files.readAllBytes(Paths.get("version")), "UTF-8").trim();
			
			String url = BASE_URL + "update.php?version=" + version;
				
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			
			if (con.getHeaderField("Update-Needed").equals("1")) {
				update = true;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return update;
	}
}
