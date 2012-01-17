package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImageStatus")
public enum ImageStatus {
    Unassigned,
    OK,
    LOCKED,
    INVALID,
    ILLEGAL;

    public int getValue() {
        return this.ordinal();
    }

    public static ImageStatus forValue(int value) {
        return values()[value];
    }

}
