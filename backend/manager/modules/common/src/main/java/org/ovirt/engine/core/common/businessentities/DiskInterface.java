package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskInterface")
public enum DiskInterface {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    IDE(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    SCSI(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    VirtIO(2);

    private int intValue;
    private static java.util.HashMap<Integer, DiskInterface> mappings;

    private synchronized static java.util.HashMap<Integer, DiskInterface> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, DiskInterface>();
        }
        return mappings;
    }

    private DiskInterface(int value) {
        intValue = value;
        DiskInterface.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static DiskInterface forValue(int value) {
        return getMappings().get(value);
    }
}
