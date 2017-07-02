package org.ovirt.engine.core.common.action;

import java.util.Collection;

import org.ovirt.engine.core.compat.Guid;

public class SyncStorageDomainsLunsParameters extends SyncLunsParameters {

    private static final long serialVersionUID = -3493377134187960265L;

    private Collection<Guid> storageDomainsToSync;

    public SyncStorageDomainsLunsParameters() {
        this(null, null);
    }

    public SyncStorageDomainsLunsParameters(Guid vdsId, Collection<Guid> storageDomainsToSync) {
        setStoragePoolId(Guid.Empty);
        setVdsId(vdsId);
        setStorageDomainsToSync(storageDomainsToSync);
    }

    public Collection<Guid> getStorageDomainsToSync() {
        return storageDomainsToSync;
    }

    public void setStorageDomainsToSync(Collection<Guid> storageDomainsToSync) {
        this.storageDomainsToSync = storageDomainsToSync;
    }
}
