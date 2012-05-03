package org.ovirt.engine.core.common;

public enum AuditLogTimeInterval {
    None(0),
    SECOND(1),
    MINUTE(60),
    HOUR(3600),
    DAY(86400),
    WEEK(604800);

    private int intValue;

    private AuditLogTimeInterval(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }
}
