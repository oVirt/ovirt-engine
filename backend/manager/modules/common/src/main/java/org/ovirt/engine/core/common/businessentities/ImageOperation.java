package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImageOperation")
public enum ImageOperation {
    Unassigned,
    Copy,
    Move;

    public int getValue() {
        return this.ordinal();
    }

    public static ImageOperation forValue(int value) {
        return values()[value];
    }
}
