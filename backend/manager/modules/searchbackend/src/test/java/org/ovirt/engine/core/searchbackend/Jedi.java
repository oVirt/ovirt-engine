package org.ovirt.engine.core.searchbackend;

public enum Jedi {
    Anakin(0),
    Luke(1),
    Leia(2),
    Yoda(3),
    Mace(4);

    private int intValue;

    private static java.util.HashMap<Integer, Jedi> mappings;

    private Jedi(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    private synchronized static java.util.HashMap<Integer, Jedi> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, Jedi>();
        }
        return mappings;
    }

    public static Jedi forValue(int value) {
        return getMappings().get(value);
    }
}
