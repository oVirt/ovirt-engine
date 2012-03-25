package org.ovirt.engine.api.restapi.resource;

public abstract class BackendSnapshotElementsResource{

    protected BackendSnapshotResource parent;
    protected String vmId;

    public BackendSnapshotElementsResource(BackendSnapshotResource parent, String vmId) {
        super();
        this.parent = parent;
        this.vmId = vmId;
    }
}
