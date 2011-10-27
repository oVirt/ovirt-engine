package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VolumeType")
public enum VolumeType {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Unassigned(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Preallocated(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Sparse(2);

    private int intValue;
    private static java.util.HashMap<Integer, VolumeType> mappings = new HashMap<Integer, VolumeType>();

    static {
        for (VolumeType volumeType : values()) {
            mappings.put(volumeType.getValue(), volumeType);
        }
    }

    private VolumeType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VolumeType forValue(int value) {
        return mappings.get(value);
    }

}
