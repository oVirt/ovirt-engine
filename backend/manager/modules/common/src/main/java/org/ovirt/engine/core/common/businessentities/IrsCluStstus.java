package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "IrsCluStstus")
public enum IrsCluStstus {
    Inactive(0),
    Active(1);

    private int intValue;
    private static java.util.HashMap<Integer, IrsCluStstus> mappings;

    private synchronized static java.util.HashMap<Integer, IrsCluStstus> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, IrsCluStstus>();
        }
        return mappings;
    }

    private IrsCluStstus(int value) {
        intValue = value;
        IrsCluStstus.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static IrsCluStstus forValue(int value) {
        return getMappings().get(value);
    }
}
