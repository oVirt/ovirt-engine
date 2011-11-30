package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskInterface")
public enum DiskInterface {

    IDE,
    SCSI,
    VirtIO;

    public int getValue() {
        return this.ordinal();
    }

    public static DiskInterface forValue(int value) {
        return values()[value];
    }
}
