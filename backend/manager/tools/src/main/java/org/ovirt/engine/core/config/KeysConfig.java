package org.ovirt.engine.core.config;

import static org.ovirt.engine.core.config.EngineConfig.DEFAULT_CONFIG_PATH;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

public class KeysConfig<T extends HierarchicalConfiguration> extends ConfigFile<T> {

    private static final String OVIRT_CONFIG_PROPERTIES = "engine-config.properties";
    private T configFile;
    private final String[] defaultFileLocations = {
            new File(DEFAULT_CONFIG_PATH, OVIRT_CONFIG_PROPERTIES).getAbsolutePath(),
            new File(DEFAULT_CONFIG_PATH, "engine-config_" + Locale.getDefault() + ".properties").getAbsolutePath()
    };

    public KeysConfig(String optionalConfigPath) throws FileNotFoundException, ConfigurationException {
        File file = locate(optionalConfigPath);
        configFile =
                (T) ConfigurationUtils.convertToHierarchical(new PropertiesConfiguration(file));
        configFile.setExpressionEngine(new XPathExpressionEngine());
    }

    @Override
    public T getFile() {
        return configFile;
    }

    @Override
    public String[] getDefaultFileLocations() {
        return defaultFileLocations;
    }

}
