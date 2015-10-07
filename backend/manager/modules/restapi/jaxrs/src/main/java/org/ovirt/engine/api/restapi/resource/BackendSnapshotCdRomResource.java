package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.resource.SnapshotCdromResource;

public class BackendSnapshotCdRomResource implements SnapshotCdromResource {

    protected String cdRomId;
    protected BackendSnapshotCdRomsResource collection;

    public BackendSnapshotCdRomResource(String cdRomId, BackendSnapshotCdRomsResource collection) {
        super();
        this.cdRomId = cdRomId;
        this.collection = collection;
    }

    @Override
    public Cdrom get() {
        for (Cdrom cdRom : collection.list().getCdroms()) {
            if (cdRom.getId().equals(cdRomId)) {
                return cdRom;
            }
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }
}
