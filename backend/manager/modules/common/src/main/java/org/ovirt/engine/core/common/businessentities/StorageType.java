package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageType")
public enum StorageType {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    UNKNOWN(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    NFS(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    FCP(2),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    ISCSI(3),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    LOCALFS(4),
    // [EnumMember]
    // CIFS(5)
    // [EnumMember]
    ALL(6);

    private int intValue;
    private static java.util.HashMap<Integer, StorageType> mappings = new HashMap<Integer, StorageType>();

    static {
        for (StorageType storageType : values()) {
            mappings.put(storageType.getValue(), storageType);
        }
    }

    private StorageType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static StorageType forValue(int value) {
        return mappings.get(value);
    }

}
