package com.videotheatre;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import com.google.gson.GsonBuilder;

public class Settings {
    public static int rowCount;
    public static int columnCount;
    public static List<String> videoDirectories;
    public static boolean loopVideo;
    public static boolean stretchVideoToGrid;
    public static boolean removeWatchedVideosFromList;

    private static String getSettingsFilePath() {
        var path = OsDetect.getSettingsDir();
        var dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.printf("created settings directory: %s\n", path);
        }

        return String.format("%s%s%s", path, File.separator, "settings.json");
    }

    public static boolean loadSettingsFromFile() throws IOException {
        var settingsPath = getSettingsFilePath();

        var file = new File(settingsPath);
        if (file.exists()) {
            var json = Files.readString(file.toPath());

            var gsonBuilder  = new GsonBuilder();
            gsonBuilder.excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT);
            var gson = gsonBuilder.create();

            var settings = gson.fromJson(json, Settings.class);
            System.out.printf("Loaded settings from file at %s\n", settingsPath);

            return true;
        }

        return false;
    }

    public static void saveSettingsToFile() throws IOException {
        var settingsPath = getSettingsFilePath();
        var file = new File(settingsPath);
        var settings = new Settings();

        var gsonBuilder  = new GsonBuilder();
        gsonBuilder.excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT);
        var gson = gsonBuilder.create();

        var json = gson.toJson(settings);
        Files.writeString(file.toPath(), json);

        System.out.printf("Saved settings file to %s\n", settingsPath);
    }
}
