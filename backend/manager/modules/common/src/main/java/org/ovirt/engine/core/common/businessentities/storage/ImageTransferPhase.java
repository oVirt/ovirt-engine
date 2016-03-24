package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum ImageTransferPhase implements Identifiable {
    // Note: when adding/changing values, add text lookup to DiskUploadImageProgressColumn
    UNKNOWN(0, "Unknown"),
    INITIALIZING(1, "Initializing"),
    TRANSFERRING(2, "Transferring"),
    RESUMING(3, "Resuming"),
    PAUSED_SYSTEM(4, "Paused by System"),
    PAUSED_USER(5, "Paused by User"),
    CANCELLED(6, "Cancelled"),
    FINALIZING_SUCCESS(7, "Finalizing Success"),
    FINALIZING_FAILURE(8, "Finalizing Failure"),
    FINISHED_SUCCESS(9, "Finished Success"),
    FINISHED_FAILURE(10, "Finished Failure");

    private int value;
    private String description;
    private static final HashMap<Integer, ImageTransferPhase> valueToPhase = new HashMap<>();

    static {
        for (ImageTransferPhase phase : values()) {
            valueToPhase.put(phase.getValue(), phase);
        }
    }

    private ImageTransferPhase(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static ImageTransferPhase forValue(int value) {
        return valueToPhase.get(value);
    }

    @Override
    public String toString() {
        return description;
    }

    public boolean canBePaused() {
        return this == INITIALIZING || this == RESUMING || this == TRANSFERRING;
    }

    public boolean canBeResumed() {
        return this == PAUSED_SYSTEM || this == PAUSED_USER;
    }

    public boolean canBeCancelled() {
        return canBePaused() || canBeResumed();
    }
}
