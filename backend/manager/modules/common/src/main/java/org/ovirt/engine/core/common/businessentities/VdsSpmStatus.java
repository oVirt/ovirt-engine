package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER NOTE: There is no Java equivalent to C# namespace aliases:
//using Timer=System.Timers.Timer;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsSpmStatus")
public enum VdsSpmStatus {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    None(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Contending(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    SPM(2);

    private int intValue;
    private static java.util.HashMap<Integer, VdsSpmStatus> mappings = new HashMap<Integer, VdsSpmStatus>();

    static {
        for (VdsSpmStatus vdsSpmStatus : values()) {
            mappings.put(vdsSpmStatus.getValue(), vdsSpmStatus);
        }
    }

    private VdsSpmStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VdsSpmStatus forValue(int value) {
        return mappings.get(value);
    }
}
