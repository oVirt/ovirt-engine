package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "PropagateErrors")
public enum PropagateErrors {
    Off,
    On;

    public int getValue() {
        return this.ordinal();
    }

    public static PropagateErrors forValue(int value) {
        return values()[value];
    }
}
