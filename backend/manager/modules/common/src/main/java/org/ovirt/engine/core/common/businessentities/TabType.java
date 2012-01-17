package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TabType")
public enum TabType {
    Hosts(0),
    Vms(1),
    Users(2),
    Templates(3),
    Events(4),
    DataCenters(5),
    Clusters(6),
    Storage(7),
    Pools(8);

    private int intValue;
    private static java.util.HashMap<Integer, TabType> mappings = new HashMap<Integer, TabType>();

    static {
        for (TabType type : values()) {
            mappings.put(type.getValue(), type);
        }
    }

    private TabType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static TabType forValue(int value) {
        return mappings.get(value);
    }
}
