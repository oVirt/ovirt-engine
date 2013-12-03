package org.ovirt.engine.api.model;

public enum GlusterState {

    UP,
    DOWN,
    UNKNOWN;

    public String value() {
        return name().toLowerCase();
    }

    public static GlusterState fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
