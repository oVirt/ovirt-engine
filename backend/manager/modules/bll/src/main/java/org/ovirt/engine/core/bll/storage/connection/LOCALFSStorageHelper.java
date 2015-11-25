package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class LOCALFSStorageHelper extends BaseFsStorageHelper {

    @Override
    protected StorageType getType() {
        return StorageType.LOCALFS;
    }
}
