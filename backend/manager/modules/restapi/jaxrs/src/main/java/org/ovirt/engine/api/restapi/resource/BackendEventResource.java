package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendEventResource extends AbstractBackendActionableResource<Event, AuditLog> implements EventResource {
    public BackendEventResource(String id) {
        super(id, Event.class, AuditLog.class);
    }

    @Override
    public Event get() {
        return performGet(VdcQueryType.GetAuditLogById, new GetAuditLogByIdParameters(asLong(id)));
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifiers of events aren't UUIDs:
        return null;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveAuditLogById, new RemoveAuditLogByIdParameters(asLong(id)));
    }
}
