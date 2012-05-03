package org.ovirt.engine.core.common;

import java.util.HashMap;

public enum AuditLogSeverity {
    NORMAL(0),
    WARNING(1),
    ERROR(2),
    // Alerts
    ALERT(10);

    private int intValue;
    private static java.util.HashMap<Integer, AuditLogSeverity> mappings = new HashMap<Integer, AuditLogSeverity>();

    static {
        for (AuditLogSeverity logSeverity : values()) {
            mappings.put(logSeverity.getValue(), logSeverity);
        }
    }

    private AuditLogSeverity(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static AuditLogSeverity forValue(int value) {
        return mappings.get(value);
    }
}
