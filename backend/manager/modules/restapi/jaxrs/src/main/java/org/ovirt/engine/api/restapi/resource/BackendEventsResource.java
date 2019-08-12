package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Events;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.api.restapi.types.ExternalStatusMapper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendEventsResource
    extends AbstractBackendCollectionResource<Event, AuditLog>
    implements EventsResource {

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
            collection.getEvents().add(addLinks(map(entity)));
        }
        return collection;
    }

    @Override
    public Response undelete(Action action) {
        return performAction(ActionType.DisplayAllAuditLogAlerts, new ActionParametersBase(), action, false);
    }

    @Override
    public EventResource getEventResource(String id) {
        return inject(new BackendEventResource(id));
    }

    private List<AuditLog> getBackendCollection() {
        if (isFiltered()) {
            return getBackendCollection(QueryType.GetAllEventMessages, new QueryParametersBase(), SearchType.AuditLog);
        } else {
            return getBackendCollection(SearchType.AuditLog);
        }
    }

    @Override
    public Response add(Event event) {
        validateParameters(event, "origin", "severity", "customId", "description");
        return performCreate(ActionType.AddExternalEvent,
                getParameters(event),
                new QueryIdResolver<Long>(QueryType.GetAuditLogById, GetAuditLogByIdParameters.class));
    }

    private AddExternalEventParameters getParameters(Event event) {

        AddExternalEventParameters parameters;
        boolean isHostExternalStateDefined = event.isSetHost() &&
                event.getHost().isSetExternalStatus();
        boolean isStorageDomainExternalStateDefined = event.isSetStorageDomain() &&
                event.getStorageDomain().isSetExternalStatus();
        if (isHostExternalStateDefined) {
            parameters = new AddExternalEventParameters(
                map(event),
                ExternalStatusMapper.map(event.getHost().getExternalStatus())
            );
        } else if (isStorageDomainExternalStateDefined) {
            parameters = new AddExternalEventParameters(
                map(event),
                ExternalStatusMapper.map(event.getStorageDomain().getExternalStatus())
            );
        } else {
            parameters =  new AddExternalEventParameters(map(event), null);
        }
        return parameters;
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }
}
