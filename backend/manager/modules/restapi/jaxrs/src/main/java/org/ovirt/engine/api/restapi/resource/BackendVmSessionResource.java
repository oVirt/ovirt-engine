package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.resource.VmSessionResource;
import org.ovirt.engine.api.resource.VmSessionsResource;

public class BackendVmSessionResource extends AbstractBackendResource<Session, Object> implements VmSessionResource {

    private VmSessionsResource parent;
    private String id;

    public BackendVmSessionResource(VmSessionsResource parent, String id) {
        super(Session.class, Object.class);
        this.parent = parent;
        this.id = id;
    }

    @Override
    public Session get() {
        // Get all sessions and search for the session with this ID. Must be done this way because
        // there's no way to get session by ID from the engine.
        Sessions sessions = parent.list();
        if (sessions.isSetSessions()) {
            for (Session session : sessions.getSessions()) {
                if (session.getId().equals(id)) {
                    return session;
                }
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND); // shouldn't happen.
    }
}
