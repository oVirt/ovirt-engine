package org.ovirt.engine.api.model;

public enum HostProtocol {
    XML,
    STOMP,
    AMQP;

    public String value() {
        return name().toLowerCase();
    }

    public static HostProtocol fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
