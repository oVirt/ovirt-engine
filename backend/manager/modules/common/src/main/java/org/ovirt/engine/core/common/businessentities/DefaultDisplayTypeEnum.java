package org.ovirt.engine.core.common.businessentities;

public enum DefaultDisplayTypeEnum {
    RdpNoUseLocal(0),
    RdpUseLocal(1),
    Spice(2);

    private int intValue;
    private static java.util.HashMap<Integer, DefaultDisplayTypeEnum> mappings;

    private synchronized static java.util.HashMap<Integer, DefaultDisplayTypeEnum> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, DefaultDisplayTypeEnum>();
        }
        return mappings;
    }

    private DefaultDisplayTypeEnum(int value) {
        intValue = value;
        DefaultDisplayTypeEnum.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static DefaultDisplayTypeEnum forValue(int value) {
        return getMappings().get(value);
    }
}
