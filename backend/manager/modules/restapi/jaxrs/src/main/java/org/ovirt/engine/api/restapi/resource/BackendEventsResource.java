package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Events;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;

public class BackendEventsResource extends
AbstractBackendCollectionResource<Event, AuditLog> implements
EventsResource {
    public BackendEventsResource() {
        super(Event.class, AuditLog.class);
    }

    @Override
    public Events list() {
        return mapCollection(getBackendCollection());
    }

    private Events mapCollection(List<AuditLog> entities) {
        Events collection = new Events();
        for (AuditLog entity : entities) {
            collection.getEvent().add(addLinks(map(entity)));
        }
        return collection;
    }

    @Override
    @SingleEntityResource
    public EventResource getEventSubResource(String id) {
        return new BackendEventResource(id, this);
    }

    private List<AuditLog> getBackendCollection() {
        return getBackendCollection(SearchType.AuditLog);
    }

    public Event lookupEvent(String id) {
        Long longId = Long.valueOf(id);
        for (AuditLog auditLog : getBackendCollection()) {
            if (auditLog.getaudit_log_id() == longId)
                return addLinks(map(auditLog));
        }
        return notFound();
    }

    @Override
    protected Response performRemove(String id) {
       throw new UnsupportedOperationException();
    }
}
