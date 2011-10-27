package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FileTypeExtension")
public enum FileTypeExtension {
    Unknown(0),
    ISO(1),
    Floppy(2);

    private int intValue;
    private static HashMap<Integer, FileTypeExtension> mappings;

    private synchronized static Map<Integer, FileTypeExtension> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, FileTypeExtension>();
        }
        return mappings;
    }

    private FileTypeExtension(int value) {
        intValue = value;
        FileTypeExtension.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static FileTypeExtension forValue(int value) {
        return values()[value];
    }
}
