package org.ovirt.engine.ui.frontend.server.gwt.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.ovirt.engine.core.common.config.ConfigUtil;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Reads, validates and stores UI plugin descriptor/configuration data.
 * <p>
 * Note that this class uses {@link EngineLocalConfig} to read local (machine-specific) Engine configuration.
 */
public class PluginDataManager {

    // Using 'initialization-on-demand holder' pattern
    private static class Holder {
        private static final PluginDataManager INSTANCE = new PluginDataManager(
                PluginDataManager.resolvePluginDataPath(), PluginDataManager.resolvePluginConfigPath());
    }

    private static final String UI_PLUGIN_DIR = "ui-plugins"; //$NON-NLS-1$
    private static final String JSON_FILE_SUFFIX = ".json"; //$NON-NLS-1$
    private static final String CONFIG_FILE_SUFFIX = "-config" + JSON_FILE_SUFFIX; //$NON-NLS-1$

    private static final long MISSING_FILE_LAST_MODIFIED = -1L;

    private static final Logger log = LoggerFactory.getLogger(PluginDataManager.class);

    /**
     * Returns UI plugin <em>data path</em>, under which UI plugin descriptor (JSON) files are placed.
     */
    public static String resolvePluginDataPath() {
        return ConfigUtil.resolvePath(EngineLocalConfig.getInstance().getUsrDir().getAbsolutePath(), UI_PLUGIN_DIR);
    }

    /**
     * Returns UI plugin <em>config path</em>, under which UI plugin configuration (JSON) files are placed.
     */
    public static String resolvePluginConfigPath() {
        return ConfigUtil.resolvePath(EngineLocalConfig.getInstance().getEtcDir().getAbsolutePath(), UI_PLUGIN_DIR);
    }

    private final File pluginDataDir;
    private final File pluginConfigDir;

    private final ObjectMapper mapper;

    // Cached plugin data, maps descriptor file names to corresponding object representations
    private final AtomicReference<Map<String, PluginData>> dataMapRef;

    private PluginDataManager(String pluginDataPath, String pluginConfigPath) {
        Map<String, PluginData> map = new HashMap<>();
        this.dataMapRef = new AtomicReference<>(map);
        this.pluginDataDir = new File(pluginDataPath);
        this.pluginConfigDir = new File(pluginConfigPath);
        this.mapper = createJsonMapper();
    }

