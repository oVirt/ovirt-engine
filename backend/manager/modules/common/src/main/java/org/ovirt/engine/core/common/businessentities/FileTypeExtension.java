package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FileTypeExtension")
public enum FileTypeExtension {
    Unknown,
    ISO,
    Floppy;

    public int getValue() {
        return this.ordinal();
    }

    public static FileTypeExtension forValue(int value) {
        return values()[value];
    }
}
