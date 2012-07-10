package org.ovirt.engine.api.model;

public enum AccessProtocol {

    GLUSTER,
    NFS,
    CIFS;

    public String value() {
        return name().toLowerCase();
    }

    public static AccessProtocol fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
