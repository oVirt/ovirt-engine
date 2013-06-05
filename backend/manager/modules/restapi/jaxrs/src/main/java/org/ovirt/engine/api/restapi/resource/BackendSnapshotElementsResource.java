package org.ovirt.engine.api.restapi.resource;

public abstract class BackendSnapshotElementsResource{

    protected BackendSnapshotResource parent;

    public BackendSnapshotElementsResource(BackendSnapshotResource parent, String vmId) {
        super();
        this.parent = parent;
    }
}
