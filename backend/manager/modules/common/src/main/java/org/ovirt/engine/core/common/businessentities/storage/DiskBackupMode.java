package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;

public enum DiskBackupMode {

    Full("full"),
    Incremental("incremental");

    private String name;

    DiskBackupMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DiskBackupMode forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
