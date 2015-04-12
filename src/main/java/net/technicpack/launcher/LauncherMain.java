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

package net.technicpack.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import net.brassbeluga.sound.main.DownloadLikes;
import net.technicpack.autoupdate.IBuildNumber;
import net.technicpack.autoupdate.Relauncher;
import net.technicpack.autoupdate.http.HttpUpdateStream;
import net.technicpack.launcher.autoupdate.CommandLineBuildNumber;
import net.technicpack.launcher.autoupdate.TechnicRelauncher;
import net.technicpack.launcher.autoupdate.VersionFileBuildNumber;
import net.technicpack.launcher.io.TechnicAvatarMapper;
import net.technicpack.launcher.io.TechnicFaceMapper;
import net.technicpack.launcher.io.TechnicInstalledPackStore;
import net.technicpack.launcher.io.TechnicLauncherDirectories;
import net.technicpack.launcher.io.TechnicUserStore;
import net.technicpack.launcher.launch.Installer;
import net.technicpack.launcher.settings.SettingsFactory;
import net.technicpack.launcher.settings.StartupParameters;
import net.technicpack.launcher.settings.TechnicSettings;
import net.technicpack.launcher.ui.LauncherFrame;
import net.technicpack.launchercore.auth.IUserStore;
import net.technicpack.launchercore.auth.IUserType;
import net.technicpack.launchercore.auth.UserModel;
import net.technicpack.launchercore.exception.DownloadException;
import net.technicpack.launchercore.image.ImageRepository;
import net.technicpack.launchercore.image.face.CrafatarFaceImageStore;
import net.technicpack.launchercore.image.face.WebAvatarImageStore;
import net.technicpack.launchercore.install.LauncherDirectories;
import net.technicpack.launchercore.install.ModpackInstaller;
import net.technicpack.launchercore.launch.java.JavaVersionRepository;
import net.technicpack.launchercore.launch.java.source.FileJavaSource;
import net.technicpack.launchercore.launch.java.source.InstalledJavaSource;
import net.technicpack.launchercore.logging.BuildLogFormatter;
import net.technicpack.launchercore.logging.RotatingFileHandler;
import net.technicpack.launchercore.mirror.MirrorStore;
import net.technicpack.launchercore.mirror.secure.rest.JsonWebSecureMirror;
import net.technicpack.launchercore.modpacks.ModpackModel;
import net.technicpack.launchercore.modpacks.resources.PackImageStore;
import net.technicpack.launchercore.modpacks.resources.PackResourceMapper;
import net.technicpack.launchercore.modpacks.resources.resourcetype.BackgroundResourceType;
import net.technicpack.launchercore.modpacks.resources.resourcetype.IModpackResourceType;
import net.technicpack.launchercore.modpacks.resources.resourcetype.IconResourceType;
import net.technicpack.launchercore.modpacks.resources.resourcetype.LogoResourceType;
import net.technicpack.launchercore.modpacks.sources.IAuthoritativePackSource;
import net.technicpack.launchercore.modpacks.sources.IInstalledPackRepository;
import net.technicpack.minecraftcore.launch.MinecraftLauncher;
import net.technicpack.minecraftcore.mojang.auth.AuthenticationService;
import net.technicpack.minecraftcore.mojang.auth.MojangUser;
import net.technicpack.platform.IPlatformApi;
import net.technicpack.platform.IPlatformSearchApi;
import net.technicpack.platform.PlatformPackInfoRepository;
import net.technicpack.platform.cache.ModpackCachePlatformApi;
import net.technicpack.platform.http.HttpPlatformApi;
import net.technicpack.platform.http.HttpPlatformSearchApi;
import net.technicpack.platform.io.AuthorshipInfo;
import net.technicpack.solder.ISolderApi;
import net.technicpack.solder.cache.CachedSolderApi;
import net.technicpack.solder.http.HttpSolderApi;
import net.technicpack.ui.components.Console;
import net.technicpack.ui.components.ConsoleFrame;
import net.technicpack.ui.components.ConsoleHandler;
import net.technicpack.ui.components.LoggerOutputStream;
import net.technicpack.ui.controls.installation.SplashScreen;
import net.technicpack.ui.lang.ResourceLoader;
import net.technicpack.utilslib.OperatingSystem;
import net.technicpack.utilslib.Utils;

