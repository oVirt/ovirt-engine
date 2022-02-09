package org.ovirt.engine.core.common.businessentities;

import java.util.Arrays;

public enum VmBackupPhase {

    INITIALIZING("Initializing"),
    CREATING_SCRATCH_DISKS("Creating scratch disks"),
    PREPARING_SCRATCH_DISK("Preparing scratch disks"),
    STARTING("Starting"),
    ADDING_BITMAPS("Adding volume bitmaps"),
    WAITING_FOR_BITMAPS("Waiting for bitmaps"),
    READY("Ready"),
    FINALIZING("Finalizing"),
    FINALIZING_FAILURE("Finalizing Failure"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed");

    private String name;

    VmBackupPhase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isBackupFinished() {
        return this == SUCCEEDED || this == FAILED;
    }

    public boolean isBackupFinalizing() {
        return this == FINALIZING || this == FINALIZING_FAILURE;
    }

    public static VmBackupPhase forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
