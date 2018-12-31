package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;

public enum DiskBackup {

    None("None"),
    Incremental("Incremental");

    private String name;

    DiskBackup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DiskBackup forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
