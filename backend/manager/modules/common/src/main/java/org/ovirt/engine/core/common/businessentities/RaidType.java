package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum RaidType {
    None(-1),
    Raid0(0),
    Raid1(1),
    Raid2(4),
    Raid5(5),
    Raid6(6),
    Raid10(10);

    private int intValue;
    private static final HashMap<Integer, RaidType> mappings = new HashMap<Integer, RaidType>();

    static {
        for (RaidType raidType : values()) {
            mappings.put(raidType.getValue(), raidType);
        }
    }

    private RaidType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static RaidType forValue(int value) {
        return mappings.get(value);
    }

}
