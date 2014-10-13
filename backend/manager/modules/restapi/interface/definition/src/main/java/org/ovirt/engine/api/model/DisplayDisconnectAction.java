package org.ovirt.engine.api.model;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DisplayDisconnectAction {
    NONE,
    LOCK_SCREEN,
    LOGOUT,
    REBOOT,
    SHUTDOWN;

    private static final Logger log = LoggerFactory.getLogger(DisplayDisconnectAction.class);
    public String value() {
        return name().toLowerCase();
    }

    public static DisplayDisconnectAction fromValue(String value) {
        try {
            return StringUtils.isEmpty(value) ? LOCK_SCREEN : valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("The value \"" + value + "\" is not a valid console disconnect action", e);
            return null;
        }
    }
}
