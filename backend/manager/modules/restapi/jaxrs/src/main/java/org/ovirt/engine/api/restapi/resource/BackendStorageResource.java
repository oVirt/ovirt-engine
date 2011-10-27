package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.resource.StorageResource;

public class BackendStorageResource implements StorageResource {

    private String id;
    private BackendHostStorageResource parent;

    public BackendStorageResource(String id, BackendHostStorageResource parent) {
        this.id = id;
        this.parent = parent;
    }

    @Override
    public Storage get() {
        return parent.lookupStorage(id);
    }
}