import com.beust.jcommander.JCommander;

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

		String launcherBuild = buildNumber.getBuildNumber();
		int build = -1;

		try {
			build = Integer.parseInt((new VersionFileBuildNumber(resources))
					.getBuildNumber());
		} catch (NumberFormatException ex) {
			// This is probably a debug build or something, build number is
			// invalid
		}

		Relauncher launcher = new TechnicRelauncher(new HttpUpdateStream(
				"http://api.technicpack.net/launcher/"),
				settings.getBuildStream() + "4", build, directories, resources,
				params);

		try {
			if (launcher.runAutoUpdater())
				startLauncher(settings, params, directories, resources,
						buildNumber);
		} catch (InterruptedException e) {
			// Canceled by user
		} catch (DownloadException e) {
			// JOptionPane.showMessageDialog(null,
			// resources.getString("launcher.updateerror.download",
			// pack.getDisplayName(), e.getMessage()),
			// resources.getString("launcher.installerror.title"),
			// JOptionPane.WARNING_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

				// if (errorDialog == null) {
				// LauncherFrame frame = null;
				//
				// try {
				// frame = Launcher.getFrame();
				// } catch (Exception ex) {
				// //This can happen if we have a very early crash- before
				// Launcher initializes
				// }
				//
				// errorDialog = new ErrorDialog(frame, e);
				// errorDialog.setVisible(true);
				// }
			}
		});
	}

	private static void startLauncher(final TechnicSettings settings,
			StartupParameters startupParameters,
			LauncherDirectories directories, ResourceLoader resources,
			IBuildNumber buildNumber) throws Exception {
		UIManager.put("ComboBox.disabledBackground",
				LauncherFrame.COLOR_FORMELEMENT_INTERNAL);
		UIManager.put("ComboBox.disabledForeground",
				LauncherFrame.COLOR_GREY_TEXT);
		System.setProperty("xr.load.xml-reader",
				"org.ccil.cowan.tagsoup.Parser");

		Utils.getLogger().info(
				"OS: "
						+ System.getProperty("os.name").toLowerCase(
								Locale.ENGLISH));
		Utils.getLogger().info(
				"Identified as "
						+ OperatingSystem.getOperatingSystem().getName());

		final SplashScreen splash = new SplashScreen(
				resources.getImage("launch_splash.png"), 0);
		Color bg = LauncherFrame.COLOR_FORMELEMENT_INTERNAL;
		splash.getContentPane().setBackground(
				new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 255));
		splash.pack();
		splash.setLocationRelativeTo(null);
		splash.setVisible(true);

		boolean loadedAether = false;

		try {
			if (Class
					.forName(
							"org.apache.maven.repository.internal.MavenRepositorySystemUtils",
							false, ClassLoader.getSystemClassLoader()) != null) {
				loadedAether = true;
			}
		} catch (ClassNotFoundException ex) {
			// Aether is not loaded
		}

		if (!loadedAether) {
			File launcherAssets = new File(directories.getAssetsDirectory(),
					"launcher");

			File aether = new File(launcherAssets, "aether-dep.jar");

			try {
				Method m = URLClassLoader.class.getDeclaredMethod("addURL",
						URL.class);
				m.setAccessible(true);
				m.invoke(ClassLoader.getSystemClassLoader(), aether.toURI()
						.toURL());
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
			}
		}

		JavaVersionRepository javaVersions = new JavaVersionRepository();
		(new InstalledJavaSource()).enumerateVersions(javaVersions);
		FileJavaSource javaVersionFile = FileJavaSource.load(new File(settings
				.getTechnicRoot(), "javaVersions.json"));
		javaVersionFile.enumerateVersions(javaVersions);
		javaVersions.selectVersion(settings.getJavaVersion(),
				settings.getJavaBitness());

		IUserStore<MojangUser> users = TechnicUserStore.load(new File(
				directories.getLauncherDirectory(), "users.json"));
		UserModel userModel = new UserModel(users, new AuthenticationService());

		MirrorStore mirrorStore = new MirrorStore(userModel);
		mirrorStore.addSecureMirror("mirror.technicpack.net",
				new JsonWebSecureMirror("http://mirror.technicpack.net/",
						"mirror.technicpack.net"));

		IModpackResourceType iconType = new IconResourceType();
		IModpackResourceType logoType = new LogoResourceType();
		IModpackResourceType backgroundType = new BackgroundResourceType();

		PackResourceMapper iconMapper = new PackResourceMapper(directories,
				resources.getImage("icon.png"), iconType);
		ImageRepository<ModpackModel> iconRepo = new ImageRepository<ModpackModel>(
				iconMapper,
				new PackImageStore(iconType, mirrorStore, userModel));
		ImageRepository<ModpackModel> logoRepo = new ImageRepository<ModpackModel>(
				new PackResourceMapper(directories,
						resources.getImage("modpack/ModImageFiller.png"),
						logoType), new PackImageStore(logoType, mirrorStore,
						userModel));
		ImageRepository<ModpackModel> backgroundRepo = new ImageRepository<ModpackModel>(
				new PackResourceMapper(directories, null, backgroundType),
				new PackImageStore(backgroundType, mirrorStore, userModel));

		ImageRepository<IUserType> skinRepo = new ImageRepository<IUserType>(
				new TechnicFaceMapper(directories, resources),
				new CrafatarFaceImageStore("http://crafatar.com/", mirrorStore));

		ImageRepository<AuthorshipInfo> avatarRepo = new ImageRepository<AuthorshipInfo>(
				new TechnicAvatarMapper(directories, resources),
				new WebAvatarImageStore(mirrorStore));

		HttpSolderApi httpSolder = new HttpSolderApi(settings.getClientId(),
				userModel);
		ISolderApi solder = new CachedSolderApi(directories, httpSolder,
				60 * 60);
		HttpPlatformApi httpPlatform = new HttpPlatformApi(
				"http://api.technicpack.net/", mirrorStore,
				buildNumber.getBuildNumber());

		IPlatformApi platform = new ModpackCachePlatformApi(httpPlatform,
				60 * 60, directories);
		IPlatformSearchApi platformSearch = new HttpPlatformSearchApi(
				"http://api.technicpack.net/", buildNumber.getBuildNumber());

		IInstalledPackRepository packStore = TechnicInstalledPackStore
				.load(new File(directories.getLauncherDirectory(),
						"installedPacks"));
		IAuthoritativePackSource packInfoRepository = new PlatformPackInfoRepository(
				platform, solder);

		MinecraftLauncher launcher = new MinecraftLauncher(platform,
				directories, userModel, settings.getClientId(), javaVersions);
		ModpackInstaller modpackInstaller = new ModpackInstaller(platform,
				settings.getClientId());
		Installer installer = new Installer(startupParameters, mirrorStore,
				directories, modpackInstaller, launcher, settings, iconMapper);

		DownloadLikes downloader = new DownloadLikes(resources);

		
		final LauncherFrame frame = new LauncherFrame(resources, skinRepo,
				userModel, settings, iconRepo, logoRepo, backgroundRepo,
				installer, avatarRepo, platform, directories, packStore,
				startupParameters, javaVersions, javaVersionFile, buildNumber, downloader);

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				splash.dispose();
				if (settings.getLaunchToModpacks())
					frame.selectTab("modpacks");
			}
		};
	}
}
