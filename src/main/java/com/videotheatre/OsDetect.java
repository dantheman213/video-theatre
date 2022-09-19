package com.videotheatre;

import java.io.File;

public class OsDetect {
    private static final String appName = "video-theatre";

    public enum OperatingSystem {
        WINDOWS_FAMILY,
        MACOS_FAMILY,
        LINUX_FAMILY,
        OTHER
    }

    private static OperatingSystem detectedOperatingSystem;

    public static OperatingSystem detect() {
        if(detectedOperatingSystem == null) {
            String rawOsProp = System.getProperty("os.name").toLowerCase();
            //System.out.println(rawOsProp);

            if(rawOsProp.contains("win")) {
                detectedOperatingSystem = OperatingSystem.WINDOWS_FAMILY;
            } else if(rawOsProp.contains("mac") || rawOsProp.contains("osx")|| rawOsProp.contains("os x")) {
                detectedOperatingSystem = OperatingSystem.MACOS_FAMILY;
            } else if(rawOsProp.contains("nix") || rawOsProp.contains("nux") || rawOsProp.contains("aix")) {
                detectedOperatingSystem = OperatingSystem.LINUX_FAMILY;
            } else {
                detectedOperatingSystem = OperatingSystem.OTHER;
            }
        }

        return detectedOperatingSystem;
    }

    public static String getSettingsDir() {
        var os = detect();

        switch (os) {
            case WINDOWS_FAMILY -> {
                return String.format("%s%s%s", System.getenv("AppData"), File.separator, appName);
            }

            case MACOS_FAMILY -> {
                return String.format("%s%sLibrary%sApplication Support%s%s", System.getenv("user.home"), File.separator, File.separator, File.separator, appName);
            }

            case LINUX_FAMILY -> {
                return String.format("%s%s%s", System.getenv("user.home"), File.separator, appName);
            }
        }

        return "";
    };
}
