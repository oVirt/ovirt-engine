package org.ovirt.engine.core.common.config;

/**
 * Config Utils Interface
 */
public interface IConfigUtilsInterface {

    /**
     * Sets the string value.
     *
     * @param name
     *            The name.
     * @param value
     *            The value.
     */
    void setStringValue(String name, String value);


    <T> T getValue(ConfigValues configValue, String version);

}
