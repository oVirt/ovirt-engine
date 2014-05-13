package org.ovirt.engine.api.model;

public enum SELinuxMode {
    ENFORCING, PERMISSIVE, DISABLED;

    public String value() {
        return name().toLowerCase();
    }

    public static SELinuxMode fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
