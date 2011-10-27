package org.ovirt.engine.core.common.businessentities;

public enum MigrationMethod {
    OFFLINE(0),
    ONLINE(1);

    private int intValue;
    private static java.util.HashMap<Integer, MigrationMethod> mappings;

    private synchronized static java.util.HashMap<Integer, MigrationMethod> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, MigrationMethod>();
        }
        return mappings;
    }

    private MigrationMethod(int value) {
        intValue = value;
        MigrationMethod.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static MigrationMethod forValue(int value) {
        return getMappings().get(value);
    }
}
