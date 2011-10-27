package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RecoveryMode")
public enum RecoveryMode {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Manual(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Safe(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Fast(2);

    private int intValue;
    private static java.util.HashMap<Integer, RecoveryMode> mappings;

    private synchronized static java.util.HashMap<Integer, RecoveryMode> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, RecoveryMode>();
        }
        return mappings;
    }

    private RecoveryMode(int value) {
        intValue = value;
        RecoveryMode.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static RecoveryMode forValue(int value) {
        return getMappings().get(value);
    }
}
