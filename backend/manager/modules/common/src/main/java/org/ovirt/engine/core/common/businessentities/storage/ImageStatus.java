package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum ImageStatus implements Identifiable {
    Unassigned(0),
    OK(1),
    LOCKED(2),
    ILLEGAL(4);

    private int intValue;
    private static final Map<Integer, ImageStatus> mappings = new HashMap<>();

    static {
        for (ImageStatus imageStatus : values()) {
            mappings.put(imageStatus.getValue(), imageStatus);
        }
    }

    ImageStatus(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static ImageStatus forValue(int value) {
        return mappings.get(value);
    }
}
