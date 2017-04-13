package org.ovirt.engine.core.utils.osinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.osinfo.MapBackedPreferences;
import org.ovirt.engine.core.utils.OsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OsInfoPreferencesLoader implements OsLoader {

    INSTANCE;

    /**
     * The source properties from which all values are loaded.
     */
    private Properties properties;
    /**
     * The configuration tree. See {@link java.util.prefs.Preferences}
     */
    private MapBackedPreferences preferences = new MapBackedPreferences(null, "");

    private Logger log = LoggerFactory.getLogger(OsInfoPreferencesLoader.class);

    public void init(Path directoryPath) {
        load(directoryPath);
        loadPreferencesFromProperties();
    }

    void load(Path directoryPath) {
        File dir = directoryPath.toFile();
        if (dir.exists()) {
            File[] files = directoryPath.toFile().listFiles((dir1, name) -> name.endsWith(".properties"));

            // load files by name order. 00-osinfo.properties should be loaded first
            if (files != null) {
                Arrays.sort(files);

                for (File file : files) {
                    log.info("Loading file '{}'", file.getPath());
                    OsinfoPropertiesParser.parse(file.getAbsolutePath());
                    loadFile(file.toPath());
                }
            }
        } else {
            log.error("Directory '{}' doesn't exist.", dir.getPath());
        }

    }

    @Override
    public MapBackedPreferences getPreferences() {
        return preferences;
    }

    private void loadFile(Path path) {
        try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties = new Properties(properties);
            properties.load(reader);
        } catch (IOException e) {
            log.error("Failed loading file '{}': {}", path, e.getMessage());
            log.debug("Exception", e);
        }

    }

    /**
     * convert the property key namepaces (key.name.space) to the hierarchical preferences node<br>
     * is done by breaking each dot separated part and creating a node out of it.<br>
     * The last node is being set with the value. Versioned value path are denoted as "key.name.space.value.x.y" and non<br>
     * versioned value are just "key.name.space.value" or just "key.name.space"<br>
     */
    private void loadPreferencesFromProperties() {
        if (properties != null) {
            for (String propertyKey : properties.stringPropertyNames()) {
                Preferences node = preferences;
                Iterator<String> iterator = Arrays.asList(propertyKey.split("\\.|\\.value")).iterator();
                String key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    // create a node from each dot separated part of the key
                    // if its "value" joining the reset of the iterator so "value" or "value.3.1" are
                    // considered as the leaf and should be set with the designated value
                    if (key.startsWith("value")) {
                        key = iterator.hasNext() ? key + "." + StringUtils.join(iterator, ".") : key;
                        break;
                    } else if (!iterator.hasNext()) {
                        // its a key without a multiple version values
                        break;
                    } else {
                        node = node.node(key);
                    }
                }
                putValue(node, key, properties.getProperty(propertyKey));
            }
        }
        // properties isn't needed anymore
        properties = null;
    }

    private void putValue(Preferences node, String key, String value) {
        node.put(key, value);
    }

}
