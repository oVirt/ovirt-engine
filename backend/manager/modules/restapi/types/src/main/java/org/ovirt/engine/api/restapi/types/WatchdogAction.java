package org.ovirt.engine.api.restapi.types;

public enum WatchdogAction {
    NONE,
    RESET,
    POWEROFF,
    PAUSE,
    DUMP;
    public String value() {
        return this.name().toLowerCase();
    }

    public static WatchdogAction fromValue(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
