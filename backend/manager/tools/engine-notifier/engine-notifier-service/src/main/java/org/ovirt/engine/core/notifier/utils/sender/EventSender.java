package org.ovirt.engine.core.notifier.utils.sender;

import org.ovirt.engine.core.common.businessentities.EventAuditLogSubscriber;

/**
 * Defines a method designed to dispatch a message to each implementation method type
 */
public interface EventSender {
    /**
     * Sends a message which constructed by the implementing class to be send as a notification to the subscriber
     * @param eventData
     *            contains data required for constructing a message
     * @param methodAddress
     *            an alternate method address if not provided by {@link event_audit_log_subscriber.getmethod_address()}
     * @return the result of the notification send action
     */
    public EventSenderResult send(EventAuditLogSubscriber eventData, String methodAddress);

}
