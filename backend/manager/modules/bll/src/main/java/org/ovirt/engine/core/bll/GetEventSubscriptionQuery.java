package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.queries.GetEventSubscriptionQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.EventDao;

/**
 * This query returns the event-subscription for the specified user and event.
 */
public class GetEventSubscriptionQuery <P extends GetEventSubscriptionQueryParameters>
extends QueriesCommandBase<P> {

    @Inject
    private EventDao eventDao;

    public GetEventSubscriptionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid userId = getParameters().getUserId();
        AuditLogType event = getParameters().getEvent();
        EventSubscriber eventSubscription = eventDao.getEventSubscription(userId, event);
        getQueryReturnValue().setReturnValue(eventSubscription);
    }
}
