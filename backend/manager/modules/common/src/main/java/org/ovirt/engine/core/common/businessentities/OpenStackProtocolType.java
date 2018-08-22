package org.ovirt.engine.core.common.businessentities;

public enum OpenStackProtocolType implements Nameable {
    https("HTTPS"),
    http("HTTP");

    private final String name;

    @Override
    public String getName() {
        return name;
    }

    OpenStackProtocolType(String name) {
        this.name = name;
    }
}
