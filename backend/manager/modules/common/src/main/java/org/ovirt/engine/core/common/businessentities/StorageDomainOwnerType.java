package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainOwnerType")
public enum StorageDomainOwnerType {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Unknown(0);

    private int intValue;
    private static java.util.HashMap<Integer, StorageDomainOwnerType> mappings;

    private synchronized static java.util.HashMap<Integer, StorageDomainOwnerType> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, StorageDomainOwnerType>();
        }
        return mappings;
    }

    private StorageDomainOwnerType(int value) {
        intValue = value;
        StorageDomainOwnerType.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static StorageDomainOwnerType forValue(int value) {
        return getMappings().get(value);
    }
}
