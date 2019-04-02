package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum ImageTransferBackend implements Identifiable {
    FILE(0, "File"),
    NBD(1, "NBD");

    private int value;
    private String description;
    private static final Map<Integer, ImageTransferBackend> valueToPhase = new HashMap<>();

    static {
        for (ImageTransferBackend phase : values()) {
            valueToPhase.put(phase.getValue(), phase);
        }
    }

    ImageTransferBackend(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static ImageTransferBackend forValue(int value) {
        return valueToPhase.get(value);
    }

    @Override
    public String toString() {
        return description;
    }
}
