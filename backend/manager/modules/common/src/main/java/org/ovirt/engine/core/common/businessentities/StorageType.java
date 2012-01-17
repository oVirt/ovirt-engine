package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageType")
public enum StorageType {
    UNKNOWN(0),
    NFS(1),
    FCP(2),
    ISCSI(3),
    LOCALFS(4),
    // CIFS(5)
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
