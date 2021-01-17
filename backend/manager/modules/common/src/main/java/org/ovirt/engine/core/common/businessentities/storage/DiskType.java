package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;

public enum DiskType {

    File("file"),
    Block("block");

    private String name;

    DiskType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DiskType forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
