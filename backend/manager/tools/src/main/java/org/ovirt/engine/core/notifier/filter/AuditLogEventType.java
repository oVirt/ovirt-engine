package org.ovirt.engine.core.notifier.filter;

public enum AuditLogEventType {

    resolveMessage(0, "Issue Solved Notification."),

    alertMessage(1, "Alert Notification.");

    private String message;
    private int value;

    private AuditLogEventType(int eventType, String message) {
        this.message = message;
        this.value = eventType;
    }

    public String getMessage() {
        return message;
    }

    public int getValue() {
        return value;
    }

    public static String getMessageByEventType(int value) {
        for (AuditLogEventType m : values()) {
            if (value == m.value) {
                return m.message;
            }
        }
        throw new IllegalArgumentException("No AuditLogEventType associated with value " + value);
    }
}
