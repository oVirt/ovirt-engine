package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainSharedStatus")
public enum StorageDomainSharedStatus {
    Unattached,
    Active,
    InActive,
    Mixed,
    Locked;

    public int getValue() {
        return this.ordinal();
    }

    public static StorageDomainSharedStatus forValue(int value) {
        return values()[value];
    }
}
