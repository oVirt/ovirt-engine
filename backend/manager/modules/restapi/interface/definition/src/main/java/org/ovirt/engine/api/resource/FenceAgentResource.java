package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.Agent;

import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface FenceAgentResource extends UpdatableResource<Agent> {
    @DELETE
    Response remove();
}
