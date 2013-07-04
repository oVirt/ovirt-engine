package org.ovirt.engine.api.model;

public enum HookContentType {
    TEXT,
    BINARY;

    public String value() {
        return name().toLowerCase();
    }

    public static HookContentType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
