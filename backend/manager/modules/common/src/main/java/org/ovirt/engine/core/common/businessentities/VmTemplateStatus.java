package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//using VdcDAL.DbBroker;

//[Serializable]
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmTemplateStatus")
public enum VmTemplateStatus {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    OK(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Locked(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Illegal(2);

    private int intValue;
    private static java.util.HashMap<Integer, VmTemplateStatus> mappings = new HashMap<Integer, VmTemplateStatus>();

    static {
        for (VmTemplateStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private VmTemplateStatus(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VmTemplateStatus forValue(int value) {
        return mappings.get(value);
    }
}
