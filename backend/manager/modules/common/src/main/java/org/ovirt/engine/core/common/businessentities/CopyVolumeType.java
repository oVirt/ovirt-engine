package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CopyVolumeType")
public enum CopyVolumeType {
    SharedVol(6), // (for template)
    InternalVol(7),
    LeafVol(8); // collapse without marking as template

    private int intValue;
    private static Map<Integer, CopyVolumeType> mappings = new HashMap<Integer, CopyVolumeType>();

    static {
        for (CopyVolumeType action : values()) {
            mappings.put(action.getValue(), action);
        }
    }

    private CopyVolumeType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static CopyVolumeType forValue(int value) {
        return mappings.get(value);
    }
}
