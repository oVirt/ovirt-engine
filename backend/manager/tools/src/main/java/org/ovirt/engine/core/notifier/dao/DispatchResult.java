package org.ovirt.engine.core.notifier.dao;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;

public class DispatchResult {

    private final AuditLogEvent event;
    private final String address;
    private final EventNotificationMethod NotificationMethod;
    private final boolean success;
    private final String errorMessage;

    private DispatchResult(
            AuditLogEvent event,
            String address,
            EventNotificationMethod NotificationMethod,
            boolean success,
            String errorMessage) {

        this.event = event;
        this.address = address;
        this.NotificationMethod = NotificationMethod;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static DispatchResult success(
            AuditLogEvent event,
            String address,
            EventNotificationMethod NotificationMethod) {
        return new DispatchResult(event, address, NotificationMethod, true, null);
    }

    public static DispatchResult failure(
            AuditLogEvent event,
            String address,
            EventNotificationMethod NotificationMethod,
            String errorMessage) {
        return new DispatchResult(event, address, NotificationMethod, false, errorMessage);
    }

    public AuditLogEvent getEvent() {
        return event;
    }

    public String getAddress() {
        return address;
    }

    public EventNotificationMethod getNotificationMethod() {
        return NotificationMethod;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
