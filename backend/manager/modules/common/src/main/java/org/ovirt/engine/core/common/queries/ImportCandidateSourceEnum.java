package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportCandidateSourceEnum")
public enum ImportCandidateSourceEnum {
    KVM,
    VMWARE;

    public int getValue() {
        return this.ordinal();
    }

    public static ImportCandidateSourceEnum forValue(int value) {
        return values()[value];
    }
}
