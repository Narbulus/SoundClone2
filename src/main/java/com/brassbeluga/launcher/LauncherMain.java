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

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import net.technicpack.autoupdate.IBuildNumber;
import net.technicpack.autoupdate.Relauncher;
import net.technicpack.launchercore.install.LauncherDirectories;
import net.technicpack.launchercore.logging.BuildLogFormatter;
import net.technicpack.launchercore.logging.RotatingFileHandler;
import net.technicpack.ui.components.Console;
import net.technicpack.ui.components.ConsoleFrame;
import net.technicpack.ui.components.ConsoleHandler;
import net.technicpack.ui.components.LoggerOutputStream;
import net.technicpack.ui.lang.ResourceLoader;
import net.technicpack.utilslib.OperatingSystem;
import net.technicpack.utilslib.Utils;

import com.beust.jcommander.JCommander;
import com.brassbeluga.launcher.autoupdate.CommandLineBuildNumber;
import com.brassbeluga.launcher.autoupdate.VersionFileBuildNumber;
import com.brassbeluga.launcher.io.TechnicLauncherDirectories;
import com.brassbeluga.launcher.settings.SettingsFactory;
import com.brassbeluga.launcher.settings.StartupParameters;
import com.brassbeluga.launcher.settings.TechnicSettings;
import com.brassbeluga.launcher.ui.LauncherFrame;
import com.brassbeluga.sound.main.DownloadLikes;

public class LauncherMain {

	public static ConsoleFrame consoleFrame;

	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Utils.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
		}


		StartupParameters params = new StartupParameters(args);
		try {
			new JCommander(params, args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		TechnicSettings settings = null;

		try {
			settings = SettingsFactory.buildSettingsObject(
					Relauncher.getRunningPath(LauncherMain.class),
					params.isMover());
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}

		if (settings == null) {
			ResourceLoader installerResources = new ResourceLoader(null, "net",
					"technicpack", "launcher", "resources");
			installerResources.setLocale(ResourceLoader.DEFAULT_LOCALE);
			return;
		}

		LauncherDirectories directories = new TechnicLauncherDirectories(
				settings.getTechnicRoot());
		ResourceLoader resources = new ResourceLoader(directories, "net",
				"technicpack", "launcher", "resources");
		resources.setLocale(settings.getLanguageCode());

		IBuildNumber buildNumber = null;

		if (params.getBuildNumber() != null
				&& !params.getBuildNumber().isEmpty())
			buildNumber = new CommandLineBuildNumber(params);
		else
			buildNumber = new VersionFileBuildNumber(resources);

		setupLogging(directories, resources, buildNumber);
		startLauncher(resources);

	}

	private static void setupLogging(LauncherDirectories directories,
			ResourceLoader resources, IBuildNumber buildNumber) {
		System.out.println("Setting up logging");
		final Logger logger = Utils.getLogger();
		File logDirectory = new File(directories.getLauncherDirectory(), "logs");
		if (!logDirectory.exists()) {
			logDirectory.mkdir();
		}
		File logs = new File(logDirectory, "techniclauncher_%D.log");
		RotatingFileHandler fileHandler = new RotatingFileHandler(
				logs.getPath());

		fileHandler.setFormatter(new BuildLogFormatter(buildNumber
				.getBuildNumber()));

		for (Handler h : logger.getHandlers()) {
			logger.removeHandler(h);
		}
		logger.addHandler(fileHandler);
		logger.setUseParentHandlers(false);

		LauncherMain.consoleFrame = new ConsoleFrame(2500,
				resources.getImage("icon.png"));
		Console console = new Console(LauncherMain.consoleFrame,
				buildNumber.getBuildNumber());
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

	private static void startLauncher(ResourceLoader resources) {

		Utils.getLogger().info("OS: " + System.getProperty("os.name").toLowerCase(Locale.ENGLISH));
		Utils.getLogger().info("Identified as " + OperatingSystem.getOperatingSystem().getName());

		DownloadLikes downloader = null;
		try {
			downloader = new DownloadLikes(resources);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start the launcher!
		new LauncherFrame(resources, downloader);
	}
}
