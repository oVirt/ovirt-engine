package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum FileTypeExtension {
    Unknown(0),
    ISO(1),
    Floppy(2),
    Disk(3),
    All(4);

    private int intValue;
    private static Map<Integer, FileTypeExtension> mappings;

    static {
        mappings = new HashMap<Integer, FileTypeExtension>();
        for (FileTypeExtension error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private FileTypeExtension(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static FileTypeExtension forValue(int value) {
        return mappings.get(value);
    }
}
