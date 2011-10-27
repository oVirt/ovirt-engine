package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "InterfaceStatus")
public enum InterfaceStatus {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    None(0),

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Up(1),

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Down(2);

    private int intValue;
    private static java.util.HashMap<Integer, InterfaceStatus> mappings;

    private synchronized static java.util.HashMap<Integer, InterfaceStatus> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, InterfaceStatus>();
        }
        return mappings;
    }

    private InterfaceStatus(int value) {
        intValue = value;
        InterfaceStatus.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static InterfaceStatus forValue(int value) {
        return getMappings().get(value);
    }
}
