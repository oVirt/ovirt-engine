package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum Jedi implements Identifiable {
    Anakin(0),
    Luke(1),
    Leia(2),
    Yoda(3),
    Mace(4);

    private int intValue;

    private static HashMap<Integer, Jedi> mappings;

    private Jedi(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    @Override
    public int getValue() {
        return intValue;
    }

    private synchronized static HashMap<Integer, Jedi> getMappings() {
        if (mappings == null) {
            mappings = new HashMap<Integer, Jedi>();
        }
        return mappings;
    }

    public static Jedi forValue(int value) {
        return getMappings().get(value);
    }
}
