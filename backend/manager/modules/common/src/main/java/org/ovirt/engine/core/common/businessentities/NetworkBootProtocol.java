package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NetworkBootProtocol")
public enum NetworkBootProtocol {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    None(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    Dhcp(1),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    StaticIp(2);

    private int intValue;
    private static java.util.HashMap<Integer, NetworkBootProtocol> mappings;

    private synchronized static java.util.HashMap<Integer, NetworkBootProtocol> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, NetworkBootProtocol>();
        }
        return mappings;
    }

    private NetworkBootProtocol(int value) {
        intValue = value;
        NetworkBootProtocol.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static NetworkBootProtocol forValue(int value) {
        return getMappings().get(value);
    }
}
