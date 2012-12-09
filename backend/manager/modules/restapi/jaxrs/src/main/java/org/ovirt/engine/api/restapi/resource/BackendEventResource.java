package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.core.common.businessentities.AuditLog;

public class BackendEventResource extends
AbstractBackendResource<Event, AuditLog> implements EventResource {
    private String id;
    private BackendEventsResource parent;

    public BackendEventResource(String id, BackendEventsResource parent) {
        super(Event.class, AuditLog.class);
        this.id = id;
        this.parent = parent;
    }

    public BackendEventResource() {
        super(Event.class, AuditLog.class);
    }

    @Override
    public Event get() {
        return parent.lookupEvent(id);
    }

    @Override
    protected Event doPopulate(Event model, AuditLog entity) {
        return model;
    }
}
