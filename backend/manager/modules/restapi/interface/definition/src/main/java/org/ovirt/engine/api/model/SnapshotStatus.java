package org.ovirt.engine.api.model;

public enum SnapshotStatus {
    OK,
    LOCKED,
    IN_PREVIEW;

    public String value() {
        return name().toLowerCase();
    }

    public static SnapshotStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
