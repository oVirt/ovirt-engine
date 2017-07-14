package org.ovirt.engine.core.bll.storage.connection;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

@Singleton
public class NFSStorageHelper extends BaseFsStorageHelper {

    @Override
    public StorageType getType() {
        return StorageType.NFS;
    }
}
