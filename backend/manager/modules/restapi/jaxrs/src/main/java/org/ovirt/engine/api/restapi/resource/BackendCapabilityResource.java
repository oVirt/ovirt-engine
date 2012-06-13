package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VersionCaps;
import org.ovirt.engine.api.resource.CapabiliyResource;

public class BackendCapabilityResource extends BackendResource implements CapabiliyResource {

    BackendCapabilitiesResource parent;
    String id;

    public BackendCapabilityResource(String id, BackendCapabilitiesResource parent) {
        super();
        this.parent=parent;
        this.id=id;
    }

    @Override
    public VersionCaps get() {
        for (Version v : this.parent.getSupportedClusterLevels()) {
            if (parent.generateId(v).equals(this.id)){
                return this.parent.generateVersionCaps(v);
            }
        }
        return notFound();
    }

    protected VersionCaps notFound() {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
