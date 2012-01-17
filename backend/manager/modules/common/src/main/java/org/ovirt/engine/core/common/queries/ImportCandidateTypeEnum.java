package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportCandidateTypeEnum")
public enum ImportCandidateTypeEnum {
    VM,
    TEMPLATE;

    public int getValue() {
        return this.ordinal();
    }

    public static ImportCandidateTypeEnum forValue(int value) {
        return values()[value];
    }
}
