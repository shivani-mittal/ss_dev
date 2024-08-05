package com.ibm.maas360.toolchain;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FolderCleanup {

    private static final Logger logger = Logger.getLogger(FolderCleanup.class.getName());

    public static void main(String[] args) {
        // Path to the main directory containing the app folders
        String mainDirPath = "evidences";
        File mainDir = new File(mainDirPath);

        if (mainDir.isDirectory()) {
            logger.info("Starting folder cleanup process...");

            // Map to store the latest folder for each app version
            Map<String, File> latestFolders = new HashMap<>();

            // Patterns to match the two scenarios
            Pattern patternWithTimestamp = Pattern.compile("(.+)-(\\d+(\\.\\d+)+)-REL-(\\d{8}\\.\\d{6})");
            Pattern patternWithoutTimestamp = Pattern.compile("(.+)-(\\d+(\\.\\d+)+)");

            for (File folder : Objects.requireNonNull(mainDir.listFiles())) {
                if (folder.isDirectory()) {
                    Matcher matcherWithTimestamp = patternWithTimestamp.matcher(folder.getName());
                    Matcher matcherWithoutTimestamp = patternWithoutTimestamp.matcher(folder.getName());

                    if (matcherWithTimestamp.matches()) {
                        String appName = matcherWithTimestamp.group(1);
                        String version = matcherWithTimestamp.group(2);
                        String dateTime = matcherWithTimestamp.group(4);

                        logger.info("Found folder: " + folder.getName() + " with appName: " + appName + ", version: " + version + ", and dateTime: " + dateTime);

                        // Create a unique key with appName and version
                        String key = appName + "-" + version;

                        // Compare with the current latest version
                        if (!latestFolders.containsKey(key) || compareDateTime(dateTime, extractDateTime(latestFolders.get(key).getName())) > 0) {
                            latestFolders.put(key, folder);
                            logger.info("Updating latest folder for app: " + appName + " version: " + version + " to dateTime: " + dateTime);
                        }
                    } else if (matcherWithoutTimestamp.matches()) {
                        String appName = matcherWithoutTimestamp.group(1);
                        String version = matcherWithoutTimestamp.group(2);

                        logger.info("Found folder: " + folder.getName() + " with appName: " + appName + " and version: " + version);

                        // Create a unique key with appName and version
                        String key = appName + "-" + version;

                        // Compare with the current latest version by version number
                        if (!latestFolders.containsKey(key) || compareVersions(version, extractVersion(latestFolders.get(key).getName())) > 0) {
                            latestFolders.put(key, folder);
                            logger.info("Updating latest folder for app: " + appName + " version: " + version);
                        }
                    }
                }
            }
                    // Remove older folders, keeping only the latest
            for (File folder : Objects.requireNonNull(mainDir.listFiles())) {
                if (folder.isDirectory()) {
                    Matcher matcherWithTimestamp = patternWithTimestamp.matcher(folder.getName());
                    Matcher matcherWithoutTimestamp = patternWithoutTimestamp.matcher(folder.getName());
                    String appName = "";
                    String version = "";
                    String key = "";

                    if (matcherWithTimestamp.matches()) {
                        appName = matcherWithTimestamp.group(1);
                        version = matcherWithTimestamp.group(2);
                        key = appName + "-" + version;
                    } else if (matcherWithoutTimestamp.matches()) {
                        appName = matcherWithoutTimestamp.group(1);
                        version = matcherWithoutTimestamp.group(2);
                        key = appName + "-" + version;
                    }

                    // Delete the folder if it is not the latest one stored in the map
                    if (!key.isEmpty() && latestFolders.containsKey(key) && !folder.equals(latestFolders.get(key))) {
                        logger.info("Deleting old folder: " + folder.getName());
                        deleteDirectory(folder);
                    }
                }
            }

            logger.info("Folder cleanup process completed.");
        } else {
            logger.warning("Provided path is not a directory: " + mainDirPath);
        }
    }

    // Compare two dateTime strings in the format YYYYMMDD.HHmmss
    private static int compareDateTime(String dt1, String dt2) {
        return dt1.compareTo(dt2);
    }

    // Extract the dateTime from the folder name
    private static String extractDateTime(String folderName) {
        Matcher matcher = Pattern.compile("(.+)-(\\d+(\\.\\d+)+)-REL-(\\d{8}\\.\\d{6})").matcher(folderName);
        return matcher.matches() ? matcher.group(4) : "";
    }

    // Extract the version from the folder name without timestamp
    private static String extractVersion(String folderName) {
        Matcher matcher = Pattern.compile("(.+)-(\\d+(\\.\\d+)+)").matcher(folderName);
        return matcher.matches() ? matcher.group(2) : "";
    }

    // Compare two version strings (e.g., x.y.z) by numerical value
    private static int compareVersions(String v1, String v2) {
        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            if (v1Part < v2Part) return -1;
            if (v1Part > v2Part) return 1;
        }
        return 0;
    }

    // Recursively delete a directory
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    logger.fine("Deleting file: " + file.getPath());
                    file.delete();
                }
            }
        }
        logger.fine("Deleting directory: " + directory.getPath());
        directory.delete();
    }
}