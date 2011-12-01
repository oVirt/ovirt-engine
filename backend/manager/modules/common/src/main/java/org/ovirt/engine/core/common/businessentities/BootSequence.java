package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * C - HardDisk, D - CDROM, N - Network first 3 numbers for backward compatibility
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "BootSequence")
public enum BootSequence {

    C(0),
    DC(1),
    N(2),
    CDN(3),
    CND(4),
    DCN(5),
    DNC(6),
    NCD(7),
    NDC(8),
    CD(9),
    D(10),
    CN(11),
    DN(12),
    NC(13),
    ND(14);

    private int intValue;
    private static Map<Integer, BootSequence> mappings;

    static {
        mappings = new HashMap<Integer, BootSequence>();
        for (BootSequence error : values()) {
            mappings.put(error.getValue(), error);
        }
    }

    private BootSequence(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static BootSequence forValue(int value) {
        return mappings.get(value);
    }
}
