package org.ovirt.engine.core.notifier.transport;

import org.ovirt.engine.core.common.businessentities.AuditLogEvent;
import org.ovirt.engine.core.common.businessentities.AuditLogEventSubscriber;

public interface Transport {

    /**
     * Sends an event to a subscriber.
     *
     * @param auditLogEvent an audit log event
     * @param AuditLogEventSubscriber the subscriber subscribed to receive this event.
     * @return an EventSenderResult representing the outcome of the operation.
     */

    public EventSenderResult send(AuditLogEvent auditLogEvent, AuditLogEventSubscriber AuditLogEventSubscriber);

}
