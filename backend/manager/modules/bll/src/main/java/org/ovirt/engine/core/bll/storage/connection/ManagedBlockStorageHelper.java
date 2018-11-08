package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@Singleton
public class ManagedBlockStorageHelper extends StorageHelperBase {
    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain,
            Guid vdsId,
            int type) {
        return new Pair<>(true, null);
    }

    @Override
    public boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.MANAGED_BLOCK_STORAGE);
    }

    @Override
    public boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext,
            ConnectHostToStoragePoolServersParameters parameters,
            List<StorageServerConnections> connections) {
        return true;
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return true;
    }
}
