package org.ovirt.engine.api.restapi.types;

public enum WatchdogModel {
    I6300ESB,
    IB700;
    public String value() {
        return this.name().toLowerCase();
    }

    public static WatchdogModel fromValue(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
