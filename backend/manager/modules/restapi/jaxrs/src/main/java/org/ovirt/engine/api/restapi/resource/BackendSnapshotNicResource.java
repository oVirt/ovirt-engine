package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.resource.SnapshotNicResource;

public class BackendSnapshotNicResource implements SnapshotNicResource {

    protected String nicId;
    protected BackendSnapshotNicsResource collection;

    public BackendSnapshotNicResource(String nicId, BackendSnapshotNicsResource collection) {
        super();
        this.nicId = nicId;
        this.collection = collection;
    }

    @Override
    public Nic get() {
        for (Nic nic : collection.list().getNics()) {
            if (nic.getId().equals(nicId)) {
                return nic;
            }
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }
}
