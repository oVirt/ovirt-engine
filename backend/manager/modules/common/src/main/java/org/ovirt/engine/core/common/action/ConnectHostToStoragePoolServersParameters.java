package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;

public class ConnectHostToStoragePoolServersParameters extends HostStoragePoolParametersBase {
    private boolean connectToInactiveDomains;

    public ConnectHostToStoragePoolServersParameters() {
        connectToInactiveDomains = true;
    }

    public ConnectHostToStoragePoolServersParameters(StoragePool storage_pool, VDS vds) {
        super(storage_pool, vds);
        connectToInactiveDomains = true;
    }

    public ConnectHostToStoragePoolServersParameters(StoragePool storagePool, VDS vds,
            boolean connectToInactiveDomains) {
        super(storagePool, vds);
        this.connectToInactiveDomains = connectToInactiveDomains;
    }

    public boolean isConnectToInactiveDomains() {
        return connectToInactiveDomains;
    }

    public void setConnectToInactiveDomains(boolean connectToInactiveDomains) {
        this.connectToInactiveDomains = connectToInactiveDomains;
    }
}
