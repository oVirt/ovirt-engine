package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CopyVolumeType")
public enum CopyVolumeType {
    SharedVol(6), // (for template)
    InternalVol(7),
    LeafVol(8); // collapse without marking as template

    private int intValue;
    private static java.util.HashMap<Integer, CopyVolumeType> mappings;

    private synchronized static java.util.HashMap<Integer, CopyVolumeType> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, CopyVolumeType>();
        }
        return mappings;
    }

    private CopyVolumeType(int value) {
        intValue = value;
        CopyVolumeType.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static CopyVolumeType forValue(int value) {
        return getMappings().get(value);
    }
}
