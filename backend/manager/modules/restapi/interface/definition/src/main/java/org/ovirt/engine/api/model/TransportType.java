package org.ovirt.engine.api.model;


public enum TransportType {

    TCP,
    RDMA;

    public String value() {
        return name().toLowerCase();
    }

    public static TransportType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
