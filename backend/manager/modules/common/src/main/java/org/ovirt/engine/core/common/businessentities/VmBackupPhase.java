package org.ovirt.engine.core.common.businessentities;

import java.util.Arrays;

public enum VmBackupPhase {

    INITIALIZING("Initializing"),
    STARTING("Starting"),
    READY("Ready"),
    FINALIZING("Finalizing");

    private String name;

    VmBackupPhase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static VmBackupPhase forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
