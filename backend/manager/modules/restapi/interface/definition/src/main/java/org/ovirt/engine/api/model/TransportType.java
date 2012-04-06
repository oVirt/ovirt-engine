package org.ovirt.engine.api.model;


public enum TransportType {

    TCP,
    RDMA;

    public String value() {
        return name();
    }

    public static TransportType fromValue(String v) {
        return valueOf(v);
    }

}
