package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionExtensionMapper {
    @Mapping(from = StorageConnectionExtension.class, to = org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension.class)
    public static StorageServerConnectionExtension map(StorageConnectionExtension restConnectionModel, StorageServerConnectionExtension template) {
        StorageServerConnectionExtension engineConnectionExt = template != null ? template : new StorageServerConnectionExtension();

        if (restConnectionModel.isSetId()) {
            engineConnectionExt.setId(new Guid(restConnectionModel.getId()));
        }
        if (restConnectionModel.isSetHost() && restConnectionModel.getHost().isSetId()) {
            engineConnectionExt.setHostId(new Guid(restConnectionModel.getHost().getId()));
        }
        if (restConnectionModel.isSetTarget()) {
            engineConnectionExt.setIqn(restConnectionModel.getTarget());
        }
        if (restConnectionModel.isSetUsername()) {
            engineConnectionExt.setUserName(restConnectionModel.getUsername());
        }
        if (restConnectionModel.isSetPassword()) {
            engineConnectionExt.setPassword(restConnectionModel.getPassword());
        }
        return engineConnectionExt;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension.class, to = StorageConnectionExtension.class)
    public static StorageConnectionExtension map(StorageServerConnectionExtension connectionExtension, StorageConnectionExtension template) {
        StorageConnectionExtension  modelConnectionExt = template != null ? template : new StorageConnectionExtension();

        modelConnectionExt.setId(connectionExtension.getId().toString());
        Host host = new Host();
        if (connectionExtension.getHostId() != null) {
            host.setId(connectionExtension.getHostId().toString());
        }
        modelConnectionExt.setHost(host);
        modelConnectionExt.setTarget(connectionExtension.getIqn());
        modelConnectionExt.setUsername(connectionExtension.getUserName());
        modelConnectionExt.setPassword(connectionExtension.getPassword());
        return modelConnectionExt;
    }
}
