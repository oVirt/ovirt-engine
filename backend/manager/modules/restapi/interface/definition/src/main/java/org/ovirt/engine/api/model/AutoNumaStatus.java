package org.ovirt.engine.api.model;

public enum AutoNumaStatus {
    DISABLE,
    ENABLE,
    UNKNOWN;

    public String value() {
        return name().toLowerCase();
    }

    public static AutoNumaStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
