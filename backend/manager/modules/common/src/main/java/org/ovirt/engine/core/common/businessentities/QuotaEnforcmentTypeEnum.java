package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "QuotaEnforcmentTypeEnum")
public enum QuotaEnforcmentTypeEnum {
    DISABLED(0),
    SOFT_ENFORCEMENT(1),
    HARD_ENFORCEMENT(2);

    private final int enforcementType;
    private static java.util.HashMap<Integer, QuotaEnforcmentTypeEnum> mappings =
            new HashMap<Integer, QuotaEnforcmentTypeEnum>();

    static {
        for (QuotaEnforcmentTypeEnum component : values()) {
            mappings.put(component.getValue(), component);
        }
    }

    public int getValue() {
        return enforcementType;
    }

    public static QuotaEnforcmentTypeEnum forValue(int value) {
        return mappings.get(value);
    }

    private QuotaEnforcmentTypeEnum(int enforcementType) {
        this.enforcementType = enforcementType;
    }

    public int getQuotaEnforcementType() {
        return this.enforcementType;
    }
}
