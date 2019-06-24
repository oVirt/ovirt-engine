package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.EventSubscription;
import org.ovirt.engine.api.model.EventSubscriptions;
import org.ovirt.engine.api.model.NotifiableEvent;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.EventSubscriptionResource;
import org.ovirt.engine.api.resource.EventSubscriptionsResource;
import org.ovirt.engine.api.restapi.types.EventSubscriptionMapper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.queries.GetEventSubscriptionQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendEventSubscriptionsResource extends AbstractBackendCollectionResource<EventSubscription, EventSubscriber> implements EventSubscriptionsResource {

    private String userId;

    public String getUserId() {
        return userId;
    }

    public BackendEventSubscriptionsResource(String userId) {
        super(EventSubscription.class, EventSubscriber.class);
        this.userId = userId;
    }

    @Override
    public Response add(EventSubscription eventSubscription) {
        // set the user-id for the subscription
        eventSubscription.setUser(new User());
        eventSubscription.getUser().setId(userId);

        // map event-subscription entity (API) to event-subscriber entity (Engine)
        EventSubscriber eventSubscriber = map(eventSubscription, new EventSubscriber());

        // set notification method = SMTP. API currently does not support SNMP.
        eventSubscriber.setEventNotificationMethod(EventNotificationMethod.SMTP);

        // run AddEventSubscription command
        EventSubscriptionParametesBase params = new EventSubscriptionParametesBase(eventSubscriber, null);
        return performCreate(ActionType.AddEventSubscription,
                params,
                new EventSubscriberResolver(userId, eventSubscription.getEvent()));
    }

    @Override
    public EventSubscriptions list() {
        return mapCollection(getBackendCollection(QueryType.GetEventSubscribersBySubscriberIdGrouped,
                new IdQueryParameters(Guid.createGuidFromString(userId))));
    }

    /**
     * Resolver which contains logic for finding EventSubscriber with the
     * provided user-id and event
     */
    private class EventSubscriberResolver implements IResolver<EventSubscription, EventSubscriber> {

        private String userId;
        private NotifiableEvent event;

        public EventSubscriberResolver(String userId, NotifiableEvent event) {
            super();
            this.userId = userId;
            this.event = event;
        }

        @Override
        public EventSubscriber resolve(EventSubscription id) throws BackendFailureException {
            // id is ignored, AddEventSubscriptionCommand doesn't return a value,
            // use provided user-id and event
            return getEventSubscriber(userId, event);
        }
    }

    /**
     * Returns the EventSubscriber entity from the Engine for the provided user-id and event.
     */
    public EventSubscriber getEventSubscriber(String userId, NotifiableEvent event) {
        Guid userIdGuid = Guid.createGuidFromString(userId);
        AuditLogType eventUpName = EventSubscriptionMapper.map(event, null);
        return getEntity(
                EventSubscriber.class,
                QueryType.GetEventSubscription,
                new GetEventSubscriptionQueryParameters(userIdGuid, eventUpName),
                "Subscription of user " + userId + "to event " + event,
                true);
    }

    /**
     * Map event-subscriber entities, which were received from the Engine,
     * to event-subscription entities (API).
     */
    private EventSubscriptions mapCollection(List<EventSubscriber> entities) {
        EventSubscriptions collection = new EventSubscriptions();
        for (EventSubscriber entity : entities) {
            collection.getEventSubscriptions().add(addLinks(populate(map(entity, new EventSubscription()), entity)));
        }
        return collection;
    }

    @Override
    public EventSubscriptionResource getEventSubscriptionResource(String id) {
        return inject(new BackendEventSubscriptionResource(id, this));
    }

}
