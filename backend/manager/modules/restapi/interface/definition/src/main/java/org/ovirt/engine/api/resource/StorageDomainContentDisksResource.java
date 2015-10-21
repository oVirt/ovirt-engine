package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Disks;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface StorageDomainContentDisksResource {
    @GET
    Disks list();

    @Path("{id}")
    StorageDomainContentDiskResource getDiskResource(@PathParam("id") String id);
}
