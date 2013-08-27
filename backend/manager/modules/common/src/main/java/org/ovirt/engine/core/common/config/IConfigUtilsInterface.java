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
     * @return
     */
    boolean getBoolValue(String name, String defaultValue);

    /**
     * Get map value.
     *
     * @param name  the name of of the config value
     * @param defaultValue      default value
     * @return
     */
    Map<String, String> getMapValue(String name, String defaultValue);

    /**
     * Gets the int value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return
     */
    int getIntValue(String name, String defaultValue);

    /**
     * Gets the date time value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return
     */
    Date getDateTimeValue(String name, String defaultValue);

    /**
     * Gets the time span value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return
     */
    TimeSpan getTimeSpanValue(String name, String defaultValue);

    /**
     * Gets the version value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return
     */
    Version getVersionValue(String name, String defaultValue);

    /**
     * Gets the path value.
     *
     * @param name
     *            The name.
     * @param defaultValue
     *            The default value.
     * @return
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
