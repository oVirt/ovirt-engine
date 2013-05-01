package org.ovirt.engine.core.notifier.utils;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.notifier.NotificationServiceException;

/**
 * The <code>NotificationConfigurator</class> reads and stores properties from a configuration file.<br>
 * It provides simple properties querying for timer interval properties types, e.g. {@link #getTimerInterval(String)}
 */
public class NotificationConfigurator {

    private static final String DEFAULT_CONF_FILE_LOCATION = "/etc/engine/notifier/notifier.conf";
    private static final Logger log = Logger.getLogger(NotificationConfigurator.class);
    private Map<String, String> prop = null;

    /**
     * Creates a {@code NotificationConfigurator} by evaluating properties values from a given file.
     * @param confFile
     *            a path to the configuration file
     * @throws NotificationServiceException
     */
    public NotificationConfigurator(String confFile) throws NotificationServiceException {
        setConfigurationFile(confFile);
    }

    /**
     * Set local configuration file for the notification service override the default configuration file
     * @param localConfFile
     *            the path of the alternate configuration file
     * @throws NotificationServiceException
     */
    private void setConfigurationFile(String localConfFile) throws NotificationServiceException {
        String confFileLocation;
        if (StringUtils.isNotEmpty(localConfFile)) {
            confFileLocation = localConfFile;
            log.info("Starting event notification service with configuration file: " + confFileLocation);
        } else {
            confFileLocation = DEFAULT_CONF_FILE_LOCATION;
            log.info("Starting event notification service with default configuration file: " + confFileLocation);
        }
        File file = new File(confFileLocation);
        if (!file.canRead()) {
            throw new NotificationServiceException("Configuration file does not exist or missing permissions: "
                    + file.getAbsoluteFile());
        }
        prop = NotificationProperties.readPropertiesFile(confFileLocation);
    }

    /**
     * Returns properties which read from file.
     *
     * @return
     */
    public Map<String, String> getProperties() {
        return prop;
    }

    /**
     * Gets a value for timer interval by a given property name
     * @param intervalPropertyName
     *            an interval property key
     * @param defaultInterval
     *            a default interval value
     * @return an interval
     * @throws NotificationServiceException
     */
    public long getTimerInterval(String intervalPropertyName, long defaultInterval) throws NotificationServiceException {
        long interval;

        if (!prop.containsKey(intervalPropertyName)) {
            interval = defaultInterval;
            log.info(String.format("%s property is missing, using default %d.", intervalPropertyName, defaultInterval));
        } else {
            try {
                interval = extractNumericProperty(prop.get(intervalPropertyName));
            } catch (NumberFormatException e) {
                throw new NotificationServiceException("Invalid format for property: " + intervalPropertyName, e);
            }
        }
        return interval;
    }

    /**
     * Extract a positive numeric property out of a property value
     * @param intervalProp property value
     * @return a numeric value represents the property
     * @throws NotificationServiceException
     */
    static public int extractNumericProperty(String intervalProp) throws NumberFormatException {
        int interval = Integer.valueOf(intervalProp);
        if (interval <= 0) {
            throw new NumberFormatException(String.format("[%s] value should be a positive number", intervalProp));
        }
        return interval;
    }
}
