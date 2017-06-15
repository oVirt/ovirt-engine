package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;

public class StorageServerConnectionExtensionParameters extends ActionParametersBase {

    @Valid
    StorageServerConnectionExtension connExt;

    public StorageServerConnectionExtensionParameters(StorageServerConnectionExtension connExt) {
        this.connExt = connExt;
    }

    public StorageServerConnectionExtensionParameters() {}

    public StorageServerConnectionExtension getStorageServerConnectionExtension() {
        return connExt;
    }

    public void setStorageServerConnectionExtension(StorageServerConnectionExtension connExt) {
        this.connExt = connExt;
    }
}
