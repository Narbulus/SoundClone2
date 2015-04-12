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

package com.brassbeluga.launcher.settings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;

import net.technicpack.utilslib.OperatingSystem;
import net.technicpack.utilslib.Utils;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonSyntaxException;

public class SettingsFactory {
    public static BrassbelugaSettings buildSettingsObject(String runningDir, boolean isMover) {

        System.out.println("Settings for exe: "+runningDir);

        File portableSettingsDir = getPortableSettingsDir(runningDir, isMover);

        if (portableSettingsDir == null)
            System.out.println("Portable settings dir has broken terribly");
        else
            System.out.println("Portable settings dir: "+portableSettingsDir.getAbsolutePath());

        BrassbelugaSettings portableSettings = tryGetSettings(portableSettingsDir);

        if (portableSettings != null && portableSettings.isPortable()) {
            System.out.println("Portable settings file found.");
            return portableSettings;
        }

        File installedSettingsDir = OperatingSystem.getOperatingSystem().getUserDirectoryForApp("technic");

        BrassbelugaSettings settings = tryGetSettings(installedSettingsDir);

        return settings;
    }

    private static BrassbelugaSettings tryGetSettings(File rootDir) {
        if (!rootDir.exists())
            return null;

        File settingsFile = new File(rootDir, "settings.json");
        if (settingsFile == null || !settingsFile.exists())
            return null;

        try {
            String json = FileUtils.readFileToString(settingsFile, Charset.forName("UTF-8"));
            BrassbelugaSettings settings = Utils.getGson().fromJson(json, BrassbelugaSettings.class);

            if (settings != null)
                settings.setFilePath(settingsFile);

            return settings;
        } catch (JsonSyntaxException e) {
            Utils.getLogger().log(Level.WARNING, "Unable to load version from " + settingsFile);
            return null;
        } catch (IOException e) {
            Utils.getLogger().log(Level.WARNING, "Unable to load version from " + settingsFile);
            return null;
        }
    }

    private static File getPortableSettingsDir(String runningDir, boolean isMover) {
        File runningFolder = new File(runningDir).getParentFile();

        if (isMover)
            return runningFolder;
        else
            return new File(runningFolder,"technic");
    }
}
