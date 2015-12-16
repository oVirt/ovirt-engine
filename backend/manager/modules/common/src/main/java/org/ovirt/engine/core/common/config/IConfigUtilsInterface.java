package org.ovirt.engine.core.common.config;

import java.util.Date;
import java.util.Map;

import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.compat.Version;

/**
 * Config Utils Interface
 */
public interface IConfigUtilsInterface {

    /**
     * Gets the bool value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            if set to <c>true</c> [default value].
     */
    boolean getBoolValue(String name, String defaultValue);

    /**
     * Get map value.
     *
     * @param name  the name of of the config value
     * @param defaultValue      default value
     */
    Map<String, String> getMapValue(String name, String defaultValue);

    /**
     * Gets the int value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     */
    int getIntValue(String name, String defaultValue);

    /**
     * Gets the date time value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     */
    Date getDateTimeValue(String name, String defaultValue);

    /**
     * Gets the time span value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     */
    TimeSpan getTimeSpanValue(String name, String defaultValue);

    /**
     * Gets the version value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     */
    Version getVersionValue(String name, String defaultValue);

    /**
     * Gets the path value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     */
    String getPathValue(String name, String defaultValue);

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
