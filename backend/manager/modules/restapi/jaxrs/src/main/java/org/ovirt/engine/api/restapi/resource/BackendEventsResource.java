package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Events;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.action.RemoveExternalEventParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
        return inject(new BackendEventResource(id, this));
    }

    private List<AuditLog> getBackendCollection() {
        if (isFiltered()) {
            return getBackendCollection(VdcQueryType.GetAllEventMessages, new VdcQueryParametersBase());
        } else {
            return getBackendCollection(SearchType.AuditLog);
        }
    }

    public Event lookupEvent(String id) {
        try {
            Long longId = Long.valueOf(id);
            for (AuditLog auditLog : getBackendCollection()) {
                if (auditLog.getaudit_log_id() == longId)
                    return addLinks(map(auditLog));
            }
            return notFound();
        } catch (NumberFormatException e) {
            return notFound();
        }
    }

    @Override
    public Response add(Event event) {
        validateParameters(event, "origin", "severity", "customId", "description");
        return performCreate(VdcActionType.AddExternalEvent,
                               new AddExternalEventParameters(map(event)),
                               new QueryIdResolver<Long>(VdcQueryType.GetAuditLogById, GetAuditLogByIdParameters.class));
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveExternalEvent, new RemoveExternalEventParameters(asLong(id)));
    }

    @Override
    protected Event doPopulate(Event model, AuditLog entity) {
        return model;
    }
}
