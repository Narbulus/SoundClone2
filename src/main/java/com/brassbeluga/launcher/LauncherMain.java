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

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import net.technicpack.ui.components.Console;
import net.technicpack.ui.components.ConsoleFrame;
import net.technicpack.ui.components.ConsoleHandler;
import net.technicpack.ui.components.LoggerOutputStream;
import net.technicpack.utilslib.Utils;

import com.brassbeluga.launcher.resources.ResourceManager;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.sound.main.DownloadLikes;

public class LauncherMain {


	private static ConsoleFrame consoleFrame;

	public static void main(String[] args) throws Exception {
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Utils.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
		}
		
		setupLogging();
		startLauncher();

	}

	
	private static void setupLogging() {
		System.out.println("Setting up logging");
		final Logger logger = Utils.getLogger();

		LauncherMain.consoleFrame = new ConsoleFrame(2500,
				ResourceManager.getImage("icon.png"));
		Console console = new Console(LauncherMain.consoleFrame,
				"0");
		LauncherMain.consoleFrame.setVisible(true);

		logger.addHandler(new ConsoleHandler(console));

		System.setOut(new PrintStream(new LoggerOutputStream(console,
				Level.INFO, logger), true));
		System.setErr(new PrintStream(new LoggerOutputStream(console,
				Level.SEVERE, logger), true));

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Unhandled Exception in " + t, e);
			}
		});
	}

	private static void startLauncher() {
		// Start the launcher!
		new LauncherFrame();
	}
}