    ObjectMapper createJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        return mapper;
    }

    public static PluginDataManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Calling this method is equivalent to:
     * <pre>
     * reloadData();
     * return getCurrentData();
     * </pre>
     */
    public Collection<PluginData> reloadAndGetCurrentData() {
        reloadData();
        return getCurrentData();
    }

    /**
     * Returns the currently valid descriptor/configuration data as unmodifiable collection.
     * <p>
     * Use {@link #reloadAndGetCurrentData} in case the caller expects 'recently-up-to-date' data.
     */
    public Collection<PluginData> getCurrentData() {
        return Collections.unmodifiableCollection(dataMapRef.get().values());
    }

    /**
     * Reloads descriptor/configuration data from local file system if necessary.
     * <p>
     * No attempts are made with regard to lock-based synchronization. The 'live' data is updated atomically through
     * {@linkplain AtomicReference#compareAndSet conditional reference re-assignment}. It may happen that another thread
     * has already updated 'live' data, in which case the current thread does nothing. This is offset by better
     * performance, assuming that the caller doesn't necessarily need to have 'completely-up-to-date' data at the given
     * point in time (having 'recently-up-to-date', but consistent data, should be enough).
     */
    public void reloadData() {
        // Get a snapshot of current data mappings
        Map<String, PluginData> currentDataMapSnapshot = dataMapRef.get();

        // Create a local working copy of current data mappings (avoid modifying 'live' data)
        Map<String, PluginData> currentDataMapCopy = new HashMap<>(currentDataMapSnapshot);

        File[] descriptorFiles = pluginDataDir.listFiles(pathname -> isJsonFile(pathname));

        if (descriptorFiles == null) {
            log.warn("Cannot list UI plugin descriptor files in '{}'", pluginDataDir.getAbsolutePath()); //$NON-NLS-1$
            return;
        }

        // Reload descriptor/configuration data
        reloadData(descriptorFiles, currentDataMapCopy);

        // Apply changes through reference assignment
        if (!dataMapRef.compareAndSet(currentDataMapSnapshot, currentDataMapCopy)) {
            log.warn("It seems that UI plugin data has changed, please reload WebAdmin application"); //$NON-NLS-1$
        }
    }

    void reloadData(File[] descriptorFiles, Map<String, PluginData> currentDataMapCopy) {
        Map<String, PluginData> entriesToUpdate = new HashMap<>();
        Set<String> keysToRemove = new HashSet<>();

        // Optimization: make sure we don't check data that we already processed
        Set<String> keysToCheckForRemoval = new HashSet<>(currentDataMapCopy.keySet());

        // Compare (possibly added or modified) files against cached data
        for (final File df : descriptorFiles) {
            final File cf = new File(pluginConfigDir, getConfigurationFileName(df));

            String descriptorFilePath = df.getAbsolutePath();
            PluginData currentData = currentDataMapCopy.get(descriptorFilePath);

            long descriptorLastModified = df.lastModified();
            long configurationLastModified = isJsonFile(cf) ? cf.lastModified() : MISSING_FILE_LAST_MODIFIED;

            // Check if data needs to be reloaded
            boolean reloadDescriptor;
            boolean reloadConfiguration;
            if (currentDataMapCopy.containsKey(descriptorFilePath)) {
                reloadDescriptor = descriptorLastModified > currentData.getDescriptorLastModified();
                reloadConfiguration = configurationLastModified > currentData.getConfigurationLastModified();

                // Change in descriptor causes reload of configuration
                reloadConfiguration |= reloadDescriptor;

                // Refresh configuration if the corresponding file has gone missing
                reloadConfiguration |= configurationLastModified == MISSING_FILE_LAST_MODIFIED
                        && currentData.getConfigurationLastModified() != MISSING_FILE_LAST_MODIFIED;
            } else {
                reloadDescriptor = true;
                reloadConfiguration = true;
            }

            // Read descriptor data
            JsonNode descriptorNode = currentData != null ? currentData.getDescriptorNode() : null;
            if (reloadDescriptor) {
                log.info("Reading UI plugin descriptor '{}'", df.getAbsolutePath()); //$NON-NLS-1$
                descriptorNode = readJsonNode(df);
                if (descriptorNode == null) {
                    // Failed to read descriptor data, nothing we can do about it
                    continue;
                }
            } else if (descriptorNode == null) {
                log.warn("UI plugin descriptor node is null for '{}'", df.getAbsolutePath()); //$NON-NLS-1$
                continue;
            }

            // Read configuration data
            JsonNode configurationNode = currentData != null ? currentData.getConfigurationNode() : null;
            if (reloadConfiguration) {
                log.info("Reading UI plugin configuration '{}'", cf.getAbsolutePath()); //$NON-NLS-1$
                configurationNode = readConfigurationNode(cf);
                if (configurationNode == null) {
                    // Failed to read configuration data, use empty object
                    configurationNode = createEmptyObjectNode();
                }
            } else if (configurationNode == null) {
                log.warn("UI plugin configuration node is null for '{}'", cf.getAbsolutePath()); //$NON-NLS-1$
                continue;
            }

            // Update data
            if (reloadDescriptor || reloadConfiguration) {
                PluginData newData = new PluginData(descriptorNode, descriptorLastModified,
                        configurationNode, configurationLastModified, mapper.getNodeFactory());

                // Validate data
                boolean dataValid = newData.validate(new PluginData.ValidationCallback() {
                    @Override
                    public void descriptorError(String message) {
                        log.warn("Validation error in '{}': {}", df.getAbsolutePath(), message); //$NON-NLS-1$
                    }

                    @Override
                    public void configurationError(String message) {
                        log.warn("Validation error in '{}': {}", cf.getAbsolutePath(), message); //$NON-NLS-1$
                    }
                });
                if (!dataValid) {
                    // Data validation failed, nothing we can do about it
                    continue;
                }

                entriesToUpdate.put(descriptorFilePath, newData);
            }

            keysToCheckForRemoval.remove(descriptorFilePath);
        }

        // Compare cached data against (possibly missing) files
        for (String descriptorFilePath : keysToCheckForRemoval) {
            File df = new File(descriptorFilePath);

            if (!df.exists()) {
                // Descriptor data file has gone missing
                keysToRemove.add(descriptorFilePath);
            }
        }

        // Perform data updates
        currentDataMapCopy.putAll(entriesToUpdate);
        currentDataMapCopy.keySet().removeAll(keysToRemove);
    }

    boolean isJsonFile(File pathname) {
        return pathname.isFile() && pathname.canRead() && pathname.getName().endsWith(JSON_FILE_SUFFIX);
    }

    JsonNode readJsonNode(File file) {
        JsonNode node = null;
        try {
            node = mapper.readValue(file, JsonNode.class);
        } catch (IOException e) {
            log.warn("Cannot read/parse JSON file '{}': {}", file.getAbsolutePath(), e.getMessage()); //$NON-NLS-1$
            log.debug("Exception", e); // $NON-NLS-1$
        }
        return node;
    }

    JsonNode readConfigurationNode(File configurationFile) {
        return isJsonFile(configurationFile) ? readJsonNode(configurationFile) : null;
    }

    String getConfigurationFileName(File descriptorFile) {
        return descriptorFile.getName().replace(JSON_FILE_SUFFIX, CONFIG_FILE_SUFFIX);
    }

    ObjectNode createEmptyObjectNode() {
        return mapper.getNodeFactory().objectNode();
    }

}
