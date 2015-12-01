package org.ovirt.engine.core.aaa;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

/**
 * A directory entry is available if it was found in the directory during the last check performed by the engine.
 */
public enum DirectoryEntryStatus implements Identifiable {
    UNAVAILABLE(0),
    AVAILABLE(1);

    private int value;

    private static Map<Integer, DirectoryEntryStatus> mappings;

    static {
        mappings = new HashMap<>();
        for (DirectoryEntryStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private DirectoryEntryStatus(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static DirectoryEntryStatus forValue(int value) {
        return mappings.get(value);
    }
}
