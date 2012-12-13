package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum LdapRefStatus implements Identifiable {
    Inactive(0),
    Active(1);

    private int intValue;
    private static java.util.HashMap<Integer, LdapRefStatus> mappings = new HashMap<Integer, LdapRefStatus>();

    static {
        for (LdapRefStatus status : values()) {
            mappings.put(status.getValue(), status);
        }
    }

    private LdapRefStatus(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static LdapRefStatus forValue(int value) {
        return mappings.get(value);
    }
}
