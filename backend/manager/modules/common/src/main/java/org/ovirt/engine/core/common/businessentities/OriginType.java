package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "OriginType")
public enum OriginType {
    ENGINE(0),
    VMWARE(1),
    XEN(2);

    private int intValue;
    private static Map<Integer, OriginType> mappings;

    static {
        mappings = new HashMap<Integer, OriginType>();
        for (OriginType error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private OriginType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static OriginType forValue(int value) {
        return mappings.get(value);
    }
}
