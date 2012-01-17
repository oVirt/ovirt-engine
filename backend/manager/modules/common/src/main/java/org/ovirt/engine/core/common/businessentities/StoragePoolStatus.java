package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StoragePoolStatus")
public enum StoragePoolStatus {
    Uninitialized,
    Up,
    Maintanance,
    NotOperational,
    Problematic,
    Contend;

    public int getValue() {
        return this.ordinal();
    }

    public static StoragePoolStatus forValue(int value) {
        return values()[value];
    }
}
