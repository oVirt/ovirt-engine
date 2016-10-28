package org.ovirt.engine.core.common.config;

/**
 * Config Utils Interface
 */
public interface IConfigUtilsInterface {
    <T> T getValue(ConfigValues configValue, String version);
    void refresh();
}
