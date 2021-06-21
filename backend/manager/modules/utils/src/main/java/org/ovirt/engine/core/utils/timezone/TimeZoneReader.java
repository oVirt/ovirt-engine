package org.ovirt.engine.core.utils.timezone;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TimeZoneReader {

    INSTANCE;

    public final String DIR_NAME = "timezones";

    private final Logger log = LoggerFactory.getLogger(TimeZoneReader.class);

    private Properties properties;

    // zone id to display name + offset
    private final Map<String, String> generalTimeZones = new HashMap<>();

    // windows zone id to display name + offset
    private final Map<String, String> windowsTimeZones = new HashMap<>();

    // windows zone id to java zone id
    private final Map<String, String> windowsToJavaTimeZones = new HashMap<>();

    public Properties getProperties() {
        return properties;
    }

    public Map<String, String> getGeneralTimezones() {
        return generalTimeZones;
    }

    public Map<String, String> getWindowsTimezones() {
        return windowsTimeZones;
    }

    public Map<String, String> getWindowsToJavaTimezones() {
        return windowsToJavaTimeZones;
    }

    private void loadPropertiesFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties = new Properties(properties);
            properties.load(reader);
        } catch (IOException e) {
            log.error("Failed loading '{}': {}", path, e.getMessage());
            log.debug("Exception", e);
        }
    }

    // this function returns timezone offset without DST with correct format
    private String getOffset(String timeZoneId) throws ZoneRulesException {
        ZoneId zone = ZoneId.of(timeZoneId);
        ZoneRules rules = zone.getRules();
        ZoneOffset standardOffset = rules.getStandardOffset(Instant.now());
        return String.format("(GMT%s)", standardOffset.toString());
    }

    private void initTimeZone(String javaTimeZoneId, String displayName) {
        try {
            String offset = getOffset(javaTimeZoneId);
            String timezoneOffsetWithDisplayName = String.format("%1$s %2$s", offset, displayName);
            generalTimeZones.put(javaTimeZoneId, timezoneOffsetWithDisplayName);
            windowsTimeZones.put(displayName, timezoneOffsetWithDisplayName);
            windowsToJavaTimeZones.put(displayName, javaTimeZoneId);
        } catch (ZoneRulesException e) {
            log.debug("failed to get time zone offset: {}", e.getMessage());
        }
    }

    private void parsePropertiesFile() {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String javaTimeZoneId = (String) entry.getKey();
            String displayName = (String) entry.getValue();
            if (displayName != null) {
                initTimeZone(javaTimeZoneId, displayName);
            } else {
                log.warn("failed to get display name of time zone id: {}", javaTimeZoneId);
            }
        }
    }

    private void loadFilesByOrder(File[] files) {
        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                log.info("Loading file '{}'", file.getPath());
                loadPropertiesFile(file.toPath());
                parsePropertiesFile();
            }
        }
    }

    public void init(Path directoryPath) {
        File dir = directoryPath.toFile();
        if (dir.exists()) {
            File[] files = directoryPath.toFile().listFiles((dir1, name) -> name.endsWith(".properties"));
            // load files by name order. 00-timezone.properties should be loaded first
            loadFilesByOrder(files);
        } else {
            log.error("Directory '{}' doesn't exist.", dir.getPath());
        }
    }
}
