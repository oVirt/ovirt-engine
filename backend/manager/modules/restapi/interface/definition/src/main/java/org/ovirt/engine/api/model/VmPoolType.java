package org.ovirt.engine.api.model;

public enum VmPoolType {

    AUTOMATIC,
    MANUAL;

    public String value() {
        return name().toLowerCase();
    }

    public static VmPoolType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
