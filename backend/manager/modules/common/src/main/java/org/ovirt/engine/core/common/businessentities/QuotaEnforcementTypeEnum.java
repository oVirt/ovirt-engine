package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum QuotaEnforcementTypeEnum implements Identifiable {
    DISABLED(0),
    SOFT_ENFORCEMENT(1),
    HARD_ENFORCEMENT(2);

    private final int enforcementType;
    private static final Map<Integer, QuotaEnforcementTypeEnum> mappings =
            Stream.of(values()).collect(Collectors.toMap(QuotaEnforcementTypeEnum::getValue, Function.identity()));

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
