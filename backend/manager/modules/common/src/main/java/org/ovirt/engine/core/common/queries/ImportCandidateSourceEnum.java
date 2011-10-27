package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportCandidateSourceEnum")
public enum ImportCandidateSourceEnum {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    KVM,
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    VMWARE;

    public int getValue() {
        return this.ordinal();
    }

    public static ImportCandidateSourceEnum forValue(int value) {
        return values()[value];
    }
}
