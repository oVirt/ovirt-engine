package org.ovirt.engine.core.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class AppConfig<T extends PropertiesConfiguration> extends ConfigFile<T> {

    private static final String RHEV_CONFIG_CONF = "engine-config.conf";
    private PropertiesConfiguration configFile;
    private final String[] defaultFileLocations = new String[] {
            System.getenv("PWD") + File.separator + RHEV_CONFIG_CONF,
            EngineConfig.DEFAULT_CONFIG_PATH + RHEV_CONFIG_CONF };

    public AppConfig(String optionalConfigDir) throws FileNotFoundException, ConfigurationException {
        File file = locate(optionalConfigDir);
        configFile = new PropertiesConfiguration(file);
    }

    @Override
    public T getFile() {
        return (T) configFile;
    }

    @Override
    protected String[] getDefaultFileLocations() {
        return defaultFileLocations;
    }
}
