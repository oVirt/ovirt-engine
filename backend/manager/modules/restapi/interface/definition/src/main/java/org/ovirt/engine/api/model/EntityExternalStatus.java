package org.ovirt.engine.api.model;

public enum EntityExternalStatus {
    OK,
    INFO,
    WARNING,
    ERROR,
    FAILURE;

    public String value() {
        return name().toLowerCase();
    }

    public static EntityExternalStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
