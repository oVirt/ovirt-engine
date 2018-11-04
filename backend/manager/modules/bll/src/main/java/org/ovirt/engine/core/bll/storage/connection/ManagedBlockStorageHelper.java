package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
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
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.MANAGED_BLOCK_STORAGE);
    }
}
