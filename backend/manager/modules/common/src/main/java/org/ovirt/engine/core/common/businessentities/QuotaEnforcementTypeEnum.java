package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum QuotaEnforcementTypeEnum implements Identifiable {
    DISABLED(0),
    SOFT_ENFORCEMENT(1),
    HARD_ENFORCEMENT(2);

    private final int enforcementType;
    private static final HashMap<Integer, QuotaEnforcementTypeEnum> mappings = new HashMap<>();

    static {
        for (QuotaEnforcementTypeEnum component : values()) {
            mappings.put(component.getValue(), component);
        }
    }

    @Override
    public int getValue() {
        return enforcementType;
    }

    public static QuotaEnforcementTypeEnum forValue(int value) {
        return mappings.get(value);
    }

    private QuotaEnforcementTypeEnum(int enforcementType) {
        this.enforcementType = enforcementType;
    }

    public int getQuotaEnforcementType() {
        return this.enforcementType;
    }
}
