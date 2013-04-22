package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum ImageStatus implements Identifiable {
    Unassigned(0),
    OK(1),
    LOCKED(2),
    INVALID(3),
    ILLEGAL(4);

    private int intValue;
    private static Map<Integer, ImageStatus> mappings = new HashMap<Integer, ImageStatus>();

    static {
        for (ImageStatus imageStatus : values()) {
            mappings.put(imageStatus.getValue(), imageStatus);
        }
    }

    private ImageStatus(int value) {
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
