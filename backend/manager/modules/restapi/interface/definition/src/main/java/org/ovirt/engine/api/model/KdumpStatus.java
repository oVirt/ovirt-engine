package org.ovirt.engine.api.model;

public enum KdumpStatus {
    UNKNOWN, DISABLED, ENABLED;

    public String value() {
        return name().toLowerCase();
    }

    public static KdumpStatus fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
