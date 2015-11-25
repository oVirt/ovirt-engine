package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class NFSStorageHelper extends BaseFsStorageHelper {

    @Override
    protected StorageType getType() {
        return StorageType.NFS;
    }
}
