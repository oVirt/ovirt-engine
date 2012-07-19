package org.ovirt.engine.ui.frontend.server.gwt.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.config.ConfigUtil;
import org.ovirt.engine.core.utils.LocalConfig;

/**
 * Reads, validates and stores UI plugin descriptor/configuration data.
 * <p>
 * Note that this class uses {@link LocalConfig} to read local (machine-specific) Engine configuration.
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

    private static final Logger logger = Logger.getLogger(PluginDataManager.class);

    /**
     * Returns UI plugin <em>data path</em>, under which UI plugin descriptor (JSON) files are placed.
     */
    public static String resolvePluginDataPath() {
        return ConfigUtil.resolvePath(LocalConfig.getInstance().getUsrDir().getAbsolutePath(), UI_PLUGIN_DIR);
    }

    /**
     * Returns UI plugin <em>config path</em>, under which UI plugin configuration (JSON) files are placed.
     */
    public static String resolvePluginConfigPath() {
        return ConfigUtil.resolvePath(LocalConfig.getInstance().getEtcDir().getAbsolutePath(), UI_PLUGIN_DIR);
    }

    private final File pluginDataDir;
    private final File pluginConfigDir;

    private final ObjectMapper mapper;

    // Cached plugin data, maps descriptor file names to corresponding object representations
    private final AtomicReference<Map<String, PluginData>> dataMapRef =
            new AtomicReference<Map<String, PluginData>>(new HashMap<String, PluginData>());

    private PluginDataManager(String pluginDataPath, String pluginConfigPath) {
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
        Map<String, PluginData> currentDataMapCopy = new HashMap<String, PluginData>(currentDataMapSnapshot);

        File[] descriptorFiles = pluginDataDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return isJsonFile(pathname);
            }
        });

        if (descriptorFiles == null) {
            logger.warn("Cannot list UI plugin descriptor files in [" + pluginDataDir.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        for (final File df : descriptorFiles) {
            final File cf = new File(pluginConfigDir, getConfigurationFileName(df));

            String descriptorFileName = df.getName();
            long descriptorLastModified = df.lastModified();
            long configurationLastModified = isJsonFile(cf) ? cf.lastModified() : -1L;

            // Check if data needs to be reloaded
            PluginData currentData = currentDataMapCopy.get(descriptorFileName);
            boolean reloadDescriptor, reloadConfiguration;
            if (currentDataMapCopy.containsKey(descriptorFileName)) {
                reloadDescriptor = descriptorLastModified > currentData.getDescriptorLastModified();
                reloadConfiguration = configurationLastModified > currentData.getConfigurationLastModified()
                        || reloadDescriptor;
            } else {
                reloadDescriptor = true;
                reloadConfiguration = true;
            }

            // Read descriptor data
            JsonNode descriptorNode = currentData != null ? currentData.getDescriptorNode() : null;
            if (reloadDescriptor) {
                logger.info("Reading UI plugin descriptor [" + df.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                descriptorNode = readJsonNode(df);
                if (descriptorNode == null) {
                    // Failed to read descriptor data, nothing we can do about it
                    continue;
                }
            } else if (descriptorNode == null) {
                logger.warn("UI plugin descriptor node is null for [" + df.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            }

            // Read configuration data
            JsonNode configurationNode = currentData != null ? currentData.getConfigurationNode() : null;
            if (reloadConfiguration) {
                logger.info("Reading UI plugin configuration [" + cf.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                configurationNode = readConfigurationNode(cf);
                if (configurationNode == null) {
                    // Failed to read configuration data, use empty object
                    configurationNode = mapper.getNodeFactory().objectNode();
                }
            } else if (configurationNode == null) {
                logger.warn("UI plugin configuration node is null for [" + cf.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
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
                        logger.warn("Validation error in [" + df.getAbsolutePath() + "]: " + message); //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    @Override
                    public void configurationError(String message) {
                        logger.warn("Validation error in [" + cf.getAbsolutePath() + "]: " + message); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                });
                if (!dataValid) {
                    // Data validation failed, nothing we can do about it
                    continue;
                }

                // Update local data mapping
                currentDataMapCopy.put(descriptorFileName, newData);
            }
        }

        // Apply changes through reference assignment
        if (!dataMapRef.compareAndSet(currentDataMapSnapshot, currentDataMapCopy)) {
            logger.warn("It seems that UI plugin data has changed, please reload WebAdmin application"); //$NON-NLS-1$
        }
    }

    boolean isJsonFile(File pathname) {
        return pathname.isFile() && pathname.canRead() && pathname.getName().endsWith(JSON_FILE_SUFFIX);
    }

    JsonNode readJsonNode(File file) {
        JsonNode node = null;
        try {
            node = mapper.readValue(file, JsonNode.class);
        } catch (IOException e) {
            logger.warn("Cannot read/parse JSON file [" + file.getAbsolutePath() + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return node;
    }

    JsonNode readConfigurationNode(File configurationFile) {
        return isJsonFile(configurationFile) ? readJsonNode(configurationFile) : null;
    }

    String getConfigurationFileName(File descriptorFile) {
        return descriptorFile.getName().replace(JSON_FILE_SUFFIX, CONFIG_FILE_SUFFIX);
    }

}
