package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainType")
public enum StorageDomainType {

    Master,
    Data,
    ISO,
    ImportExport,
    Unknown;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainType forValue(int value) {
        return values()[value];
    }

}
