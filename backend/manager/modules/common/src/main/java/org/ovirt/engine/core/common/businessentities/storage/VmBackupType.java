package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;

public enum VmBackupType {
    Live("live"),
    Cold("cold"),
    Hybrid("hybrid");

    private String name;

    VmBackupType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static VmBackupType forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
