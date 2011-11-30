package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * C - HardDisk, D - CDROM, N - Network first 3 numbers for backward compatibility
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "BootSequence")
public enum BootSequence {

    C,
    DC,
    N,
    CDN,
    CND,
    DCN,
    DNC,
    NCD,
    NDC,
    CD,
    D,
    CN,
    DN,
    NC,
    ND;

    public int getValue() {
        return this.ordinal();
    }

    public static BootSequence forValue(int value) {
        return values()[value];
    }
}
