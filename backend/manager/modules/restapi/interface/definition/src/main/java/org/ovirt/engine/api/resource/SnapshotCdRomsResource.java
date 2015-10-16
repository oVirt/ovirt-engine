package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Cdroms;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface SnapshotCdRomsResource {

    @GET
    public Cdroms list();

    @Path("{id}")
    public SnapshotCdRomResource getCdRomResource(@PathParam("id") String id);
}
