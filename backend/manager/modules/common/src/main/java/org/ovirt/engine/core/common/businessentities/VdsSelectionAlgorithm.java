package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsSelectionAlgorithm")
public enum VdsSelectionAlgorithm {
    None,
    EvenlyDistribute,
    PowerSave;

    public int getValue() {
        return this.ordinal();
    }

    public static VdsSelectionAlgorithm forValue(int value) {
        return values()[value];
    }
}
