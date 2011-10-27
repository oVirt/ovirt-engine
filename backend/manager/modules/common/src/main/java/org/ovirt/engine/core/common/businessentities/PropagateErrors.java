package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "PropagateErrors")
public enum PropagateErrors {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Off(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    On(1);

    private int intValue;
    private static java.util.HashMap<Integer, PropagateErrors> mappings;

    private synchronized static java.util.HashMap<Integer, PropagateErrors> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, PropagateErrors>();
        }
        return mappings;
    }

    private PropagateErrors(int value) {
        intValue = value;
        PropagateErrors.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static PropagateErrors forValue(int value) {
        return getMappings().get(value);
    }
}
