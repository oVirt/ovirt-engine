package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainStatus")
public enum StorageDomainStatus {
    Unknown,
    Uninitialized,
    Unattached,
    Active,
    InActive,
    Locked,
    Maintenance;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainStatus forValue(int value) {
        return values()[value];
    }

}
