package org.ovirt.engine.core.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class AppConfig extends ConfigFile<PropertiesConfiguration> {

    private static final String CONFIG_CONF = "engine-config.conf";
    private PropertiesConfiguration configFile;

    public AppConfig(String optionalConfigDir) throws FileNotFoundException, ConfigurationException {
        File file = locate(optionalConfigDir);
        configFile = new PropertiesConfiguration(file);
    }

    @Override
    public PropertiesConfiguration getFile() {
        return configFile;
    }

    @Override
    protected String[] getDefaultFileLocations() {
        return new String[] {new File(EngineConfig.DEFAULT_CONFIG_PATH, CONFIG_CONF).getAbsolutePath()};
    }
}
