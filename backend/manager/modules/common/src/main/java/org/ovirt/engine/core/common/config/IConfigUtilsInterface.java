package org.ovirt.engine.core.common.config;

import java.util.Map;

/**
 * Config Utils Interface
 */
public interface IConfigUtilsInterface {
    <T> T getValue(ConfigValues configValue, String version);
    void refresh();
    <T> Map<String, T> getValuesForAllVersions(ConfigValues configValue);
    boolean valueExists(ConfigValues configValue, String version);
}
