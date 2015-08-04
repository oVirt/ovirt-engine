package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.compat.Guid;

public class StorageConnectionExtensionResourceTestHelper {
    protected static StorageServerConnectionExtension getEntity(Guid extensionID, Guid hostID, String pass, String username, String iqn) {
        StorageServerConnectionExtension extension = new StorageServerConnectionExtension();

        extension.setId(extensionID);
        extension.setHostId(hostID);
        extension.setPassword(pass);
        extension.setUserName(username);
        extension.setIqn(iqn);

        return extension;
    }

    protected static StorageConnectionExtension getModel(Guid extensionID, Guid hostID, String pass, String username, String iqn) {
        StorageConnectionExtension extension = new StorageConnectionExtension();
        Host host = new Host();
        host.setId(hostID.toString());

        extension.setHost(host);
        extension.setId(extensionID.toString());
        extension.setTarget(iqn);
        extension.setUsername(username);
        extension.setPassword(pass);

        return extension;
    }
}
