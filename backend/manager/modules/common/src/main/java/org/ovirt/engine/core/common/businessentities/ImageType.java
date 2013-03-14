package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum ImageType {
    Unknown(0),
    ISO(1),
    Floppy(2),
    Disk(3),
    All(4);

    private int intValue;
    private static Map<Integer, ImageType> mappings;

    static {
        mappings = new HashMap<Integer, ImageType>();
        for (ImageType error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private ImageType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static ImageType forValue(int value) {
        return mappings.get(value);
    }
}
