package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendIscsiBondStorageServerConnection extends BackendStorageServerConnectionResource {

    public BackendIscsiBondStorageServerConnection(String id, BackendStorageServerConnectionsResource parent) {
        super(id, parent);
    }

    @Override
    public StorageConnection get() {
        return performGet(VdcQueryType.GetStorageServerConnectionByIscsiBondId, new StorageServerConnectionQueryParametersBase(guid.toString()));
    }
}
