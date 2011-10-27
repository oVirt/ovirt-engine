package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * C - HardDisk, D - CDROM, N - Network first 3 numbers for backward compatibility
 */
// C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET
// attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "BootSequence")
public enum BootSequence {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    C(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    DC(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    N(2),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    CDN(3),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    CND(4),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    DCN(5),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    DNC(6),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    NCD(7),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    NDC(8),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    CD(9),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    D(10),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    CN(11),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    DN(12),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    NC(13),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    ND(14);

    private int intValue;
    private static java.util.HashMap<Integer, BootSequence> mappings;

    private synchronized static java.util.HashMap<Integer, BootSequence> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, BootSequence>();
        }
        return mappings;
    }

    private BootSequence(int value) {
        intValue = value;
        BootSequence.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static BootSequence forValue(int value) {
        return getMappings().get(value);
    }
}
