package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VolumeFormat")
public enum VolumeFormat {
    // Added in order to keep the ordinal and array element values consistent
    UNUSED0(0), UNUSED1(1), UNUSED2(2),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Unassigned(3),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    COW(4),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    RAW(5);

    private int intValue;
    private static java.util.HashMap<Integer, VolumeFormat> mappings = new HashMap<Integer, VolumeFormat>();

    static {
        for (VolumeFormat volumeFormat : values()) {
            mappings.put(volumeFormat.getValue(), volumeFormat);
        }
    }

    private VolumeFormat(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VolumeFormat forValue(int value) {
        return mappings.get(value);
    }

}
