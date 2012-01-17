package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SpmStatus")
public enum SpmStatus {
    SPM,
    Contend,
    Free,
    Unknown_Pool,
    SPM_ERROR;

    public int getValue() {
        return this.ordinal();
    }

    public static SpmStatus forValue(int value) {
        return values()[value];
    }

}
