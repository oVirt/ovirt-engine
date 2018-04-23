package org.ovirt.engine.core.common;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public enum AuditLogSeverity implements Identifiable {
    NORMAL(0),
    WARNING(1),
    ERROR(2),
    // Alerts
    ALERT(10);

    private int intValue;
    private static final Map<Integer, AuditLogSeverity> mappings = new HashMap<>();

    static {
        for (AuditLogSeverity logSeverity : values()) {
            mappings.put(logSeverity.getValue(), logSeverity);
        }
    }

    private AuditLogSeverity(int value) {
        intValue = value;
    }

    @Override
    public int getValue() {
        return intValue;
    }

    public static AuditLogSeverity forValue(int value) {
        return mappings.get(value);
    }

}
