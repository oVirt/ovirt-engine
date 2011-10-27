package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskType")
public enum DiskType {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Unassigned(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    System(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Data(2),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Shared(3),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Swap(4),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Temp(5);

    private int intValue;
    private static java.util.HashMap<Integer, DiskType> mappings;

    private synchronized static java.util.HashMap<Integer, DiskType> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, DiskType>();
        }
        return mappings;
    }

    private DiskType(int value) {
        intValue = value;
        DiskType.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static DiskType forValue(int value) {
        return getMappings().get(value);
    }

}
