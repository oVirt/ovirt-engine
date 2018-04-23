package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum Jedi implements Identifiable {
    Anakin(0),
    Luke(1),
    Leia(2),
    Yoda(3),
    Mace(4);

    private int intValue;

    private static Map<Integer, Jedi> mappings;

    private Jedi(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    @Override
    public int getValue() {
        return intValue;
    }

    private static synchronized Map<Integer, Jedi> getMappings() {
        if (mappings == null) {
            mappings = new HashMap<>();
        }
        return mappings;
    }

    public static Jedi forValue(int value) {
        return getMappings().get(value);
    }
}
