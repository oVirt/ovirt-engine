package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.EventSubscription;
import org.ovirt.engine.api.model.NotifiableEvent;
import org.ovirt.engine.api.resource.EventSubscriptionResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.compat.Guid;

public class BackendEventSubscriptionResource extends AbstractBackendSubResource<EventSubscription, EventSubscriber> implements EventSubscriptionResource {

    private NotifiableEvent event;
    private String userId;
    private BackendEventSubscriptionsResource parent;

    public BackendEventSubscriptionResource(String id, BackendEventSubscriptionsResource parent) {
        // super constructor invoked with an empty guid string as ID, otherwise it fails
        // (it converts the string to a Guid). This ID is then immediately overridden with
        // the event, which is the real ID.
        super(Guid.Empty.toString(), EventSubscription.class, EventSubscriber.class);
        this.id = id;
        this.event = NotifiableEvent.valueOf(id.toUpperCase());
        this.parent = parent;
        this.userId = parent.getUserId();
    }

    @Override
    public EventSubscription get() {
        EventSubscriber eventSubscriber = parent.getEventSubscriber(userId, event);
        return addLinks(populate(map(eventSubscriber, new EventSubscription()), eventSubscriber));
    }

    @Override
    public Response remove() {
        EventSubscriber eventSubscriber = parent.getEventSubscriber(userId, event);
        return performAction(ActionType.RemoveEventSubscription,
                new EventSubscriptionParametesBase(eventSubscriber, null));
    }

}
