package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum ImageTransferPhase implements Identifiable {
    // Note: when adding/changing values, add text lookup to DiskTransferProgressColumn
    UNKNOWN(0, "Unknown"),
    INITIALIZING(1, "Initializing"),
    TRANSFERRING(2, "Transferring"),
    RESUMING(3, "Resuming"),
    PAUSED_SYSTEM(4, "Paused by System"),
    PAUSED_USER(5, "Paused by User"),
    CANCELLED_SYSTEM(6, "Cancelled"),
    FINALIZING_SUCCESS(7, "Finalizing Success"),
    FINALIZING_FAILURE(8, "Finalizing Failure"),
    FINISHED_SUCCESS(9, "Finished Success"),
    FINISHED_FAILURE(10, "Finished Failure"),
    CANCELLED_USER(11, "Cancelled by user"),
    FINALIZING_CLEANUP(12, "Finalizing Cleanup"),
    FINISHED_CLEANUP(13, "Finished Cleanup");

    private int value;
    private String description;
    private static final Map<Integer, ImageTransferPhase> valueToPhase = new HashMap<>();

    // TODO: revisit this in the future to validate all transitions
    // private static final EnumMap<ImageTransferPhase, EnumSet<ImageTransferPhase>> validTransitions = new EnumMap<>(ImageTransferPhase.class);

    static {
        for (ImageTransferPhase phase : values()) {
            valueToPhase.put(phase.getValue(), phase);
        }

        // TODO: revisit this in the future to validate all transitions
        //        validTransitions.put(INITIALIZING, EnumSet.of(TRANSFERRING, PAUSED_SYSTEM, CANCELLED_SYSTEM));
        //        validTransitions.put(TRANSFERRING, EnumSet.of(FINALIZING_SUCCESS, PAUSED_SYSTEM, CANCELLED_SYSTEM, CANCELLED_USER, PAUSED_USER));
        //        validTransitions.put(RESUMING, EnumSet.of(TRANSFERRING, PAUSED_SYSTEM, CANCELLED_SYSTEM));
        //        validTransitions.put(PAUSED_SYSTEM, EnumSet.of(RESUMING, CANCELLED_USER, CANCELLED_SYSTEM));
        //        validTransitions.put(PAUSED_USER, EnumSet.of(RESUMING, CANCELLED_USER, CANCELLED_SYSTEM));
        //        validTransitions.put(CANCELLED_USER, EnumSet.of(FINALIZING_CLEANUP));
        //        validTransitions.put(CANCELLED_SYSTEM, EnumSet.of(FINALIZING_FAILURE));
        //        validTransitions.put(FINALIZING_SUCCESS, EnumSet.of(FINALIZING_FAILURE, FINISHED_SUCCESS));
        //        validTransitions.put(FINALIZING_FAILURE, EnumSet.of(FINISHED_FAILURE));
        //        validTransitions.put(FINALIZING_CLEANUP, EnumSet.of(FINISHED_CLEANUP));
        //        validTransitions.put(FINISHED_FAILURE, EnumSet.of(FINISHED_FAILURE));
        //        validTransitions.put(FINISHED_SUCCESS, EnumSet.of(FINISHED_SUCCESS));

    }

    ImageTransferPhase(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static boolean isValidTransition(ImageTransferPhase from, ImageTransferPhase to) {
        return !from.isFinished();
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

    public boolean isPaused() {
        return this == PAUSED_SYSTEM || this == PAUSED_USER;
    }

    public boolean canBeCancelled() {
        return canBePaused() || isPaused();
    }

    public boolean isFinished() {
        return this == FINISHED_FAILURE || this == FINISHED_SUCCESS;
    }
}
