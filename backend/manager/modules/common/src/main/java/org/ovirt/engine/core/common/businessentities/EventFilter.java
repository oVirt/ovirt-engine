package org.ovirt.engine.core.common.businessentities;

public interface EventFilter {

    boolean isSubscribed(AuditLogEvent event);

}
