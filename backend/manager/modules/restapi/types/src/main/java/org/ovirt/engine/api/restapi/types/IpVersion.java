package org.ovirt.engine.api.restapi.types;

public enum IpVersion {
    V4,
    V6;

    public String value() {
        return name().toLowerCase();
    }

    public static IpVersion fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
