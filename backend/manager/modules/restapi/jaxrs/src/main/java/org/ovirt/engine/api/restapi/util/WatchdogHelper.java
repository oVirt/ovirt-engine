package org.ovirt.engine.api.restapi.util;

import org.ovirt.engine.api.model.Watchdog;

public class WatchdogHelper {

    private WatchdogHelper() { }

    /**
     * Check if the watchdog is null or has no fields set.
     *
     * @param watchdog the {@link Watchdog} object to check
     * @return {@code true} if the watchdog is {@code null} or all of the fields
     *         (action, model, id) are unset; {@code false} otherwise
     */
    public static boolean isWatchdogEmpty(Watchdog watchdog) {
        return watchdog == null || !watchdog.isSetAction() && !watchdog.isSetModel() && !watchdog.isSetId();
    }
}
