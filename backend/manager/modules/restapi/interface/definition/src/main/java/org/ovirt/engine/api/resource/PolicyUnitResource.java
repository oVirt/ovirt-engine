package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.BaseResource;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface PolicyUnitResource<T extends BaseResource> {
    @GET
    public T get();
}
