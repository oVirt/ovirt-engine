package org.ovirt.engine.api.model;

public enum HookStatus {
    ENABLED,
    DISABLED,
    MISSING;

    public String value() {
        return name().toLowerCase();
    }

    public static HookStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
