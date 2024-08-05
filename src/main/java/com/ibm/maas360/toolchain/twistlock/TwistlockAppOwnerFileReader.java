package com.ibm.maas360.toolchain.twistlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TwistlockAppOwnerFileReader
{
    public static final String APP_OWNER_MAPPING_FILE = "AppOwners.log";

    private static final Logger logger = Logger.getLogger(TwistlockAppOwnerFileReader.class.getSimpleName());

    public static Map<String, String> loadAppOwnersFromLog(String filePath) throws IOException
    {
        logger.info("Reading app owners file at path " + filePath);
        // Replace this with your list of app names and owners
        Map<String, String> appOwners = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String appName = parts[0].trim();
                    String owner = parts[1].trim();
                    appOwners.put(appName, owner);
                } else {
                    logger.warning("Skipping invalid line in log file: " + line);
                }
            }
        }

        logger.info("Count of App Owners read " + appOwners.size());
        return appOwners;
    }
}
