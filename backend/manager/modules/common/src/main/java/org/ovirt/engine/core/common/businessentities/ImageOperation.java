package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImageOperation")
public enum ImageOperation {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Unassigned(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Copy(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Move(2);

    private int intValue;
    private static java.util.HashMap<Integer, ImageOperation> mappings;

    private synchronized static java.util.HashMap<Integer, ImageOperation> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, ImageOperation>();
        }
        return mappings;
    }

    private ImageOperation(int value) {
        intValue = value;
        ImageOperation.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static ImageOperation forValue(int value) {
        return getMappings().get(value);
    }
}
