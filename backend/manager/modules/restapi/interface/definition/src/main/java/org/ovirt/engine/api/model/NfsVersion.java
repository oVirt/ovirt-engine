package org.ovirt.engine.api.model;

public enum NfsVersion {
    AUTO,
    V3,
    V4,
    V4_1;

    public String value() {
        return name().toLowerCase();
    }

    public static NfsVersion fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
