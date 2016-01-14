package org.ovirt.engine.core.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;

public abstract class ConfigFile<T> {

    public abstract T getFile();

    protected abstract String[] getDefaultFileLocations();

    protected File locate(String optionalConfigFilePath) throws FileNotFoundException {
        return EngineConfigUtils.locateFileInPaths(
                !StringUtils.isBlank(optionalConfigFilePath) ? new String[] { optionalConfigFilePath }
                        : getDefaultFileLocations());
    }

}
