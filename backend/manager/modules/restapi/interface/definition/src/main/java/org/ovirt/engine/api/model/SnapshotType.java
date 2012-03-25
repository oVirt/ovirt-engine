package org.ovirt.engine.api.model;

public enum SnapshotType {

    REGULAR,
    ACTIVE,
    STATELESS,
    PREVIEW;

    public String value() {
        return name().toLowerCase();
    }

    public static SnapshotType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
