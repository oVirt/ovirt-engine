package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.resource.FileResource;

public class BackendFileResource implements FileResource {

    private String id;
    private BackendFilesResource parent;

    public BackendFileResource(String id, BackendFilesResource parent) {
        this.id = id;
        this.parent = parent;
    }

    @Override
    public File get() {
        return parent.lookupFile(id);
    }
}
