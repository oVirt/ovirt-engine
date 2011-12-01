package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskType")
public enum DiskType {

    Unassigned(0),
    System(1),
    Data(2),
    Shared(3),
    Swap(4),
    Temp(5);

    private int intValue;
    private static Map<Integer, DiskType> mappings;

    static {
        mappings = new HashMap<Integer, DiskType>();
        for (DiskType error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private DiskType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static DiskType forValue(int value) {
        return mappings.get(value);
    }
}
