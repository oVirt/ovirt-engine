package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "OriginType")
public enum OriginType {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    ENGINE(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    VMWARE(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    XEN(2);

    private int intValue;
    private static java.util.HashMap<Integer, OriginType> mappings;

    private synchronized static java.util.HashMap<Integer, OriginType> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, OriginType>();
        }
        return mappings;
    }

    private OriginType(int value) {
        intValue = value;
        OriginType.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static OriginType forValue(int value) {
        return getMappings().get(value);
    }
}
