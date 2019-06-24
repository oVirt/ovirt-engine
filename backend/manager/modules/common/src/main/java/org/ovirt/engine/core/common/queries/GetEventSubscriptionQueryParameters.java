package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;

public class GetEventSubscriptionQueryParameters extends QueryParametersBase {

    public GetEventSubscriptionQueryParameters() {
        super();
    }

    public GetEventSubscriptionQueryParameters(Guid userId, AuditLogType event) {
        super();
        this.userId = userId;
        this.event = event;
    }

    private Guid userId;
    private AuditLogType event;

    public Guid getUserId() {
        return userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public AuditLogType getEvent() {
        return event;
    }

    public void setEvent(AuditLogType event) {
        this.event = event;
    }

}
