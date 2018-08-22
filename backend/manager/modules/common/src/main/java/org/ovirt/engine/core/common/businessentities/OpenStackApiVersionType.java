package org.ovirt.engine.core.common.businessentities;

public enum OpenStackApiVersionType implements Nameable {
    v3("v3"),
    v2_0("v2.0");

    private final String name;

    @Override
    public String getName() {
        return name;
    }

    public String getPath() {
        return "/" + getName();
    }

    OpenStackApiVersionType(String name) {
        this.name = name;
    }

}
