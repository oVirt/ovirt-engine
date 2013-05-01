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

    private static final Logger log = Logger.getLogger(NotificationConfigurator.class);
    private Map<String, String> prop = null;

    public NotificationConfigurator() {
        prop = NotificationProperties.getInstance().getProperties();
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
