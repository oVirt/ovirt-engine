package org.ovirt.engine.api.model;

public enum AccessProtocol {

    GLUSTER,
    NFS,
    CIFS;

    public String value() {
        return name();
    }

    public static AccessProtocol fromValue(String v) {
        return valueOf(v);
    }

}
